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
                int itemProjectSize = ItemProject.itemprojectDao.find("SELECT p.* From `db_task` t,`db_company` c,`db_item` i,`db_item_project` p WHERE t.id=" + task_id + "AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id AND p.process is NULL").size();
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
