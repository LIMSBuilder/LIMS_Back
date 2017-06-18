package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.*;

/**
 * Created by chenyangyang on 2017/6/16.
 */
public class ReportController extends Controller {
    /**
     * 显示公司
     **/
    public void company() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                Map total = new HashMap();
                List<Company> companyList = Company.companydao.find("SELECT * FROM `db_company` WHERE task_id=" + task_id);
                List<Map> result = new ArrayList<>();
                for (Company company : companyList) {
                    Map temp = new HashMap();
                    temp.put("company", company.toSimpleJSON());
                    result.add(temp);
                }
                total.put("item", result);
                renderJson(total);
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据company_id 找到合同信息
     **/
    public void contract() {
        try {
            int company_id = getParaToInt("company_id");
            Company company = Company.companydao.findById(company_id);
            Map total = new HashMap();
            if (company != null) {
                Contract contract = Contract.contractDao.findFirst("SELECT c.*  FROM `db_contract` c,`db_task` t,`db_company` p WHERE p.id=" + company_id + " AND t.id=p.task_id\n" +
                        "AND c.id=t.contract_id ");

                if (contract != null) {
                    total.put("items", contract.Json());
                } else {
                    ServiceContract serviceContract = ServiceContract.serviceContractDao.findFirst("SELECT c.*  FROM `db_service_contract` c,`db_task` t,`db_company` p WHERE p.id=" + company_id + " AND t.id=p.task_id\n" +
                            "AND c.id=t.service_id");
                    if (serviceContract != null) {
                        total.put("items", serviceContract.Json());
                    } else {
                        renderNull();
                    }
                }
                renderJson(total);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据company_id找到task
     **/
    public void task() {
        try {
            int conmpany_id = getParaToInt("company_id");
            Company company = Company.companydao.findById(conmpany_id);
            Map total = new HashMap();
            if (company != null) {
                Task task = Task.taskDao.findFirst("SELECT t.*  FROM `db_company` c,`db_task` t WHERE c.id=" + conmpany_id + " AND t.id=c.task_id");
                total.put("items", task.toJsonSingle());
            } else {
                renderNull();
            }
            renderJson(total);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据company_id 找到所有的样品信息
     **/
    public void sample() {
        try {
            int conmpany_id = getParaToInt("company_id");
            Company company = Company.companydao.findById(conmpany_id);
            Map total = new HashMap();
            if (company != null) {
                List<Sample> sampleList = Sample.sampleDao.find("SELECT *FROM `db_sample` where company_id =" + conmpany_id);
                List<Map> reult = new ArrayList<>();
                for (Sample sample : sampleList) {
                    reult.add(sample.toSimpleJson());
                }
                total.put("items", reult);
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据company_id找到所有的交接单
     **/
    public void receipt() {
        try {
            int conmpany_id = getParaToInt("company_id");
            Company company = Company.companydao.findById(conmpany_id);
            Map total = new HashMap();
            if (company != null) {
                List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_sample` WHERE company_id=" + conmpany_id + " ORDER BY s.identify");
                total.put("count", sampleList.size());
                if (sampleList.size() != 0) {
                    String first = sampleList.get(0).getStr("identify");
                    String last = sampleList.get(sampleList.size() - 1).getStr("identify");
                    total.put("firstIdentify", first);
                    total.put("lastIdentify", last);
                }
                Map<List, List> back = new HashMap<>();
                for (Sample sample : sampleList) {
                    List<MonitorProject> monitorProjectList = MonitorProject.monitorProjectdao.find("SELECT m.* FROM `db_sample` s,`db_sample_project` p,`db_item_project` i,`db_monitor_project` m\n" +
                            "WHERE s.id=" + sample.get("id") + " AND p.sample_id=s.id AND p.item_project_id=i.id AND i.project_id=m.id");
                    List<Map> b = new ArrayList<>();
                    for (MonitorProject monitorProject : monitorProjectList) {
                        b.add(monitorProject.toJsonSingle());
                    }
                    if (back.containsKey(b)) {
                        //当前已经存在这分析项目的组合
                        back.get(b).add(sample);
                    } else {
                        List t = new ArrayList();
                        t.add(sample);
                        back.put(b, t);
                    }
                }
                List<Map> mapList = new ArrayList<>();
                for (List s : back.keySet()) {
                    Map m = new HashMap();
                    m.put("projects", s);
                    m.put("samples", back.get(s));
                    List<Map> de = new ArrayList();
                    for (int i = 0; i < back.get(s).size(); i++) {
                        List<Description> descriptionList = Description.descriptionDao.find("SELECT * FROM `db_sample_deseription` WHERE sample_id =" + ((Sample) (back.get(s).get(i))).get("id"));
                        for (Description description : descriptionList) {
                            de.add(description.toJsonSingle());
                        }

                    }
                    m.put("item", de);

                    mapList.add(m);

                }
                total.put("items", mapList);
                renderJson(total);
            } else {
                renderError(500);
            }
        } catch (Exception e) {
            renderNull();
        }
    }

    /**
     * 根据company_id 找到所有送检单
     **/
    public void inspect() {
        try {
            int conmpany_id = getParaToInt("company_id");
            Company company = Company.companydao.findById(conmpany_id);
            if (company != null) {
                List<Inspect> inspectList = Inspect.inspectDao.find("SELECT p.* from `db_item` i,`db_item_project` t,`db_inspect` p\n" +
                        "WHERE i.company_id=" + conmpany_id + " AND t.item_id =i.id\n" +
                        "AND p.item_project_id=t.id");
                List<Map> result = new ArrayList<>();
                for (Inspect inspect : inspectList) {
                    Map temp = new HashMap();
                    List inspectJson = new ArrayList<>();
                    switch (inspect.getStr("type")) {
                        case "water":
                            List<InspectWater> inspectWaterList = InspectWater.inspectWaterDao.find("SELECT * FROM `db_inspect_water` WHERE inspect_id='" + inspect.get("id") + "' AND process>=2");

                            for (InspectWater inspectWater : inspectWaterList) {
                                inspectJson.add(inspectWater.toJSON());
                            }
                            break;
                        case "soil":
                            List<InspectSoil> inspectSoilList = InspectSoil.inspectSoilDao.find("SELECT * FROM `db_inspect_soil` WHERE inspect_id='" + inspect.get("id") + "' AND process>=2");
                            for (InspectSoil inspectSoil : inspectSoilList) {
                                inspectJson.add(inspectSoil.toJSON());
                            }
                            break;
                        case "solid":
                            List<InspectSoild> inspectSoilds = InspectSoild.inspectSoildDao.find("SELECT * FROM `db_inspect_solid` WHERE inspect_id='" + inspect.get("id") + "' AND process>=2");
                            for (InspectSoild inspectSoild : inspectSoilds) {
                                inspectJson.add(inspectSoild.toJSON());
                            }
                            break;

                        case "air":
                            List<InspectAir> inspectAirList = InspectAir.inspectAir.find("SELECT * FROM `db_inspect_air` WHERE inspect_id='" + inspect.get("id") + "' AND process>=2");
                            for (InspectAir inspectAir : inspectAirList) {
                                inspectJson.add(inspectAir.toJSON());
                            }
                            break;
                        case "dysodia":
                            List<InspectDysodia> inspectDysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT * FROM `db_inspect_dysodia` WHERE inspect_id='" + inspect.get("id") + "' AND process>=2");
                            for (InspectDysodia inspectDysodia : inspectDysodiaList) {
                                inspectJson.add(inspectDysodia.toJSON());
                            }
                            break;
                    }
                    temp.put("items", inspectJson);
                    temp.put("inspect", inspect.toSingleJson());
                    result.add(temp);
                }
                renderJson(result);
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);

        }
    }


    /***
     * 保存报告类型
     * */
    public void create() {
        try {
            boolean result = true;
            Report report = new Report();
            String name = getPara("type");
            String path = getPara("path");
            int flag =getParaToInt("flag");
            result = result && report.set("type", name).set("path", path).set("singer", ParaUtils.getCurrentUser(getRequest()).get("id")).set("sign_time", ParaUtils.sdf.format(new Date())).set("flag",flag).save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 删除报告
     **/
    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Report.report.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 以公司为单位，显示报告
     **/
    public void List() {
        try {
            int company_id = getParaToInt("company_id");
            Company company = Company.companydao.findById(company_id);
            if (company != null) {
                List<Report> reportList = Report.report.find("SELECT * FROM `db_report` WHERE company_id=" + company_id);
                List<Map> result = new ArrayList<>();
                for (Report report : reportList) {
                    result.add(report.Json());
                }
                renderJson(result);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 报告在线查看
     * **/
    public void check(){
        try {
            int id =getParaToInt("id");
            Report report = Report.report.findById(id);
            getRequest().setAttribute("report", report);
            render("/template/report.jsp");

        }catch (Exception e){
            renderError(500);
        }
    }
}
