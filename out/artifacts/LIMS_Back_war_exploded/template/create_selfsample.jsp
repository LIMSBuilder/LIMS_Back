<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="com.zhuozhengsoft.pageoffice.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.WordDocument" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.DataRegion" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.Table" %>
<%@ page import="java.util.List" %>
<%@ page import="com.lims.model.*" %>
<%@ taglib uri="http://java.pageoffice.cn" prefix="po" %>
<%
    Task task = (Task) request.getAttribute("task");
    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();
    if (task != null) {
        List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_sample` WHERE task_id=" + task.get("id"));
        DataRegion dataRegion = doc.openDataRegion("PO_Table");
        Table table = dataRegion.openTable(1);
        for (int i = 0; i < sampleList.size(); i++) {
            Sample sample = sampleList.get(i);
            table.openCellRC(2 + i, 1).setValue(i + 1 + "");
            table.openCellRC(2 + i, 2).setValue(sample.getStr("name"));
            table.openCellRC(2 + i, 3).setValue(sample.getStr("identify"));
            List<SampleProject> sampleProjectList = SampleProject.sampleprojrctDao.find("SELECT * FROM `db_sample_project` WHERE sample_id=" + sample.get("id"));
            String temp = "";
            for (SampleProject sampleProject : sampleProjectList) {
                MonitorProject project = MonitorProject.monitorProjectdao.findById(sampleProject.get("project_id"));
                temp += project.getStr("name") + " ";
            }
            table.openCellRC(2 + i, 4).setValue(temp);
            table.openCellRC(2 + i, 5).setValue(sample.getStr("character"));
            table.openCellRC(2 + i, 6).setValue(sample.get("condition").toString());
            table.insertRowAfter(table.openCellRC(2 + i, 1));
        }
        doc.openDataRegion("PO_Sample_Client").setValue(task.getStr("client_unit"));
//        doc.openDataRegion("PO_Client").setValue(task.getStr("client_unit"));

    }
    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/selfSample.docx", OpenModeType.docNormalEdit, "张三");
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
