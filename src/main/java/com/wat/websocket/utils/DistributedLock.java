package com.wat.websocket.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @Date: 2018-07-19 11:42
 * @Description: 基于zk 的分布式锁
 */
public class DistributedLock implements Lock, Watcher {

    Logger logger = LogManager.getLogger();

    private ZooKeeper zk = null;
    /**
     * 根节点
     */
    private String rootLock = "/locks";
    /**
     * 需要加锁的资源
     */
    private String lockName;
    /**
     * 正在等待的锁
     */
    private String waitLock;
    /**
     * 当前锁
     */
    private String currentLock;
    /**
     * 计数器
     * CountDownLatch是一种java.util.concurrent包下一个同步工具类，
     * 它允许一个或多个线程等待直到在其他线程中一组操作执行完成。
     */
    private CountDownLatch countDownLatch;
    /**
     * 获取锁超时时间
     */
    private int sessionTimeout = 3000;
    /**
     * 异常数组
     */
    private List<Exception> exceptionList = new ArrayList<Exception>();

    /**
     * @methdName: DistributedLock
     * @param: config zk地址 127.0.0.1:2181
     * @param: lockName 需要加锁资源
     * @return: DistributedLock 实例
     * @Description: 初始化锁配置
     * @version: V1.0
     */
    public DistributedLock(String config, String lockName) {
        this.lockName = lockName;
        try {
            // 连接zookeeper
            zk = new ZooKeeper(config, sessionTimeout, this);
            Stat stat = zk.exists(rootLock, false);
            if (stat == null) {
                // 如果根节点不存在，则创建根节点
                zk.create(rootLock, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            logger.info("创建根节点失败，异常信息：" + e);
        } catch (InterruptedException e) {
            logger.info("创建根节点失败，异常信息：" + e);
        } catch (KeeperException e) {
            logger.info("创建根节点失败，异常信息：" + e);
        }
    }

    /**
     * @methdName: process
     * @param: [event] 这里只对 exits 事件做了监控
     * @return: void
     * @Description: 当监控的锁被释放删除以后，通知等待者获取锁。 Watched 只会触发一次
     * @version: V1.0
     */
    @Override
    public void process(WatchedEvent event) {
        if (this.countDownLatch != null) {
            this.countDownLatch.countDown();
        }
    }

    /**
     * @methdName: lock
     * @return: void
     * @Description: 锁定
     * @version: V1.0
     */
    @Override
    public void lock() {
        if (exceptionList.size() > 0) {
            throw new LockException(exceptionList.get(0));
        }
        try {
            /**
             * 尝试获取锁
             */
            if (this.tryLock()) {
                logger.info("线程 ：[" + Thread.currentThread().getName() + " ],获得：" + currentLock + "锁");
                return;
            } else {
                /**
                 *等待锁
                 */
                waitForLock(waitLock, sessionTimeout);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * @methdName: tryLock
     * @param: []
     * @return: boolean
     * @Description: 尝试获得锁
     * @version: V1.0
     */
    @Override
    public boolean tryLock() {
        try {
            String splitStr = "_lock_";
            if (lockName.contains(splitStr)) {
                logger.info(lockName + " 锁名称不合法");
                throw new LockException("锁名有误");
            }
            /**
             * 创建临时有序节点
             */
            currentLock = zk.create(rootLock + "/" + lockName + splitStr, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("线程 ：[" + Thread.currentThread().getName() + " ] 创建 [ "+currentLock + " ] 成功!");
            /**
             * 获取所有子节点（不需watch）
             */
            List<String> subNodes = zk.getChildren(rootLock, false);
            /**
             * 取出要关注的锁的 lockName
             */
            List<String> lockObjects = new ArrayList<String>();

            for (String node : subNodes) {
                /**
                 * 子节点命名规则  $lockName +  "_lock_" + 序列号（自增）
                 */
                String nodePrefix = node.split(splitStr)[0];
                if (nodePrefix.equals(lockName)) {
                    lockObjects.add(node);
                }
            }
            /**
             * 排序
             */
            Collections.sort(lockObjects);
            logger.info("线程 [ " + Thread.currentThread().getName() + " ] 尝试获取的锁为： [" + currentLock + "]");
            /**
             * 若当前节点为最小节点，则获取锁成功
             */
            if (currentLock.equals(rootLock + "/" + lockObjects.get(0))) {
                return true;
            }
            /**
             * 若不是最小节点，则找到自己的前一个节点
             */
            String prevNode = currentLock.substring(currentLock.lastIndexOf("/") + 1);
            waitLock = lockObjects.get(Collections.binarySearch(lockObjects, prevNode) - 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @methdName: tryLock
     * @param: [timeout, unit]
     * @return: boolean
     * @Description: 尝试获取锁
     * @version: V1.0
     */
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        try {
            if (this.tryLock()) {
                return true;
            }
            return waitForLock(waitLock, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @methdName: waitForLock
     * @param: prevLock 上一个锁（等待的锁）
     * @param: waitTime  等待时间
     * @return: boolean
     * @Description: 等待获取锁
     * @version: V1.0
     */
    private boolean waitForLock(String prevLock, long waitTime) throws KeeperException, InterruptedException {
        /**
         * 对前一个节点是否存在 做监控
         * 只对要等待的锁做监控可以节省消耗。
         */
        Stat stat = zk.exists(rootLock + "/" + prevLock, true);

        if (stat != null) {
            logger.info("线程 [ " + Thread.currentThread().getName() + " ]等待锁" + rootLock + "/" + prevLock + " 释放");
            this.countDownLatch = new CountDownLatch(1);
            /**
             * 计数等待，若等到前一个节点消失(被删除)，则precess中进行countDown，停止等待，获取锁
             */
            long startTime =System.currentTimeMillis();
            this.countDownLatch.await(waitTime, TimeUnit.MILLISECONDS);
            this.countDownLatch = null;
            long endTime =System.currentTimeMillis();
            logger.info("线程 [ " +Thread.currentThread().getName()+ " ] 等待 [" +currentLock+ "],用时：" + (endTime-startTime) +" ms");
        }
        return true;
    }

    /**
     * @methdName: unlock
     * @param: []
     * @return: void
     * @Description: 释放锁，放在finally 中
     * @version: V1.0
     */
    @Override
    public void unlock() {
        try {
            System.out.println("释放锁 " + currentLock);
            zk.delete(currentLock, -1);
            currentLock = null;
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }


    public class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public LockException(String e) {
            super(e);
        }

        public LockException(Exception e) {
            super(e);
        }
    }
}
