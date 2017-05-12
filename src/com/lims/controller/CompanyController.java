package com.lims.controller;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.ExcelRead;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by chenyangyang on 2017/5/1.
 */
public class CompanyController extends Controller {
    /**
     * 删除公司
     **/
    public void deleteCompany() {
        try {
            int id = getParaToInt("id");
            Boolean result = Company.companydao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 删除一条监测项目记录
     **/
    public void deleteItem() {
        try {
            int id = getParaToInt("id");
            Boolean result = Contractitem.contractitemdao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 返回公司列表
     **/
    public void compangList() {
        try {
            int id = getParaToInt("id");
            List<Company> companyList = Company.companydao.find("select * from `db_company` where id= " + id);
            List<Map> result = new ArrayList<>();
            for (Company company : companyList) {
                Map temp = new HashMap();
                temp.put("id", company.get("id"));
                temp.put("name", company.get("name"));
                result.add(temp);
            }
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 增加监测项目记录
     **/
    public void addItem() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int company_id = getParaToInt("id");
                    int element = getParaToInt("element");
                    int point = getParaToInt("point");
                    int frequency = getParaToInt("frequency");
                    String other = getPara("other");
                    Boolean result = true;
                    if (Contractitem.contractitemdao.find("select * from `db_item` where company_id='" + company_id + "'and  point='" + point + "' and element='" + element + "' and other= '" + other + "' and frequency ='" + frequency + "'").size() != 0) {
                        renderJson(RenderUtils.CODE_REPEAT);
                    } else {
                        Contractitem contractitem = new Contractitem();
                        contractitem.set("company_id", company_id).set("point", point).set("element", element).set("other", other).set("frequency", frequency);
                        result = result && contractitem.save();
                        Integer[] projectList = getParaValuesToInt("project[]");
                        for (int id : projectList) {
                            ItemProject itemProject = new ItemProject();
                            itemProject.set("item_id", contractitem.getInt("id"))
                                    .set("project_id", id);
                            result = result && itemProject.save();
                            if (!result) return false;
                        }


                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 新增一家公司  EXCEL
     **/
    public void addCompany() {
        try {
            int contract_id = getParaToInt("id");
            String path = getPara("path");
            ExcelRead read = new ExcelRead();
            String[] titles = {"id", "company", "element", "pointList", "projectList", "frequency"};
            List<Map> reading = read.readExcel(path, titles);
            Boolean result = true;
            for (Map temp : reading) {
                String companyStr = temp.get("company").toString();
                String elementStr = temp.get("element").toString();
                String frequencyStr = temp.get("frequency").toString();
                Element element = Element.elementDao.findFirst("SELECT * FROM `db_element` WHERE name='" + elementStr + "'");
                Frequency frequency = Frequency.frequencyDao.findFirst("SELECT * FROM `db_frequency` WHERE total='" + frequencyStr + "'");
                String[] projectList = temp.get("projectList").toString().split(" ");
                String[] pointList = temp.get("pointList").toString().split(" ");
                Company company = new Company();

                int companyList = Company.companydao.find("select * from `db_company` where company = '" + companyStr + "'And contract_id =" + contract_id).size();
                Contractitem contractitem = new Contractitem();
                if (companyList != 0) {
                    if (company.get("process") != 0) {
                        company.set("process", 0);
                        result = result && company.update();
                    }
                    contractitem.set("company_id", company.getInt("id")).set("point", pointList.length)
                            .set("other", companyStr).set("element", element.getInt("id")).set("frequency", frequency.get("id"));
                    result = result && contractitem.save();
                    for (String projectName : projectList) {
                        MonitorProject project = MonitorProject.monitorProjectdao
                                .findFirst("SELECT * FROM `db_monitor_project` WHERE name='" + projectName + "'");
                        if (project != null) {
                            Integer[] project1 = getParaValuesToInt("project[]");
                            for (Integer pro : project1) {
                                ItemProject itemProject = new ItemProject();
                                result = result && itemProject.set("project_id", pro)
                                        .set("item_id", contractitem.get("id")).set("isPackage", itemProject.get("isPackage")).save();
                            }
                        }
                    }

                } else {
                    company.set("company", companyStr).set("contract_id", contract_id).set("flag", 1)
                            .set("creater", ParaUtils.getCurrentUser(getRequest()).getInt("id"))
                            .set("create_time", ParaUtils.sdf2.format(new Date())).set("process", 0);
                    result = result && company.save();
                    contractitem.set("company_id", company.getInt("id")).set("point", pointList.length).set("other", companyStr).set("element", element.getInt("id")).set("frequency", frequency.getInt("id"));
                    result = result && contractitem.save();
                    for (String projectName : projectList) {
                        MonitorProject project = MonitorProject.monitorProjectdao.findFirst("SELECT * FROM `db_monitor_project` WHERE name='" + projectName + "'");
                        if (project != null) {
                            ItemProject itemProject = new ItemProject();
                            result = result && itemProject.set("project_id", project.getInt("id")).set("item_id", contractitem.get("id")).set("isPackage", itemProject.get("isPackage")).save();

                        }
                    }
                }

            }

            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 修改监测项目
     * **/

    public void changeItem() {
        try {
            int item_id = getParaToInt("id");
            Contractitem contractitem = Contractitem.contractitemdao.findById(item_id);
            Integer[] project = getParaValuesToInt("project[]");
            boolean result = true;
            if (contractitem != null) {
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_item` i,`db_item_project`p WHERE id =" + item_id + "And p.item_id=i.id");
                Integer[] list = (Integer[]) itemProjectList.toArray();

            }


        } catch (Exception e) {
            renderError(500);
        }
    }
}