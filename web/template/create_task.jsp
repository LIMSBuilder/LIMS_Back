<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="com.zhuozhengsoft.pageoffice.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.WordDocument" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.DataRegion" %>
<%@ page import="com.lims.model.Task" %>
<%@ page import="com.lims.model.User" %>
<%@ taglib uri="http://java.pageoffice.cn" prefix="po" %>
<%
    Task task = (Task) request.getAttribute("task");
    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();
    if (task != null) {
        doc.openDataRegion("PO_identify").setValue(task.getStr("identify"));
        doc.openDataRegion("PO_client_unit").setValue(task.getStr("client_unit"));
        doc.openDataRegion("PO_name").setValue(task.getStr("name"));
        doc.openDataRegion("PO_aim").setValue(task.getStr("aim"));
        doc.openDataRegion("PO_address").setValue(task.getStr("client_address"));
        doc.openDataRegion("PO_code").setValue(task.getStr("client_code"));
        doc.openDataRegion("PO_client").setValue(task.getStr("client"));
        doc.openDataRegion("PO_tel").setValue(task.getStr("client_tel"));
        doc.openDataRegion("PO_creater").setValue(User.userDao.findById(task.get("creater")).getStr("name"));
        doc.openDataRegion("PO_create_time").setValue(task.getStr("create_time"));
        doc.openDataRegion("PO_way0").setValue(((Integer) task.get("way")) == 1 ? "是" : "否");
        doc.openDataRegion("PO_way1").setValue(((Integer) task.get("way")) == 0 ? "是" : "否");
        doc.openDataRegion("PO_way1_text").setValue(task.getStr("wayDesp"));
        doc.openDataRegion("PO_charge").setValue(User.userDao.findById((Integer) task.get("charge")).getStr("name"));


    }
    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/taskTemplate.doc", OpenModeType.docNormalEdit, "张三");
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
