package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;


import java.sql.SQLException;

import java.util.*;

/**
 * Created by chenyangyang on 2017/4/15.
 */
public class DispatchController extends Controller {

    /**
     * 任务派遣创建接口
     */
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    Integer[] projectList = getParaValuesToInt("company[]");
                    for (int id : projectList) {
                        Dispatch dispatch = new Dispatch();
                        result = result && dispatch.set("company_id", id).set("creater", ParaUtils.getCurrentUser(getRequest()).get("id")).set("create_tine", ParaUtils.sdf.format(new Date())).set("date", ParaUtils.sdf2.format(new Date())).set("process", 0).save();
                        Company company = Company.companydao.findById(id);
                        if (company != null) {
                            result = result && company.set("process", 1).update();
                        } else return false;

                        if (!result) return false;
                        int charge_id = getParaToInt("charge_id");
                        DispatchUser dispatchUser = new DispatchUser();
                        result = result && dispatchUser.set("delivery_id", dispatch.get("id")).set("user_id", charge_id).set("type", 1).save();
                        if (!result) return false;
                        Integer[] joiner = getParaValuesToInt("join_id[]");
                        if (joiner != null) {
                            for (int joinId : joiner) {
                                DispatchUser dispatchJoiner = new DispatchUser();
                                result = result && dispatchJoiner.set("delivery_id", dispatch.get("id")).set("user_id", joinId).set("type", 0).save();
                                if (!result) break;
                            }
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
     * 获取个人派遣任务列表
     */
    public void UserDispatchList() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            User user = ParaUtils.getCurrentUser(getRequest());
            Page<Company> companyPage = Company.companydao.paginate(currentPage, rowCount, "SELECT c.*", " FROM `db_delivery_user` u,`db_delivery` d,`db_company` c \n" +
                    "WHERE u.user_id=" + user.get("id") + " AND u.delivery_id=d.id AND d.company_id=c.id AND d.process=0");
            List<Company> companyList = companyPage.getList();
            Map results = toJson1(companyList);
            results.put("currentPage", currentPage);
            results.put("totalPage", companyPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson1(List<Company> companyList) {
        Map result = new HashMap();
        List temp = new ArrayList();
        for (Company company : companyList) {
            temp.add(company.toSimpleJSON());
        }
        result.put("results", temp);
        return result;
    }

    /**
     * 获取taskList
     **/
    public void taskList() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            User user = ParaUtils.getCurrentUser(getRequest());
            Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT DISTINCT t.*", " FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t ,`db_inspect` s WHERE  s.analyst='" + user.get("id") + "'AND p.id = s.item_project_id AND i.id = p.item_id AND c.id = i.company_id AND t.id =c.task_id");
            List<Task> taskList = taskPage.getList();
            Map results = toJsonTask(taskList);
            results.put("currentPage", currentPage);
            results.put("totalPage", taskPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJsonTask(List<Task> taskList) {
        Map result = new HashMap();
        List temp = new ArrayList();
        for (Task task : taskList) {
            temp.add(task.toJsonSingle());
        }
        result.put("results", temp);
        return result;
    }


    /**
     * 获取项目列表
     **/
    public void item() {
        try {
            User user = ParaUtils.getCurrentUser(getRequest());
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            Map total = new HashMap();
            if (task != null) {
                List<MonitorProject> monitorProjectList = MonitorProject.monitorProjectdao.find("SELECT DISTINCT  m.* FROM`db_task`t, `db_company` c,`db_item` i,`db_item_project` p ,`db_monitor_project` m ,`db_inspect` s\n" +
                        "WHERE t.id=" + task_id + " AND c.task_id = t.id AND i.company_id=c.id AND p.item_id=i.id AND m.id = p.project_id AND s.analyst='" + user.get("id") + "'AND p.id = s.item_project_id");
                List<Map> result = new ArrayList<>();
                for (MonitorProject monitorProject : monitorProjectList) {
                    Map temp = new HashMap();
                    temp.put("project", monitorProject.toJsonSingle());
                    result.add(temp);

                }
                total.put("items", result);
                renderJson(total);
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 对应项目显示其编号，加标号，平行样
     **/

    public void inspect() {
        try {
            int task_id = getParaToInt("task_id");
            int monitor_id = getParaToInt("project_id");
            Task task = Task.taskDao.findById(task_id);
            List result = new ArrayList();
            if (task != null) {
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t \n" +
                        "WHERE p.project_id='" + monitor_id + "' AND p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id AND task_id=" + task.get("id"));
                for (ItemProject itemProject : itemProjectList) {
                    List<Inspect> inspectList = Inspect.inspectDao.find("SELECT * FROM `db_inspect` WHERE item_project_id =" + itemProject.get("id"));
                    for (Inspect inspect : inspectList) {
                        Map temp = new HashMap();
                        List item = new ArrayList();
                        List inspectJson = new ArrayList();
                        switch (inspect.getStr("type")) {
                            case "water":
                                List<InspectWater> inspectWaterList = InspectWater.inspectWaterDao.find("SELECT * FROM `db_inspect_water` WHERE  inspect_id=" + inspect.get("id"));
                                for (InspectWater inspectWater : inspectWaterList) {
                                    inspectJson.add(inspectWater.toJSON());
                                    List<InspectWaterReview> inspectWaterReviewList = InspectWaterReview.inspectWaterReviewDao.find("SELECT * FROM `db_inspect_water_review` WHERE  water_id=" + inspectWater.get("id"));
                                    for (InspectWaterReview inspectWaterReview : inspectWaterReviewList) {
                                        item.add(inspectWaterReview.toJSON());

                                    }
                                }

                                break;
                            case "soil":
                                List<InspectSoil> inspectSoilList = InspectSoil.inspectSoilDao.find("SELECT * FROM `db_inspect_soil` WHERE  inspect_id=" + inspect.get("id"));
                                for (InspectSoil inspectSoil : inspectSoilList) {
                                    inspectJson.add(inspectSoil.toJSON());
                                    List<InspectSoilReview> inspectSoilReviewList = InspectSoilReview.inspectSoilReviewDao.find("SELECT * FROM `db_inspect_soil_review` WHERE  soil_id=" + inspectSoil.get("id"));
                                    for (InspectSoilReview inspectSoilReview : inspectSoilReviewList) {
                                        item.add(inspectSoilReview.toJSON());

                                    }
                                }
                                break;
                            case "solid":
                                List<InspectSoild> inspectSoilds = InspectSoild.inspectSoildDao.find("SELECT * FROM `db_inspect_solid` WHERE  inspect_id=" + inspect.get("id"));
                                for (InspectSoild inspectSoild : inspectSoilds) {
                                    inspectJson.add(inspectSoild.toJSON());
                                    List<InspectSoildReview> inspectSoildReviewList = InspectSoildReview.inspectSoildReviewdao.find("SELECT * FROM `db_inspect_solid_review` WHERE  solid_id=" + inspectSoild.get("id"));
                                    for (InspectSoildReview inspectSoildReview : inspectSoildReviewList) {
                                        item.add(inspectSoildReview.toJSON());

                                    }
                                }
                                break;

                            case "air":
                                List<InspectAir> inspectAirList = InspectAir.inspectAir.find("SELECT * FROM `db_inspect_air` WHERE  inspect_id=" + inspect.get("id"));
                                for (InspectAir inspectAir : inspectAirList) {
                                    inspectJson.add(inspectAir.toJSON());
                                    List<InspectAirReview> inspectAirReviewList = InspectAirReview.inspectAirReview.find("SELECT * FROM `db_inspect_air_review` WHERE   air_id=" + inspectAir.get("id"));
                                    for (InspectAirReview inspectAirReview:inspectAirReviewList) {
                                        item.add(inspectAirReview.toJSON());

                                    }
                                }
                                break;
                            case "dysodia":
                                List<InspectDysodia> inspectDysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT * FROM `db_inspect_dysodia` WHERE    inspect_id=" + inspect.get("id"));
                                for (InspectDysodia inspectDysodia : inspectDysodiaList) {
                                    inspectJson.add(inspectDysodia.toJSON());
                                    List<InspectDysodiaReview> inspectDysodiaReviewList = InspectDysodiaReview.inspectDysodiaReviewdao.find("SELECT * FROM `db_inspect_dysodia_review` WHERE dysodia_id=" + inspectDysodia.get("id"));
                                    for (InspectDysodiaReview inspectDysodiaReview:inspectDysodiaReviewList) {
                                        item.add(inspectDysodiaReview.toJSON());

                                    }
                                }
                                break;
                        }
                        temp.put("item",item);
                        temp.put("items", inspectJson);
                        temp.put("inspect", inspect.toSingleJson());
                        result.add(temp);
                    }
                }
                renderJson(result);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void inspectInfo() {
        try {
            int task_id = getParaToInt("task_id");
            int monitor_id = getParaToInt("project_id");
            Task task = Task.taskDao.findById(task_id);
            Map temp = new HashMap();
            temp.put("sample_time", task.get("sample_time"));
            temp.put("sample_creater", User.userDao.findById(task.get("sample_creater")).toSimpleJson());
            temp.put("sample_receiver", User.userDao.findById(task.get("sample_receiver")).toSimpleJson());
            List<Sample> tagList = Sample.sampleDao.find("SELECT * FROM `db_sample` WHERE id in (SELECT t.sample_id FROM `db_tag` t WHERE t.project_id=" + monitor_id + " AND t.task_id=" + task_id + ")");
            List<Sample> libList = Sample.sampleDao.find("SELECT * FROM `db_sample` WHERE id in (SELECT t.sample_id FROM `db_lib` t WHERE t.project_id=" + monitor_id + " AND t.task_id=" + task_id + ")");
            temp.put("tagList", toJson(tagList));
            temp.put("libList", toJson(libList));
            renderJson(temp);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void inspectAttachment() {
        try {
            int task_id = getParaToInt("task_id");
            int monitor_id = getParaToInt("project_id");
            List<InspectAttachment> inspectAttachmentList = InspectAttachment.inspectAttachmentDao.find("SELECT * FROM `db_inspect_attachment` WHERE task_id=" + task_id + " AND project_id=" + monitor_id);
            renderJson(inspectAttachmentList);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public List toJson(List<Sample> entityList) {
        List result = new ArrayList();
        try {
            for (Sample sample : entityList) {
                result.add(toJsonSingle(sample));
            }
            //json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return result;
    }

    public Map toJsonSingle(Sample sample) {
        Map<String, Object> types = new HashMap<>();
        types.put("id", sample.getInt("id"));
        types.put("identify", sample.get("identify"));
        types.put("result", sample.get("result"));
        return types;
    }

    /**
     * 检查当前任务书是否已经允许流转到实验室(样品交接表)
     */
    public void checkFlowLab() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                int size = Company.companydao.find("SELECT * FROM `db_company` c WHERE c.task_id=" + task_id + " AND c.process!=2").size();
                if (size != 0) {
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                } else {
                    Boolean result = task.set("process", ProcessKit.getTaskProcess("laboratory")).set("sample_time", ParaUtils.sdf2.format(new Date())).set("sample_creater", ParaUtils.getCurrentUser(getRequest()).get("id")).update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 检查当前任务书是否已经允许流转到质控室（质控表）
     */
    public void checkFlow() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                int size = Sample.sampleDao.find("SELECT s.* FROM `db_company` c,`db_sample` s WHERE c.task_id=" + task_id + "  AND s.company_id =c.id AND s.process!=3").size();
                if (size != 0 || task.get("flag2") != 1) {
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                } else {
                    Boolean result = task.set("process", ProcessKit.getTaskProcess("quality")).update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 质控完成，重新将任务流转到实验室
     **/
    public void checkFlowLabtorary() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            boolean result = true;
            if (task != null) {
                int itemProjectSize = ItemProject.itemprojectDao.find("SELECT p.* From `db_task` t,`db_company` c,`db_item` i,`db_item_project` p WHERE t.id='" + task_id + "'AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id AND (p.process is NULL OR p.inspect is null)").size();

                if (itemProjectSize != 0) {
                    //还有没有质控
                    renderJson(RenderUtils.CODE_UNIQUE);
                } else {
                    result = result && task.set("process", ProcessKit.getTaskProcess("lab")).update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }

            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }


}
