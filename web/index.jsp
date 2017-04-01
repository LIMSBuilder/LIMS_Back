<%--
  Created by IntelliJ IDEA.
  User: qulongjun
  Date: 2017/2/23
  Time: 下午5:22
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <script type="text/javascript" src="ajax-pushlet-client.js"></script>
    <script type="text/javascript">
        PL._init();
        //PL.setDebug(true);
        PL.joinListen('/cuige/he');
        function onData(event) {
            console.log(event.get("mess"));
//            alert(event.get("mess"));
            // 离开
            // PL.leave();
        }
    </script>
</head>
<body>
<center>
    <h1>
        my first pushlet 2!
    </h1>
</center>
</body>
</html>
