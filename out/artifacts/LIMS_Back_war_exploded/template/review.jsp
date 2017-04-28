<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page language="java" import="com.zhuozhengsoft.pageoffice.*" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.WordDocument" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.DataRegion" %>
<%@ page import="com.zhuozhengsoft.pageoffice.wordwriter.Table" %>
<%@ page import="java.util.List" %>
<%@ page import="com.lims.model.*" %>
<%@ taglib uri="http://java.pageoffice.cn" prefix="po" %>
<%
    ContractReview contractReview = (ContractReview) request.getAttribute("contractReview");
    PageOfficeCtrl poCtrl1 = new PageOfficeCtrl(request);
    WordDocument doc = new WordDocument();
    if (contractReview != null) {
        Contract contract = Contract.contractDao.findById(contractReview.get("contract_id"));
        doc.openDataRegion("PO_identify").setValue(contract.getStr("identify"));
        doc.openDataRegion("PO_client_unit").setValue(contract.getStr("client_unit"));
        doc.openDataRegion("PO_trustee_unit").setValue(contract.getStr("trustee_unit"));
        doc.openDataRegion("PO_monitorContent").setValue(contract.getStr("name"));
        doc.openDataRegion("PO_same").setValue(((Integer) contractReview.get("same")) == 1 ? "是" : "否");
        doc.openDataRegion("PO_contract").setValue(((Integer) contractReview.get("contract")) == 1 ? "是" : "否");
        doc.openDataRegion("PO_guest").setValue(((Integer) contractReview.get("guest")) == 1 ? "是" : "否");
        doc.openDataRegion("PO_package").setValue(((Integer) contractReview.get("package")) == 1 ? "是" : "否");
        doc.openDataRegion("PO_company").setValue(((Integer) contractReview.get("company")) == 1 ? "合格" : "不合格");
        doc.openDataRegion("PO_money").setValue(((Integer) contractReview.get("money")) == 1 ? "是" : "否");
        doc.openDataRegion("PO_time").setValue(((Integer) contractReview.get("time")) == 1 ? "合适" : "不合适");
        if(contractReview.getStr("reject_msg")!=null){
            doc.openDataRegion("PO_other").setValue(contractReview.getStr("reject_msg"));
        }

        doc.openDataRegion("PO_result").setValue(((Integer) contractReview.get("result")) == 1 ? "审核通过" : "审核不通过");

    }

    poCtrl1.setWriter(doc);
    poCtrl1.setServerPage(request.getContextPath() + "/poserver.zz");
    poCtrl1.setSaveFilePage("savefile.jsp");//如要保存文件，此行必须
    //打开文件
    poCtrl1.webOpen("/doc/reviewTemplate.docx", OpenModeType.docNormalEdit, "张三");
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
