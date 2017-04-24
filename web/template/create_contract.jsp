<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="com.zhuozhengsoft.pageoffice.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.WordDocument" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.DataRegion" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.Table" %>
<%@ page import="java.util.List" %>
<%@ page import="com.lims.model.*" %>
<%@ taglib uri="http://java.pageoffice.cn" prefix="po" %>
<%
    Contract contract = (Contract) request.getAttribute("contract");
    //List<MonitorProject> projectList = MonitorProject.monitorProjectdao.find("select m.* from `db_company`c ,`db_item`i,`db_item_project` p,`db_monitor_project` m  where c.contract_id = " + contract.get("id") + "AND i.company_id =c.id AND p.item_id = i.id AND p.isPackage=1 AND p.project_id=m.id" );
    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();
    if (contract != null) {
        doc.openDataRegion("PO_identify").setValue(contract.getStr("identify"));
        doc.openDataRegion("PO_client_unit").setValue(contract.getStr("client_unit"));
        doc.openDataRegion("PO_client_code").setValue(contract.getStr("client_code"));
        doc.openDataRegion("PO_client_address").setValue(contract.getStr("client_address"));
        doc.openDataRegion("PO_client_tel").setValue(contract.getStr("client_tel"));
        doc.openDataRegion("PO_client").setValue(contract.getStr("client"));
        doc.openDataRegion("PO_client_fax").setValue(contract.getStr("client_fax"));
        doc.openDataRegion("PO_project_name").setValue(contract.getStr("name"));
        doc.openDataRegion("PO_aim").setValue(contract.getStr("aim"));
        doc.openDataRegion("PO_monitorWay").setValue(Type.typeDao.findById((Integer) contract.get("type")).getStr("name"));
        DataRegion dataRegion = doc.openDataRegion("PO_Table");
        Table table = dataRegion.openTable(7);
        List<Company> companyList = Company.companydao.find("select * from `db_company` where contract_id =" + contract.get("id"));
        for (int i = 0; i < companyList.size(); i++) {
            Company company = companyList.get(i);
            List<Contractitem> contractitemList = Contractitem.contractitemdao.find("select * from `db_item` where company_id =" + company.get("id"));
            for (int j = 0; j < contractitemList.size(); j++) {
                Contractitem contractitem = contractitemList.get(j);
                table.openCellRC(8 + j, 2).setValue(Element.elementDao.findById((Integer) contractitem.get("element")).getStr("name"));
                table.openCellRC(8 + j, 3).setValue((contractitem.get("point")).toString());
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("select * from `db_item_project` where item_id=" + contractitem.get("id"));
                String temp = "";
                for (ItemProject itemProject : itemProjectList) {
                    MonitorProject project = MonitorProject.monitorProjectdao.findById(itemProject.get("project_id"));
                    temp += project.getStr("name") + " ";
                }
                table.openCellRC(8 + j, 4).setValue(temp);
                table.openCellRC(8 + j, 5).setValue(Frequency.frequencyDao.findById((Integer)contractitem.get("frequency")).getStr("total"));
                table.openCellRC(8 + j, 6).setValue(contractitem.getStr("other"));
                table.insertRowAfter(table.openCellRC(8 + j, 1));

            }
        }
        doc.openDataRegion("PO_way0").setValue(((Integer) contract.get("way")) == 1 ? "是" : "否");
        doc.openDataRegion("PO_way1").setValue(((Integer) contract.get("way")) == 0 ? "是" : "否");
        doc.openDataRegion("PO_package_unit").setValue(contract.getStr("package_unit"));
//        doc.openDataRegion("po_package").setValue();//分包项目
        doc.openDataRegion("PO_paymentWay").setValue(contract.getStr("paymentWay"));
        doc.openDataRegion("PO_finish_time").setValue(contract.getStr("finish_time"));
       doc.openDataRegion("PO_money").setValue(contract.getStr("payment"));
        doc.openDataRegion("PO_way2").setValue(((Integer) contract.get("in_room"))==1 ?"是":"否");
        doc.openDataRegion("PO_way3").setValue(((Integer) contract.get("secret"))==1 ?"是":"否");
        doc.openDataRegion("PO_Other").setValue(contract.getStr("other"));
        doc.openDataRegion("PO_trustee_unit").setValue(contract.getStr("trustee_unit"));
        doc.openDataRegion("PO_trustee_code").setValue(contract.getStr("trustee_code"));
        doc.openDataRegion("PO_trustee_address").setValue(contract.getStr("trustee_address"));
        doc.openDataRegion("PO_trustee_tel").setValue(contract.getStr("trustee_tel"));
        doc.openDataRegion("PO_trustee").setValue(User.userDao.findById((Integer) contract.get("creater")).getStr("name"));
        doc.openDataRegion("PO_trustee_fax").setValue(contract.getStr("trustee_fax"));


    }

    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/contractTemplate.doc", OpenModeType.docNormalEdit, "张三");
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
