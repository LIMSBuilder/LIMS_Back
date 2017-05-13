<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="com.zhuozhengsoft.pageoffice.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.WordDocument" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.DataRegion" %>
<%@ page import="com.lims.utils.ParaUtils" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="com.lims.model.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.Table" %>
<%@ taglib uri="http://java.pageoffice.cn" prefix="po" %>
<%
    Task task = (Task) request.getAttribute("task");


    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();
    if (task != null) {
        doc.openDataRegion("PO_identify").setValue(task.getStr("identify"));
        doc.openDataRegion("PO_client_unit").setValue(task.getStr("client_unit"));
        doc.openDataRegion("PO_type").setValue(Type.typeDao.findById(task.getInt("type")).getStr("name"));
        doc.openDataRegion("PO_sample_sendTime").setValue(ParaUtils.sdf2.format(new Date()));
        doc.openDataRegion("PO_sample_sender").setValue(ParaUtils.getCurrentUser(request).getStr("name"));

        List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_task` t,`db_company` c,`db_sample` s \n" +
                "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND s.company_id=c.id ORDER BY s.identify");
        if (sampleList.size() != 0) {
            doc.openDataRegion("PO_sample_identify").setValue(sampleList.get(0).getStr("identify") + "~" + sampleList.get(sampleList.size() - 1).getStr("identify"));
        }
        doc.openDataRegion("PO_sample_count").setValue(sampleList.size() + "");

        DataRegion dataRegion = doc.openDataRegion("PO_Table");
        Table table = dataRegion.openTable(1);

        List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p\n" +
                "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id");

        for (int i = 0; i < itemProjectList.size(); i++) {
            MonitorProject monitorProject = MonitorProject.monitorProjectdao.findById(itemProjectList.get(i).get("project_id"));
            table.openCellRC(6 + i, 1).setValue(i + 1 + "");
            table.openCellRC(6 + i, 2).setValue(Element.elementDao.findById(monitorProject.get("element_id")).getStr("name"));
            table.openCellRC(6 + i, 3).setValue(monitorProject.getStr("name"));
            int count = Sample.sampleDao.find("SELECT s.* FROM `db_sample` s ,`db_sample_project` p WHERE p.sample_id = s.id AND p.item_project_id=" + itemProjectList.get(i).get("id")).size();
            table.openCellRC(6 + i, 4).setValue(count + "");
            table.insertRowAfter(table.openCellRC(6 + i, 1));
        }
    }


    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/delivery.doc", OpenModeType.docNormalEdit, "张三");
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
