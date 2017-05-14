<%--
  Created by IntelliJ IDEA.
  User: chenyangyang
  Date: 2017/5/14
  Time: 上午9:53
  To change this template use File | Settings | File Templates.
--%>
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
        doc.openDataRegion("PO_name").setValue(task.getStr("name"));

        DataRegion dataRegion = doc.openDataRegion("PO_Table");
        Table table = dataRegion.openTable(1);
        List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p\n" +
                "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id");
        for (int i = 0; i < itemProjectList.size(); i++) {
            MonitorProject monitorProject = MonitorProject.monitorProjectdao.findById(itemProjectList.get(i).get("project_id"));
            table.openCellRC(3 + i, 1).setValue(i + 1 + "");
            table.openCellRC(3 + i, 2).setValue(monitorProject.getStr("name"));
            int count = ItemProject.itemprojectDao.find("select * from `db_item_project` where project_id =" + itemProjectList.get(i).get("project_id")).size();
            table.openCellRC(3 + i, 3).setValue(count + "");
            int balance = Sample.sampleDao.find("SELECT s.* FROM `db_sample` s ,`db_sample_project` p WHERE p.sample_id = s.id AND p.item_project_id=" + itemProjectList.get(i).get("id") + " AND s.balance != null").size();
            table.openCellRC(3 + i, 4).setValue(balance + "");
            int labCount = Lib.libDao.find("select * from `db_lib` where item_project_id =" + itemProjectList.get(i).get("id")).size();
            table.openCellRC(3 + i, 5).setValue(labCount + "");
            List<Lib> libList = Lib.libDao.find("select s.* from `db_sample` s,`db_lib` p where p.item_project_id =" + itemProjectList.get(i).get("id") + " and s.id=p.sample_id");
            String temp = "";
            for (Lib lib : libList) {
                temp += lib.getStr("identify") + " ";
            }
            table.openCellRC(3 + i, 6).setValue(temp);
            int tagCount = Tag.tagDao.find("select * from `db_tag` where item_project_id =" + itemProjectList.get(i).get("id")).size();
            table.openCellRC(3 + i, 7).setValue(tagCount + "");
            String tags = "";
            List<Tag> tagList = Tag.tagDao.find("select s.* from `db_sample` s,`db_tag` p where p.item_project_id =" + itemProjectList.get(i).get("id") + " and s.id=p.sample_id");
            for (Tag tag : tagList) {
                temp += tag.getStr("identify") + " ";
            }
            table.openCellRC(3 + i, 8).setValue(tags);
            Blind blind = Blind.blindDao.findFirst("select * from `db_blind` where item_project_id =" +itemProjectList.get(i).get("id"));
            table.openCellRC(3 + i, 9).setValue(blind == null ? "" : blind.get("blind").toString());
          //  table.openCellRC(3 + i, 9).setValue(blind.get("blind") == null ? "" : blind.get("blind").toString());
        }

    }
    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/QualityTemplate.docx", OpenModeType.docNormalEdit, "张三");
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
