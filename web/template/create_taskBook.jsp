<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="com.zhuozhengsoft.pageoffice.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.WordDocument" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.DataRegion" %>
<%@ page import="com.lims.model.Task" %>
<%@ page import="com.lims.model.User" %>
<%@ page import="com.lims.model.Type" %>
<%@ taglib uri="http://java.pageoffice.cn" prefix="po" %>
<%
    Task task = (Task) request.getAttribute("task");
    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();
    if (task != null) {
        doc.openDataRegion("PO_identify").setValue(task.getStr("identify"));
        doc.openDataRegion("PO_client_unit").setValue(task.getStr("client_unit"));
        doc.openDataRegion("PO_type").setValue(Type.typeDao.findById((Integer) task.get("type")).getStr("name"));
//样品编号        doc.openDataRegion("PO_sampleIdentify")
// 送达样品日期
// 送样总件数


    }
    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/TaskhandoverTemplate.doc", OpenModeType.docNormalEdit, "张三");
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
