function check() {

    var uid = $("#uid");
    var name = $("#userName");

    if(uid.val() == "" || name == ""){
        alert("请输入输入用户ID 和 用户名");
        return false;
    }
    return true;
}