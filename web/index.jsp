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
    <title>$Title$</title>
    <script type="text/javascript" src="comet4j.js"></script>
    <script type="text/javascript">
        function init(){

            var number1 = document.getElementById('number1');
            var number2 = document.getElementById('number2');
            // 建立连接，push 即web.xml中 CometServlet的<url-pattern>
            JS.Engine.start('push');
            // 监听后台某个频道
            JS.Engine.on(
                {
                    // 对应服务端 “频道1” 的值 channel1
                    channel1 : function(num1){
                        number1.innerHTML = num1;
                    },
                    // 对应服务端 “频道2” 的值 channel2
                    channel2 : function(num2){
                        number2.innerHTML = num2;
                    },
                }
            );
        }
    </script>
  </head>
  <body onload="init()">
  数字1：<span id="number1">...</span><br></br>
  数字2：<span id="number2">...</span>
  </body>
</html>
