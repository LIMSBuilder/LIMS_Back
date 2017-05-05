package com.lims.controller;

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
     * 删除一个项目
     **/
    public void deleteProject() {
        try {
            int id = getParaToInt("id");
            boolean result = ItemProject.itemprojectDao.deleteById(id);
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
     * 修改公司名称
     **/
    public void changeCompany() {
        try {
            int id = getParaToInt("id");
            Boolean result = true;
            String name = getPara("name");
            Company company = Company.companydao.findById(id);
            if (company != null) {
                result = result && company.set("name", name).update();
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
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
                    int id = getParaToInt("id");
                    String[] items = getParaValues("project_items[]");
                    Boolean result = true;

                    for (String item : items) {
                        Map temp = Jackson.getJson().parse(item, Map.class);
                        List<Map> projectItems = (List<Map>) temp.get("items");
                        for (Map itemMap : projectItems) {
                            Contractitem contractitem = new Contractitem();
                            result = result && contractitem.set("company_id", id).set("element", ((Map) itemMap.get("element")).get("id")).set("frequency", ((Map) itemMap.get("frequency")).get("id")).set("point", itemMap.get("point")).set("other", itemMap.get("other")).save();
                            List<Map> project = (List<Map>) itemMap.get("project");
                            for (Map pro : project) {
                                ItemProject itemProject = new ItemProject();
                                result = result && itemProject.set("project_id", pro.get("id")).set("item_id", contractitem.get("id")).set("isPackage", itemProject.get("isPackage")).save();
                                if (!result) break;
                            }
                            if (!result) break;
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
     * 新增一个分析项目
     **/
    public void addProject() {
        try {
            boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    Integer[] project = getParaValuesToInt("project[]");
                    boolean result = true;
                    for (Integer pro : project) {

                        ItemProject itemProject = new ItemProject();
                        result = result && itemProject.set("project_id", pro).set("item_id", id).set("isPackage", itemProject.get("isPackage")).save();
                        if (!result) break;
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
            int flag = getParaToInt("flag");
            String path = getPara("path");
            ExcelRead read = new ExcelRead();
            String[] titles = {"id", "company", "element", "pointList", "projectList", "frequency"};
            List<Map> reading = read.readExcel(path, titles);
            List returnBack = new ArrayList();
            Map<String, List> obj = new HashMap<>();
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
                if (getParaToInt("flag") == 1) {
                    List<Company> companyList = Company.companydao.find("select * from `db_company` where company = " + companyStr + "And task_id =" + getPara("task_id"));
                    if (companyList != null) {

                        company.set("company", companyStr).set("task_id", getPara("task_id")).set("flag", 1)
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).getInt("id"))
                                .set("create_time", ParaUtils.sdf2.format(new Date())).set("process", 0)
                                .save();
                        result = result && company.save();
                    } else {
                        company.set("company", companyStr).set("task_id", getPara("task_id")).set("flag", 1)
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).getInt("id"))
                                .set("create_time", ParaUtils.sdf2.format(new Date())).set("process", 0)
                                .save();
                        result = result && company.save();
                    }

                } else if (getParaToInt("flag") == 0) {
                    List<Company> companyList = Company.companydao.find("select * from `db_company` where company = " + companyStr + "And contract_id =" + getPara("contract_id"));
                    if (companyList != null) {
                        company.set("company", companyStr).set("contract_id", getPara("contract_id")).set("flag", 0)
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).getInt("id"))
                                .set("create_time", ParaUtils.sdf2.format(new Date())).set("process", 0)
                                .save();
                        result = result && company.save();
                    } else {
                        company.set("company", companyStr).set("contract_id", getPara("contract_id")).set("flag", 0)
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).getInt("id"))
                                .set("create_time", ParaUtils.sdf2.format(new Date())).set("process", 0)
                                .save();
                    }

                }
            }
        } catch (Exception e) {
            renderError(500);
        }

    }
}
