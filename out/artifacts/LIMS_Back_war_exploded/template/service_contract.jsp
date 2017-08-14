<%--
  Created by IntelliJ IDEA.
  User: chenyangyang
  Date: 2017/8/13
  Time: 上午9:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="com.zhuozhengsoft.pageoffice.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.WordDocument" %>
<%@ page import="com.lims.model.*" %>
<%@ taglib uri="http://java.pageoffice.cn" prefix="po" %>
<%
    ServiceContract serviceContract = (ServiceContract) request.getAttribute("serviceContract");
    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();

    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen(serviceContract.getStr("path"), OpenModeType.docNormalEdit, "张三");
    poCtrl1.setTagId("PageOfficeCtrl1"); //此行必须

%>

<html>
<head>
    <title>Title</title>
</head>
<body>
<po:PageOfficeCtrl id="PageOfficeCtrl1"/>
</body>
</html>