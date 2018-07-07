var webSocket = null;
$(function () {
    connect();
});

/**
 * 改变按钮状态
 * @param connected
 */
function setConnected(connected) {

    document.getElementById('sendUid').disabled = connected;
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('echo').disabled = !connected;
    document.getElementById('echoall').disabled = !connected;
}

function connect() {
    var uid = $("#uid").val();
    console.log("uid :" + uid);
    if ('WebSocket' in window) {
        webSocket = new WebSocket("ws://localhost:8181/websck?uid=" + uid);
    } else {
        webSocket = new SockJS("http://localhost:8181/sockjs/websck/info?uid=" + uid);
    }
    webSocket.onopen = function () {
    };
    webSocket.onmessage = function (event) {
        log(event.data);
    };
    webSocket.onclose = function (event) {
        /**
         * 关闭时清除用户列表
         */
        $('#users').children("p").remove();
        var retMessage = "{\"code\": \"1\",\"message\": \"关闭\"}"
        log(retMessage);
    };
}

function p2pConnect() {
    var sendUid = $("#sendUid ").val();
    if (sendUid == "") {
        alert("用户ID不可以为空！")
        return false;
    }
    console.log("uid :" + sendUid);
    setConnected(true);
    var myconsole = $('#console')[0];
    var pTime = document.createElement('p');
    pTime.style.wordWrap = 'break-word';
    pTime.style.fontSize = "12px";
    pTime.style.textAlign = "center";
    var str = "[ " + getNowFormatDate() + " ]";
    pTime.appendChild(document.createTextNode(str));
    myconsole.appendChild(pTime);

    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.setAttribute("class", "toPeer")
    var str = "  勾搭[ "+sendUid +" ]成功... ";
    var img = document.createElement("img");
    img.src = "/images/4.jpg";
    p.appendChild(document.createTextNode(str));
    p.appendChild(img);
    myconsole.appendChild(p);
}
function zhuxiao() {
    if (webSocket != null) {
        webSocket.close();
        webSocket = null;
    }
}
function disconnect() {
    setConnected(false);
}


/**
 *  type = 1 群发， code = 41
 * @param type
 * @returns {boolean}
 */
function echo(type) {
    if (webSocket != null) {
        var message = document.getElementById('message').value;
        var toUid = $("#sendUid ").val();
        var jStr = {};
        if (message == "") {
            alert("请输入信息...");
            return false;
        }

        if (type == 1) {
            jStr.code = "41";
        } else {
            jStr.code = "21";
        }

        jStr.message = message;
        jStr.toUid = toUid;
        var jsonStr = JSON.stringify(jStr);
        webSocket.send(jsonStr);
        log(jsonStr);
        $("#message").val("");
    } else {
        alert('连接已关闭, 请重新连接.');
    }
}


function sendMsg() {
    if (webSocket.readyState == SockJS.OPEN) {
        var msg = $("#msg").val();
        webSocket.send(msg);
    } else {
        alert("连接失败!")
    }
}

/**
 *  code 0 成功
 *       10 online user
 *       20 其他用户发送的信息
 *       21 用户发送给其他用户信息
 *       30 发送失败
 *
 *
 */
function log(message) {
    var messageData = $.parseJSON(message);
    var console = $('#console')[0];
    var users = $('#users')[0];

    /**
     * 新建连接
     */
    if (messageData.code == 0) {
        var pTime = document.createElement('p');
        pTime.style.wordWrap = 'break-word';
        pTime.style.fontSize = "12px";
        pTime.style.textAlign = "center";
        var str = "[ " + getNowFormatDate() + " ]";
        pTime.appendChild(document.createTextNode(str));
        console.appendChild(pTime);

        var p = document.createElement('p');
        p.style.wordWrap = 'break-word';
        p.setAttribute("class", "main_p")
        var str = "  着陆成功 ^_^  ";
        p.appendChild(document.createTextNode(str));
        console.appendChild(p);
    }

    /**
     * 更新在线用户
     */
    if (messageData.code == 10) {
        $('#users').children("p").remove();
        var sendUid = $("#sendUid");
        sendUid.empty();
        sendUid.append("<option value='-1'>请选择</option> ");
        for (var i = 0; i < messageData.userList.length; i++) {
            var p = document.createElement('p');
            p.style.wordWrap = 'break-word';
            p.style.fontSize = "12px";
            var str = "[ " + getNowFormatDate() + " ] " + messageData.userList[i] + "  ";
            p.appendChild(document.createTextNode(str));
            sendUid.append("<option value='" + messageData.userList[i] + "'>" + messageData.userList[i] + "</option> ");
            users.appendChild(p);
        }
    }
    /**
     * 关闭连接
     */
    if (messageData.code == 1) {
        var pTime = document.createElement('p');
        pTime.style.wordWrap = 'break-word';
        pTime.style.fontSize = "12px";
        pTime.style.textAlign = "center";
        var str = "[ " + getNowFormatDate() + " ]";
        pTime.appendChild(document.createTextNode(str));
        console.appendChild(pTime);

        var p = document.createElement('p');
        p.style.wordWrap = 'break-word';
        var str = " 再见 ^_^  ";
        p.appendChild(document.createTextNode(str));
        console.appendChild(p);
    }

    /**
     * 自己发送信息
     */
    if (messageData.code == 21 || messageData.code == 41) {
        createPElement(console, messageData, 1);
    }

    /**
     * 接受其他人发送信息
     */
    if (messageData.code == 20) {
        createPElement(console, messageData, 0);
    }

    /**
     * 接受其群发发送信息
     */
    if (messageData.code == 40) {
        createPElement(console, messageData, 3);
    }
    /**
     * 发送失败消息
     */
    if (messageData.code == 30) {
        createPElement(console, messageData, 2);
    }
    while (console.childNodes.length > 30) {
        console.removeChild(console.firstChild);
    }
    console.scrollTop = console.scrollHeight;

}


/**
 * 创建新元素
 * @param objet
 * @param messageData
 * @param type  0 正常  1 用户自身 2 发送失败  3 群发信息
 */
function createPElement(objet, messageData, type) {
    var pTime = document.createElement('p');
    pTime.style.wordWrap = 'break-word';
    pTime.style.textAlign = "center";
    pTime.style.fontSize = "12px";
    var str = "[ " + getNowFormatDate() + " ]";
    pTime.appendChild(document.createTextNode(str));
    objet.appendChild(pTime);

    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    if (type == 1) {
        p.style.textAlign = "right";
        p.style.color = "blue";
    }

    /**
     * 群发设置颜色
     */
    if (type == 3) {
        p.style.color = "#e9220b";
    }

    var str = messageData.message;
    var img = document.createElement("img");
    if (type == 1) {
        img.src = "/images/1.jpg";
        p.appendChild(document.createTextNode(str));
        p.appendChild(img);
    } else if (type == 0) {
        img.src = "/images/2.jpg";
        p.appendChild(img);
        p.appendChild(document.createTextNode(str));
    } else if (type == 3) {
        img.src = "/images/3.jpg";
        p.appendChild(img);
        p.appendChild(document.createTextNode(str));
    } else {
        p.appendChild(document.createTextNode(str));
    }
    objet.appendChild(p);
}


/**
 * 获取当前时间
 * @returns {string}
 */
function getNowFormatDate() {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
        + " " + date.getHours() + seperator2 + date.getMinutes()
        + seperator2 + date.getSeconds();
    return currentdate;
}