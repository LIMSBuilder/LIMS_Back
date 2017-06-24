package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
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
     * 保存报告
     * 0-上传的报告 1- 生成的报告
     * */
    public void create() {
        try {
            Report report = new Report();
            Boolean result = report
                    .set("type", getPara("type"))
                    .set("flag", 0)
                    .set("report_path", getPara("report_path"))
                    .set("process", 0)
                    .set("company_id", getPara("company_id"))
                    .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                    .set("create_time", ParaUtils.sdf.format(new Date()))
                    .save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 删除报告
     **/
    public void deleteReport() {
        try {
            int id = getParaToInt("report_id");
            Boolean result = Report.report.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 批量删除报告
     * **/
    public void deleteAll() {
        try {
            Integer[] selected = getParaValuesToInt("selected[]");
            Boolean result = true;
            for (int i = 0; i < selected.length; i++) {
                int id = selected[i];
                result = result && Report.report.deleteById(id);
                if (!result) break;
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 以公司为单位，显示报告
     **/
    public void list() {
        try {
            int company_id = getParaToInt("id");
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
     **/
    public void check() {
        try {
            int id = getParaToInt("id");
            Report report = Report.report.findById(id);
            getRequest().setAttribute("report", report);
            render("/template/report.jsp");

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 报告审核意见
     **/
    public void reviewMessage() {
        try {
            int id = getParaToInt("id");
            Report report = Report.report.findById(id);
            Map total = new HashMap();
            if (report != null) {
                List<ReportFirstReview> reportFirstReviewList = ReportFirstReview.reportFirstReview.find("SELECT * FROM `db_report_first_review` WHERE report_id=" + id);
                List<Map> result = new ArrayList<>();
                for (ReportFirstReview reportFirstReview : reportFirstReviewList) {
                    result.add(reportFirstReview.Json());
                }
                total.put("firstReview", result);
                List<ReportSecondReview> reportSecondReviewList = ReportSecondReview.reportSecondReview.find("SELECT * FROM `db_report_second_review` WHERE report_id=" + id);
                List<Map> result2 = new ArrayList<>();
                for (ReportSecondReview reportSecondReview : reportSecondReviewList) {
                    result2.add(reportSecondReview.Json());
                }
                total.put("secondReview", result2);
                List<ReportThirdReview> reportThirdReviewList = ReportThirdReview.reportThirdReview.find("SELECT * FROM `db_report_third_review` WHERE report_id=" + id);
                List<Map> result3 = new ArrayList<>();
                for (ReportThirdReview reportThirdReview : reportThirdReviewList) {
                    result3.add(reportThirdReview.Json());
                }
                total.put("thirdReview", result3);
                renderJson(total);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }

    }


    /**
     * 报告编制流转
     */
    public void editFlow() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    List<Company> companyList = Company.companydao.find("SELECT c.* FROM `db_task` t,`db_company` c WHERE c.task_id=t.id AND t.id=" + getPara("task_id"));
                    for (Company company : companyList) {
                        List<Report> reportList = Report.report.find("SELECT r.* FROM `db_report` r WHERE company_id=" + company.get("id"));
                        if (reportList.size() == 0) {
                            result = false;
                            break;
                        }
                        for (Report report : reportList) {
                            if (report.getInt("process") < 1)
                                result = result && report.set("process", 1).set("firstReview", null).set("secondReview", null).set("thirdReview", null).update();
                        }
                    }
                    Task task = Task.taskDao.findById(getPara("task_id"));
                    result = result && task.set("process", ProcessKit.getTaskProcess("reportFirstReview")).update();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void firstReview() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    ReportFirstReview firstReview = new ReportFirstReview();
                    Boolean result = firstReview.set("condition1", getPara("condition1"))
                            .set("condition2", getPara("condition2"))
                            .set("condition3", getPara("condition3"))
                            .set("condition4", getPara("condition4"))
                            .set("condition5", getPara("condition5"))
                            .set("condition6", getPara("condition6"))
                            .set("condition7", getPara("condition7"))
                            .set("other", getPara("other"))
                            .set("reviewer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                            .set("review_time", ParaUtils.sdf.format(new Date()))
                            .set("report_id", getPara("report_id"))
                            .save();
                    Report report = Report.report.findById(getPara("report_id"));
                    report.set("firstReview", firstReview.get("id"));
//                    result = result && report.set("firstReview", firstReview.get("id")).update();
                    if (getParaToInt("condition1") + getParaToInt("condition2") + getParaToInt("condition3") + getParaToInt("condition4") + getParaToInt("condition5") + getParaToInt("condition6") + getParaToInt("condition7") != 7) {
                        //审核拒绝
                        report.set("process", -1);
                    } else {
                        //审核通过
                        report.set("process", 2);
                    }
                    result = result && report.update();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void secondReview() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    ReportSecondReview secondReview = new ReportSecondReview();
                    Boolean result = secondReview.set("condition1", getPara("condition1"))
                            .set("condition2", getPara("condition2"))
                            .set("condition3", getPara("condition3"))
                            .set("condition4", getPara("condition4"))
                            .set("condition5", getPara("condition5"))
                            .set("condition6", getPara("condition6"))
                            .set("other", getPara("other"))
                            .set("reviewer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                            .set("review_time", ParaUtils.sdf.format(new Date()))
                            .set("report_id", getPara("report_id"))
                            .save();
                    Report report = Report.report.findById(getPara("report_id"));
                    report.set("secondReview", secondReview.get("id"));
                    if (getParaToInt("condition1") + getParaToInt("condition2") + getParaToInt("condition3") + getParaToInt("condition4") + getParaToInt("condition5") + getParaToInt("condition6") != 6) {
                        //审核拒绝
                        report.set("process", -2);
                    } else {
                        //审核通过
                        report.set("process", 3);
                    }
                    result = result && report.update();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void thirdReview() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    ReportThirdReview thirdReview = new ReportThirdReview();
                    Boolean result = thirdReview.set("condition1", getPara("condition1"))
                            .set("condition2", getPara("condition2"))
                            .set("condition3", getPara("condition3"))
                            .set("other", getPara("other"))
                            .set("reviewer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                            .set("review_time", ParaUtils.sdf.format(new Date()))
                            .set("report_id", getPara("report_id"))
                            .save();
                    Report report = Report.report.findById(getPara("report_id"));
                    report.set("thirdReview", thirdReview.get("id"));
                    if (getParaToInt("condition1") + getParaToInt("condition2") + getParaToInt("condition3") != 3) {
                        //审核拒绝
                        report.set("process", -3);
                    } else {
                        //审核通过
                        report.set("process", 4);
                    }
                    result = result && report.update();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void firstReviewFlow() {
        try {
            Boolean result = true;
            Task task = Task.taskDao.findById(getPara("task_id"));
            if (task != null) {
                List<Report> reportList = Report.report.find("SELECT r.* FROM `db_task` t,`db_company` c ,`db_report` r\n" +
                        "WHERE t.id=" + getPara("task_id") + " AND c.task_id=t.id AND r.company_id=c.id AND r.process=1");
                if (reportList.size() != 0) {
                    //还有未处理的报告审核
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                } else {
                    List<Report> rejectReport = Report.report.find("SELECT r.* FROM `db_task` t,`db_company` c ,`db_report` r\n" +
                            "WHERE t.id=" + getPara("task_id") + " AND c.task_id=t.id AND r.company_id=c.id AND r.process=-1");
                    if (rejectReport.size() != 0) {
                        //存在审核拒绝的记录
                        result = result && task.set("process", ProcessKit.getTaskProcess("report")).update();
                    } else {
                        //全部审核通过
                        result = result && task.set("process", ProcessKit.getTaskProcess("reportSecondReview")).update();
                    }
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void secondReviewFlow() {
        try {
            Boolean result = true;
            Task task = Task.taskDao.findById(getPara("task_id"));
            if (task != null) {
                List<Report> reportList = Report.report.find("SELECT r.* FROM `db_task` t,`db_company` c ,`db_report` r\n" +
                        "WHERE t.id=" + getPara("task_id") + " AND c.task_id=t.id AND r.company_id=c.id AND r.process=2");
                if (reportList.size() != 0) {
                    //还有未处理的报告审核
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                } else {
                    List<Report> rejectReport = Report.report.find("SELECT r.* FROM `db_task` t,`db_company` c ,`db_report` r\n" +
                            "WHERE t.id=" + getPara("task_id") + " AND c.task_id=t.id AND r.company_id=c.id AND r.process=-2");
                    if (rejectReport.size() != 0) {
                        //存在审核拒绝的记录
                        result = result && task.set("process", ProcessKit.getTaskProcess("report")).update();
                    } else {
                        //全部审核通过
                        result = result && task.set("process", ProcessKit.getTaskProcess("reportThirdReview")).update();
                    }
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void thirdReviewFlow() {
        try {
            Boolean result = true;
            Task task = Task.taskDao.findById(getPara("task_id"));
            if (task != null) {
                List<Report> reportList = Report.report.find("SELECT r.* FROM `db_task` t,`db_company` c ,`db_report` r\n" +
                        "WHERE t.id=" + getPara("task_id") + " AND c.task_id=t.id AND r.company_id=c.id AND r.process=3");
                if (reportList.size() != 0) {
                    //还有未处理的报告审核
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                } else {
                    List<Report> rejectReport = Report.report.find("SELECT r.* FROM `db_task` t,`db_company` c ,`db_report` r\n" +
                            "WHERE t.id=" + getPara("task_id") + " AND c.task_id=t.id AND r.company_id=c.id AND r.process=-3");
                    if (rejectReport.size() != 0) {
                        //存在审核拒绝的记录
                        result = result && task.set("process", ProcessKit.getTaskProcess("report")).update();
                    } else {
                        //全部审核通过
                        result = result && task.set("process", ProcessKit.getTaskProcess("reportReceive")).update();
                    }
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void receive() {
        try {
            Report report = Report.report.findById(getPara("report_id"));
            if (report != null) {
                Boolean result = report.set("process", 5).update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else
                renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void receiveFlow() {
        try {
            Boolean result = true;
            Task task = Task.taskDao.findById(getPara("task_id"));
            if (task != null) {
                List<Report> reportList = Report.report.find("SELECT r.* FROM `db_task` t,`db_company` c ,`db_report` r\n" +
                        "WHERE t.id=" + getPara("task_id") + " AND c.task_id=t.id AND r.company_id=c.id AND r.process=4");
                if (reportList.size() != 0) {
                    //还有未处理的报告接收
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                } else {
                    result = result && task.set("process", ProcessKit.getTaskProcess("finish")).update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }
            }
        } catch (Exception e) {
            renderError(500);
        }
    }
}