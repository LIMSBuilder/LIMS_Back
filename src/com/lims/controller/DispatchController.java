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
import com.sun.jdi.InvalidLineNumberException;
import org.apache.poi.ss.formula.functions.T;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
            Map results = toJson(companyList);
            results.put("currentPage", currentPage);
            results.put("totalPage", companyPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<Company> companyList) {
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
            Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT DISTINCT t.*", " FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t WHERE p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id AND p.assayer=" + user.get("id"));
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
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t \n" +
                        "WHERE p.assayer='" + user.get("id") + "' AND p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id AND task_id=" + task.get("id"));
                Map<Object, List> obj = new HashMap<>();
                List<Map> result = new ArrayList<>();
                for (ItemProject itemProject : itemProjectList) {
                    Map temp = new HashMap();
                    temp = itemProject.toJsonSingle();
                    if (obj.containsKey(itemProject)) {
                        List item = obj.get(itemProject);
                        item.add(temp);
                        result.addAll(item);
                    } else {
                        List item = new ArrayList();
                        item.add(temp);
                        result.addAll(item);
                    }
                    total.put("items", result);
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
     * 对应项目显示其编号，加标号，平行样
     **/

    public void inspect() {
        try {
            int task_id = getParaToInt("task_id");
            int monitor_id = getParaToInt("monitor_id");
            Task task = Task.taskDao.findById(task_id);
            Map total = new HashMap();
            List result = new ArrayList();
            if (task != null) {
                List<Sample> sampleList = Sample.sampleDao.find("SELECT DISTINCT s.* FROM `db_company` c,`db_sample` s, `db_sample_project` p, `db_item_project` i WHERE c.task_id = '" + task_id + "' AND s.company_id = c.ic AND i.project_id='" + monitor_id + "'AND p.item_project_id = i.id");

                for (Sample sample:sampleList){
                    Map temp =new HashMap();
                    temp =sample.toSimpleJson();
                    List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE sample_id=" + sample.get("id"));
                    List<Map> mapList1 = new ArrayList<>();
                    for (Lib lib : libList) {
                        mapList1.add(sample.toSimpleJson());
                    }
                    temp.put("lab", mapList1);

                    List<Tag> tagList = Tag.tagDao.find("select * from `db_tag` where sample_id=" + sample.get("id"));
                    List<Map> ta = new ArrayList<>();
                    for (Tag tag : tagList) {
                        ta.add(sample.toSimpleJson());
                    }
                    temp.put("tag", ta);
                    result.add(temp);
                }
                total.put("items",result);
                renderJson(total);
            } else {
                renderNull();
            }


        } catch (Exception e) {
            renderError(500);
        }
    }

//    /**
//     * 获取项目列表
//     **/
//    public void itemList() {
//        try {
//
//            User user = ParaUtils.getCurrentUser(getRequest());
//            List<Task> taskList = Task.taskDao.find("SELECT DISTINCT t.* FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t WHERE p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id AND p.assayer=" + user.get("id"));
//            Map total = new HashMap();
//            List<Map> result = new ArrayList();
//            for (Task task : taskList) {
//
//                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t \n" +
//                        "WHERE p.assayer='" + user.get("id") + "' AND p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id AND task_id=" + task.get("id"));
//                Map<Object, List> obj = new HashMap<>();
//                for (ItemProject itemProject : itemProjectList) {
//                    Map temp = new HashMap();
//                    temp = itemProject.toJsonSingle();
//                    temp.put("identify", task.get("identify"));
//                    temp.put("sample_time", task.get("sample_time"));
//                    temp.put("receive_time", task.get("receive_time"));
//                    temp.put("sample_creater", User.userDao.findById(task.get("sample_creater")).get("name"));
//                    temp.put("sample_receiver", User.userDao.findById(task.get("sample_receiver")).get("name"));
//                    if (obj.containsKey(itemProject)) {
//
//                        List item = obj.get(itemProject);
//                        List<Map> re = new ArrayList<>();
//                        List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_company` c,`db_sample` s,`db_sample_project` p WHERE c.task_id='" + task.get("id") + "'AND s.company_id =c.id AND p.sample_id=s.id AND p.item_project_id=" + itemProject.get("id"));
//                        for (Sample sample : sampleList) {
//                            re.add(sample.toSimpleJson());
//                        }
//                        temp.put("sample", re);
//                        List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE item_project_id=" + itemProject.get("id"));
//                        List<Map> mapList1 = new ArrayList<>();
//                        for (Lib lib : libList) {
//                            Sample sample = Sample.sampleDao.findById(lib.get("sample_id"));
//                            mapList1.add(sample.toSimpleJson());
//                        }
//                        temp.put("lab", mapList1);
//
//                        List<Tag> tagList = Tag.tagDao.find("select * from `db_tag` where item_project_id=" + itemProject.get("id"));
//                        List<Map> ta = new ArrayList<>();
//                        for (Tag tag : tagList) {
//                            Sample sample = Sample.sampleDao.findById(tag.get("sample_id"));
//                            ta.add(sample.toSimpleJson());
//                        }
//                        temp.put("tag", ta);
//                        item.add(temp);
//                        result.addAll(item);
//
//                    } else {
//                        List item = new ArrayList();
//                        List<Map> re = new ArrayList<>();
//                        List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_company` c,`db_sample` s,`db_sample_project` p WHERE c.task_id='" + task.get("id") + "'AND s.company_id =c.id AND p.sample_id=s.id AND p.item_project_id=" + itemProject.get("id"));
//                        for (Sample sample : sampleList) {
//                            re.add(sample.toSimpleJson());
//                        }
//                        temp.put("sample", re);
//                        List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE item_project_id=" + itemProject.get("id"));
//                        List<Map> mapList1 = new ArrayList<>();
//                        for (Lib lib : libList) {
//                            Sample sample = Sample.sampleDao.findById(lib.get("sample_id"));
//                            mapList1.add(sample.toSimpleJson());
//                        }
//                        temp.put("lab", mapList1);
//
//                        List<Tag> tagList = Tag.tagDao.find("select * from `db_tag` where item_project_id=" + itemProject.get("id"));
//                        List<Map> ta = new ArrayList<>();
//                        for (Tag tag : tagList) {
//                            Sample sample = Sample.sampleDao.findById(tag.get("sample_id"));
//                            ta.add(sample.toSimpleJson());
//                        }
//                        temp.put("tag", ta);
//                        item.add(temp);
//                        result.addAll(item);
//
//                    }
//
//                }
//                total.put("items", result);
//            }
//
//            renderJson(total);
//        } catch (Exception e) {
//            renderError(500);
//        }
//    }


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
                if (size != 0) {
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
                int itemProjectSize = ItemProject.itemprojectDao.find("SELECT p.* From `db_task` t,`db_company` c,`db_item` i,`db_item_project` p WHERE t.id='" + task_id + "'AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id AND p.process is NULL").size();
                if (itemProjectSize != 0) {
                    //还有没有质控
                    renderJson(RenderUtils.CODE_UNIQUE);
                    return;
                } else {
                    result = result && task.set("process", ProcessKit.getTaskProcess("lab")).update();

                }
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }


}
