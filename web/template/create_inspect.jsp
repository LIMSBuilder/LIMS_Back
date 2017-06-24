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
    Inspect inspect = (Inspect) request.getAttribute("inspect");


    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();
    if (inspect != null) {
        ItemProject itemProject = ItemProject.itemprojectDao.findById(inspect.get("item_project_id"));
        if (itemProject != null) {
            doc.openDataRegion("PO_name").setValue(MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).getStr("name"));
            Task task = Task.taskDao.findFirst("SELECT * FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t WHERE p.id=" + itemProject.get("id") + " AND p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id");
            if (task != null) {
                doc.openDataRegion("PO_identify").setValue(task.getStr("identify"));
            }
        }
        doc.openDataRegion("PO_sample_time").setValue(inspect.getStr("sample_time"));
        doc.openDataRegion("PO_send_time").setValue(inspect.getStr("send_time"));
        doc.openDataRegion("PO_sender").setValue(User.userDao.findById(inspect.getInt("sender")).getStr("name"));

        DataRegion dataRegion = doc.openDataRegion("PO_Table");
        Table table = dataRegion.openTable(1);
        String type = inspect.getStr("type");
        if (type.equals("water")) {
            List<InspectWater> waterList = InspectWater.inspectWaterDao.find("SELECT * FROM `db_inspect_water` WHERE inspect_id=" + inspect.get("id"));
            for (int i = 0; i < waterList.size(); i++) {
                table.openCellRC(2 + i, 1).setValue(Sample.sampleDao.findById(waterList.get(i).get("sample_id")).getStr("identify"));
                table.openCellRC(2 + i, 2).setValue(Element.elementDao.findById(MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("element_id")).getStr("name"));
                table.insertRowAfter(table.openCellRC(2 + i, 1));
            }
        }

        if (type.equals("soil")) {
            List<InspectSoil> soilList = InspectSoil.inspectSoilDao.find("SELECT * FROM `db_inspect_soil` WHERE inspect_id=" + inspect.get("id"));
            for (int i = 0; i < soilList.size(); i++) {
                table.openCellRC(2 + i, 1).setValue(Sample.sampleDao.findById(soilList.get(i).get("sample_id")).getStr("identify"));
                table.openCellRC(2 + i, 2).setValue(Element.elementDao.findById(MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("element_id")).getStr("name"));
                table.insertRowAfter(table.openCellRC(2 + i, 1));
            }
        }

        if (type.equals("solid")) {
            List<InspectSoild> soildList = InspectSoild.inspectSoildDao.find("SELECT * FROM `db_inspect_solid` WHERE inspect_id=" + inspect.get("id"));
            for (int i = 0; i < soildList.size(); i++) {
                table.openCellRC(2 + i, 1).setValue(Sample.sampleDao.findById(soildList.get(i).get("sample_id")).getStr("identify"));
                table.openCellRC(2 + i, 2).setValue(Element.elementDao.findById(MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("element_id")).getStr("name"));
                table.insertRowAfter(table.openCellRC(2 + i, 1));
            }
        }

        if (type.equals("air")) {
            List<InspectAir> airList = InspectAir.inspectAir.find("SELECT * FROM `db_inspect_air` WHERE inspect_id=" + inspect.get("id"));
            for (int i = 0; i < airList.size(); i++) {
                table.openCellRC(2 + i, 1).setValue(Sample.sampleDao.findById(airList.get(i).get("sample_id")).getStr("identify"));
                table.openCellRC(2 + i, 2).setValue(Element.elementDao.findById(MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("element_id")).getStr("name"));
                table.insertRowAfter(table.openCellRC(2 + i, 1));
            }
        }

        if (type.equals("dysodia")) {
            List<InspectDysodia> dysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT * FROM `db_inspect_dysodia` WHERE inspect_id=" + inspect.get("id"));
            for (int i = 0; i < dysodiaList.size(); i++) {
                table.openCellRC(2 + i, 1).setValue(Sample.sampleDao.findById(dysodiaList.get(i).get("sample_id")).getStr("identify"));
//                table.openCellRC(2 + i, 2).setValue(Element.elementDao.findById(MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("element_id")).getStr("name"));
                table.insertRowAfter(table.openCellRC(2 + i, 1));
            }
        }
    }
    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/"+inspect.get("type")+".doc", OpenModeType.docNormalEdit, "张三");
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
