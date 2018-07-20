package com.wat.websocket.utils;

/**
 * @Author: chuangwang8
 * @Date: 2018-07-19 14:37
 * @Description:
 */
public class DistributedLockTest {
    static int n = 500;

    public static void secskill() {
        System.out.println(--n);
    }

    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            public void run() {
                DistributedLock lock = null;
                try {
                    lock = new DistributedLock("47.75.53.40:2181", "wangchuang");
                    lock.lock();
                    secskill();
                    System.out.println(Thread.currentThread().getName() + "正在运行");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.err.println("111111111111111111111111");
                    e.printStackTrace();
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
