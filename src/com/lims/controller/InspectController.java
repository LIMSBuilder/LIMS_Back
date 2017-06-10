package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;
import com.lims.utils.LoggerKit;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by chenyangyang on 2017/6/3.
 */
public class InspectController extends Controller {
    /**
     * 保存送检单数据
     **/
    public void save() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    String type = getPara("type");
                    Boolean result = true;
                    switch (type) {
                        case "water":
                            InspectWater inspectWater = InspectWater.inspectWaterDao.findById(getPara("id"));
                            if (inspectWater != null) {
                                result = result && inspectWater.set("result", getPara("result")).set("process", 1).update();
                            } else return false;
                            break;
                        case "soil":
                            InspectSoil inspectSoil = InspectSoil.inspectSoilDao.findById(getPara("id"));
                            if (inspectSoil != null) {
                                result = result && inspectSoil.set("result", getPara("result")).set("point", getPara("point")).set("remark", getPara("remark")).set("process", 1).update();
                            } else return false;
                            break;
                        case "solid":
                            InspectSoild inspectSoild = InspectSoild.inspectSoildDao.findById(getPara("id"));
                            if (inspectSoild != null) {
                                result = result && inspectSoild.set("result", getPara("result")).set("volume", getPara("volume")).set("flow", getPara("flow")).set("concentration", getPara("concentration")).set("discharge", getPara("discharge")).set("process", 1).update();
                            } else return false;
                            break;
                        case "air":
                            InspectAir inspectAir = InspectAir.inspectAir.findById(getPara("id"));
                            if (inspectAir != null) {
                                result = result && inspectAir.set("result", getPara("result")).set("volume", getPara("volume")).set("concentration", getPara("concentration")).set("process", 1).update();
                            } else return false;
                            break;
                        case "dysodia":
                            InspectDysodia inspectDysodia = InspectDysodia.inspectDysodiaDao.findById(getPara("id"));
                            if (inspectDysodia != null) {
                                result = result && inspectDysodia.set("result", getPara("result")).set("concentration", getPara("concentration")).set("process", 1).update();
                            } else return false;
                            break;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void saveAttachment() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    int task_id = getParaToInt("task_id");
                    int project_id = getParaToInt("project_id");
                    String path = getPara("path");

                    String fileName = path.trim().substring(path.trim().lastIndexOf("\\") + 1);
                    InspectAttachment attachment = new InspectAttachment();
                    result = result && attachment.set("task_id", task_id).set("project_id", project_id).set("path", path).set("name", fileName).save();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void deleteAttachment() {
        try {
            int id = getParaToInt("id");
            Boolean result = InspectAttachment.inspectAttachmentDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 实验室原始记录表附件文件在线查看
     * **/
    public void download() {
        try {
            int id = getParaToInt("id");
            InspectAttachment inspectAttachment = InspectAttachment.inspectAttachmentDao.findById(id);
            getRequest().setAttribute("inspectAttachment", inspectAttachment);
            render("/template/labOrigin.jsp");
        } catch (Exception e) {
            renderError(500);
        }

    }

    /**
     * 实验室分析任务流转
     **/
    public void flowWork() {
        try {
            int task_id = getParaToInt("task_id");
            int project_id = getParaToInt("project_id");
            Task task = Task.taskDao.findById(task_id);
            boolean result = true;
            if (task != null) {
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_item_project` p,`db_item` i,`db_company` c,`db_task` t \n" +
                        "WHERE p.project_id='" + project_id + "' AND p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id AND task_id=" + task.get("id"));
                for (ItemProject itemProject : itemProjectList) {
                    List<Inspect> inspectList = Inspect.inspectDao.find("SELECT * FROM `db_inspect` WHERE item_project_id =" + itemProject.get("id"));
                    for (Inspect inspect : inspectList) {
                        switch (inspect.getStr("type")) {
                            case "water":
                                List<InspectWater> inspectWaterList = InspectWater.inspectWaterDao.find("SELECT * FROM `db_inspect_water` WHERE inspect_id='" + inspect.get("id") + "'AND process =0");
                                if (inspectWaterList.size() != 0) {
                                    renderJson(RenderUtils.CODE_UNIQUE);
                                } else {
                                    result = result && inspect.set("analysis_time", ParaUtils.sdf2.format(new Date())).update();
                                    List<InspectWater> inspectWaterList1 = InspectWater.inspectWaterDao.find("SELECT * FROM `db_inspect_water` WHERE inspect_id=" + inspect.get("id"));
                                    for (InspectWater inspectWater : inspectWaterList1) {
                                        result = result && inspectWater.set("process", 2).update();
                                    }
                                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                                }
                                break;
                            case "soil":
                                List<InspectSoil> inspectSoilList = InspectSoil.inspectSoilDao.find("SELECT * FROM `db_inspect_soil` WHERE inspect_id='" + inspect.get("id") + "'AND process =0");
                                if (inspectSoilList.size() != 0) {
                                    renderJson(RenderUtils.CODE_UNIQUE);
                                } else {
                                    result = result && inspect.set("analysis_time", ParaUtils.sdf2.format(new Date())).update();
                                    List<InspectSoil> inspectSoilList1 = InspectSoil.inspectSoilDao.find("SELECT * FROM `db_inspect_soil` WHERE inspect_id=" + inspect.get("id"));
                                    for (InspectSoil inspectSoil : inspectSoilList1) {
                                        result = result && inspectSoil.set("process", 2).update();
                                    }
                                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

                                }
                                break;
                            case "solid":
                                List<InspectSoild> inspectSoildList = InspectSoild.inspectSoildDao.find("SELECT * FROM `db_inspect_solid` WHERE inspect_id='" + inspect.get("id") + "'AND process =0");
                                if (inspectSoildList.size() != 0) {
                                    renderJson(RenderUtils.CODE_UNIQUE);
                                } else {
                                    result = result && inspect.set("analysis_time", ParaUtils.sdf2.format(new Date())).update();
                                    List<InspectSoild> inspectSoildList1 = InspectSoild.inspectSoildDao.find("SELECT * FROM `db_inspect_solid` WHERE inspect_id=" + inspect.get("id"));
                                    for (InspectSoild inspectSoild : inspectSoildList1) {
                                        result = result && inspectSoild.set("process", 2).update();
                                    }
                                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                                }
                                break;
                            case "air":
                                List<InspectAir> inspectAirList = InspectAir.inspectAir.find("SELECT * FROM `db_inspect_air` WHERE inspect_id='" + inspect.get("id") + "'AND process =0");
                                if (inspectAirList.size() != 0) {
                                    renderJson(RenderUtils.CODE_UNIQUE);
                                } else {
                                    result = result && inspect.set("analysis_time", ParaUtils.sdf2.format(new Date())).update();
                                    List<InspectAir> inspectAirList1 = InspectAir.inspectAir.find("SELECT * FROM `db_inspect_air` WHERE inspect_id=" + inspect.get("id"));
                                    for (InspectAir inspectAir : inspectAirList1) {
                                        result = result && inspectAir.set("process", 2).update();
                                    }
                                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                                }
                                break;

                            case "dysodia":
                                List<InspectDysodia> inspectDysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT * FROM `db_inspect_dysodia` WHERE inspect_id='" + inspect.get("id") + "'AND process =0");
                                if (inspectDysodiaList.size() != 0) {
                                    renderJson(RenderUtils.CODE_UNIQUE);
                                } else {
                                    result = result && inspect.set("analysis_time", ParaUtils.sdf2.format(new Date())).update();
                                    List<InspectDysodia> inspectDysodiaList1 = InspectDysodia.inspectDysodiaDao.find("SELECT * FROM `db_inspect_dysodia` WHERE inspect_id=" + inspect.get("id"));
                                    for (InspectDysodia inspectDysodia : inspectDysodiaList1) {
                                        result = result && inspectDysodia.set("process", 2).update();
                                    }
                                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                                }
                                break;

                        }
                    }
                }
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 复合任务显示 taskList
     **/
    public void taskList() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT DISTINCT t.*", "FROM `db_inspect_water` w ,`db_sample` s,`db_company` c,`db_task` t,`db_inspect_air` a,`db_inspect_dysodia` dy,`db_inspect_soil` so,`db_inspect_solid` li\n" +
                    " WHERE (\n" +
                    "(w.process=2 AND s.id=w.sample_id)\n" +
                    "OR(a.process=2 AND s.id=a.sample_id)\n" +
                    "OR(dy.process=2 AND s.id=dy.sample_id )\n" +
                    "OR(so.process=2 AND s.id=so.sample_id)\n" +
                    "OR(li.process=2 AND s.id=li.sample_id)\n" +
                    ")AND c.id =s.company_id AND t.id =c.task_id ");
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
     * 显示复核项目
     **/
    public void itemList() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                Map total = new HashMap();
                List<MonitorProject> monitorProjectList = MonitorProject.monitorProjectdao.find("SELECT DISTINCT m.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p ,`db_inspect` isp,\n" +
                        "`db_inspect_water` w,\n" +
                        "`db_inspect_air` ai,\n" +
                        "`db_inspect_soil` so ,\n" +
                        "`db_inspect_solid` sl ,\n" +
                        "`db_inspect_dysodia` dy ,\n" +
                        "`db_monitor_project` m\n" +
                        "WHERE t.id='" + task_id +
                        "' AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id AND isp.item_project_id=p.id AND p.project_id=m.id\n" +
                        "AND (\n" +
                        "(w.inspect_id=isp.id AND w.process>=2) OR \n" +
                        "(ai.inspect_id=isp.id AND ai.process>=2) OR\n" +
                        "(so.inspect_id=isp.id AND so.process>=2) OR\n" +
                        "(sl.inspect_id=isp.id AND sl.process>=2) OR\n" +
                        "(dy.inspect_id=isp.id AND dy.process>=2)\n" +
                        ")");
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
     * 显示复核项目对应的详情
     **/
    public void detail() {
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
                        List inspectJson = new ArrayList();
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
     * f复核
     **/
    public void review() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    String type = getPara("type");
                    Boolean result = true;
                    switch (type) {
                        case "water":
                            InspectWater inspectWater = InspectWater.inspectWaterDao.findById(getPara("id"));
                            if (inspectWater != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectWaterReview inspectWaterReview = new InspectWaterReview();
                                result = result && inspectWaterReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("water_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    result = result && inspectWater.set("review_id", inspectWaterReview.get("id")).set("process", 3).update();
                                } else {
                                    result = result && inspectWater.set("review_id", inspectWaterReview.get("id")).set("process", 0).update();
                                }
                            } else return false;

                            break;
                        case "soil":
                            InspectSoil inspectSoil = InspectSoil.inspectSoilDao.findById(getPara("id"));
                            if (inspectSoil != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectSoilReview inspectSoilReview = new InspectSoilReview();
                                result = result && inspectSoilReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("soil_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    result = result && inspectSoil.set("review_id", inspectSoilReview.get("id")).set("process", 3).update();
                                } else {
                                    result = result && inspectSoil.set("review_id", inspectSoilReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
                        case "solid":
                            InspectSoild inspectSoild = InspectSoild.inspectSoildDao.findById(getPara("id"));
                            if (inspectSoild != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectSoildReview inspectSoildReview = new InspectSoildReview();
                                result = result && inspectSoildReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("solid_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    result = result && inspectSoild.set("review_id", inspectSoildReview.get("id")).set("process", 3).update();
                                } else {
                                    result = result && inspectSoild.set("review_id", inspectSoildReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
                        case "air":
                            InspectAir inspectAir = InspectAir.inspectAir.findById(getPara("id"));
                            if (inspectAir != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectAirReview inspectAirReview = new InspectAirReview();
                                result = result && inspectAirReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("air_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    result = result && inspectAir.set("review_id", inspectAirReview.get("id")).set("process", 3).update();
                                } else {
                                    result = result && inspectAir.set("review_id", inspectAirReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
                        case "dysodia":
                            InspectDysodia inspectDysodia = InspectDysodia.inspectDysodiaDao.findById(getPara("id"));
                            if (inspectDysodia != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectDysodiaReview inspectDysodiaReview = new InspectDysodiaReview();
                                result = result && inspectDysodiaReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("solid_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    result = result && inspectDysodia.set("review_id", inspectDysodiaReview.get("id")).set("process", 3).update();
                                } else {
                                    result = result && inspectDysodia.set("review_id", inspectDysodiaReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
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
     * 审核taskList
     **/
    public void reviewTaskList() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT DISTINCT t.*", "FROM `db_inspect_water` w ,`db_sample` s,`db_company` c,`db_task` t,`db_inspect_air` a,`db_inspect_dysodia` dy,`db_inspect_soil` so,`db_inspect_solid` li\n" +
                    " WHERE (\n" +
                    "(w.process=3 AND s.id=w.sample_id)\n" +
                    "OR(a.process=3 AND s.id=a.sample_id)\n" +
                    "OR(dy.process=3 AND s.id=dy.sample_id )\n" +
                    "OR(so.process=3 AND s.id=so.sample_id)\n" +
                    "OR(li.process=3 AND s.id=li.sample_id)\n" +
                    ")AND c.id =s.company_id AND t.id =c.task_id ");
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

    /**
     * 审核具体项目
     **/
    public void reviewItem() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                Map total = new HashMap();
                List<MonitorProject> monitorProjectList = MonitorProject.monitorProjectdao.find("SELECT DISTINCT m.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p ,`db_inspect` isp,\n" +
                        "`db_inspect_water` w,\n" +
                        "`db_inspect_air` ai,\n" +
                        "`db_inspect_soil` so ,\n" +
                        "`db_inspect_solid` sl ,\n" +
                        "`db_inspect_dysodia` dy ,\n" +
                        "`db_monitor_project` m\n" +
                        "WHERE t.id='" + task_id +
                        "' AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id AND isp.item_project_id=p.id AND p.project_id=m.id\n" +
                        "AND (\n" +
                        "(w.inspect_id=isp.id AND w.process>=3) OR \n" +
                        "(ai.inspect_id=isp.id AND ai.process>=3) OR\n" +
                        "(so.inspect_id=isp.id AND so.process>=3) OR\n" +
                        "(sl.inspect_id=isp.id AND sl.process>=3) OR\n" +
                        "(dy.inspect_id=isp.id AND dy.process>=3)\n" +
                        ")");
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
     * 审核具体项目list
     **/
    public void reviewItemList() {
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
                        List inspectJson = new ArrayList();
                        switch (inspect.getStr("type")) {
                            case "water":
                                List<InspectWater> inspectWaterList = InspectWater.inspectWaterDao.find("SELECT * FROM `db_inspect_water` WHERE inspect_id='" + inspect.get("id") + "' AND process>=3");

                                for (InspectWater inspectWater : inspectWaterList) {
                                    inspectJson.add(inspectWater.toJSON());
                                }
                                break;
                            case "soil":
                                List<InspectSoil> inspectSoilList = InspectSoil.inspectSoilDao.find("SELECT * FROM `db_inspect_soil` WHERE inspect_id='" + inspect.get("id") + "' AND process>=3");
                                for (InspectSoil inspectSoil : inspectSoilList) {
                                    inspectJson.add(inspectSoil.toJSON());
                                }
                                break;
                            case "solid":
                                List<InspectSoild> inspectSoilds = InspectSoild.inspectSoildDao.find("SELECT * FROM `db_inspect_solid` WHERE inspect_id='" + inspect.get("id") + "' AND process>=3");
                                for (InspectSoild inspectSoild : inspectSoilds) {
                                    inspectJson.add(inspectSoild.toJSON());
                                }
                                break;

                            case "air":
                                List<InspectAir> inspectAirList = InspectAir.inspectAir.find("SELECT * FROM `db_inspect_air` WHERE inspect_id='" + inspect.get("id") + "' AND process>=3");
                                for (InspectAir inspectAir : inspectAirList) {
                                    inspectJson.add(inspectAir.toJSON());
                                }
                                break;
                            case "dysodia":
                                List<InspectDysodia> inspectDysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT * FROM `db_inspect_dysodia` WHERE inspect_id='" + inspect.get("id") + "' AND process>=3");
                                for (InspectDysodia inspectDysodia : inspectDysodiaList) {
                                    inspectJson.add(inspectDysodia.toJSON());
                                }
                                break;
                        }
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

    /**
     * 审核结果
     **/
    public void reviewList() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    String type = getPara("type");
                    int task_id = getParaToInt("task_id");
                    Boolean result = true;
                    switch (type) {
                        case "water":
                            InspectWater inspectWater = InspectWater.inspectWaterDao.findById(getPara("id"));
                            if (inspectWater != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectWaterReview inspectWaterReview = new InspectWaterReview();
                                result = result && inspectWaterReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("water_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    int size = Task.taskDao.find("SELECT DISTINCT t.*\n" +
                                            "FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                            "`db_inspect_air` a,\n" +
                                            "`db_inspect_dysodia` dy,\n" +
                                            "`db_inspect_soil` so,\n" +
                                            "`db_inspect_solid` sd,\n" +
                                            "`db_inspect_water` wt\n" +
                                            "WHERE t.id=" + task_id +
                                            " AND c.task_id=t.id AND s.company_id=c.id\n" +
                                            "AND (\n" +
                                            "(a.sample_id=s.id AND a.process<4) OR\n" +
                                            "(dy.sample_id=s.id AND dy.process<4) OR\n" +
                                            "(so.sample_id=s.id AND so.process<4) OR\n" +
                                            "(sd.sample_id=s.id AND sd.process<4) OR\n" +
                                            "(wt.sample_id=s.id AND wt.process<4)\n" +
                                            ")").size();
                                    if (size == 0) {
                                        Task task = Task.taskDao.findById(task_id);
                                        result = result && task.set("process", ProcessKit.getTaskProcess("firstReview")).update();
                                    }
                                    result = result && inspectWater.set("check_id", inspectWaterReview.get("id")).set("process", 4).update();
                                } else {
                                    result = result && inspectWater.set("check_id", inspectWaterReview.get("id")).set("process", 0).update();
                                }
                            } else return false;

                            break;
                        case "soil":
                            InspectSoil inspectSoil = InspectSoil.inspectSoilDao.findById(getPara("id"));
                            if (inspectSoil != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectSoilReview inspectSoilReview = new InspectSoilReview();
                                result = result && inspectSoilReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("soil_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    int size = Task.taskDao.find("SELECT DISTINCT t.*\n" +
                                            "FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                            "`db_inspect_air` a,\n" +
                                            "`db_inspect_dysodia` dy,\n" +
                                            "`db_inspect_soil` so,\n" +
                                            "`db_inspect_solid` sd,\n" +
                                            "`db_inspect_water` wt\n" +
                                            "WHERE t.id=" + task_id +
                                            " AND c.task_id=t.id AND s.company_id=c.id\n" +
                                            "AND (\n" +
                                            "(a.sample_id=s.id AND a.process<4) OR\n" +
                                            "(dy.sample_id=s.id AND dy.process<4) OR\n" +
                                            "(so.sample_id=s.id AND so.process<4) OR\n" +
                                            "(sd.sample_id=s.id AND sd.process<4) OR\n" +
                                            "(wt.sample_id=s.id AND wt.process<4)\n" +
                                            ")").size();
                                    if (size == 0) {
                                        Task task = Task.taskDao.findById(task_id);
                                        result = result && task.set("process", ProcessKit.getTaskProcess("firstReview")).update();
                                    }
                                    result = result && inspectSoil.set("check_id", inspectSoilReview.get("id")).set("process", 4).update();
                                } else {
                                    result = result && inspectSoil.set("check_id", inspectSoilReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
                        case "solid":
                            InspectSoild inspectSoild = InspectSoild.inspectSoildDao.findById(getPara("id"));
                            if (inspectSoild != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectSoildReview inspectSoildReview = new InspectSoildReview();
                                result = result && inspectSoildReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("soil_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    int size = Task.taskDao.find("SELECT DISTINCT t.*\n" +
                                            "FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                            "`db_inspect_air` a,\n" +
                                            "`db_inspect_dysodia` dy,\n" +
                                            "`db_inspect_soil` so,\n" +
                                            "`db_inspect_solid` sd,\n" +
                                            "`db_inspect_water` wt\n" +
                                            "WHERE t.id=" + task_id +
                                            " AND c.task_id=t.id AND s.company_id=c.id\n" +
                                            "AND (\n" +
                                            "(a.sample_id=s.id AND a.process<4) OR\n" +
                                            "(dy.sample_id=s.id AND dy.process<4) OR\n" +
                                            "(so.sample_id=s.id AND so.process<4) OR\n" +
                                            "(sd.sample_id=s.id AND sd.process<4) OR\n" +
                                            "(wt.sample_id=s.id AND wt.process<4)\n" +
                                            ")").size();
                                    if (size == 0) {
                                        Task task = Task.taskDao.findById(task_id);
                                        result = result && task.set("process", ProcessKit.getTaskProcess("firstReview")).update();
                                    }
                                    result = result && inspectSoild.set("check_id", inspectSoildReview.get("id")).set("process", 4).update();
                                } else {
                                    result = result && inspectSoild.set("check_id", inspectSoildReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
                        case "air":
                            InspectAir inspectAir = InspectAir.inspectAir.findById(getPara("id"));
                            if (inspectAir != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectAirReview inspectAirReview = new InspectAirReview();
                                result = result && inspectAirReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("air_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    int size = Task.taskDao.find("SELECT DISTINCT t.*\n" +
                                            "FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                            "`db_inspect_air` a,\n" +
                                            "`db_inspect_dysodia` dy,\n" +
                                            "`db_inspect_soil` so,\n" +
                                            "`db_inspect_solid` sd,\n" +
                                            "`db_inspect_water` wt\n" +
                                            "WHERE t.id=" + task_id +
                                            " AND c.task_id=t.id AND s.company_id=c.id\n" +
                                            "AND (\n" +
                                            "(a.sample_id=s.id AND a.process<4) OR\n" +
                                            "(dy.sample_id=s.id AND dy.process<4) OR\n" +
                                            "(so.sample_id=s.id AND so.process<4) OR\n" +
                                            "(sd.sample_id=s.id AND sd.process<4) OR\n" +
                                            "(wt.sample_id=s.id AND wt.process<4)\n" +
                                            ")").size();
                                    if (size == 0) {
                                        Task task = Task.taskDao.findById(task_id);
                                        result = result && task.set("process", ProcessKit.getTaskProcess("firstReview")).update();
                                    }
                                    result = result && inspectAir.set("check_id", inspectAirReview.get("id")).set("process", 4).update();
                                } else {
                                    result = result && inspectAir.set("check_id", inspectAirReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
                        case "dysodia":
                            InspectDysodia inspectDysodia = InspectDysodia.inspectDysodiaDao.findById(getPara("id"));
                            if (inspectDysodia != null) {
                                User user = ParaUtils.getCurrentUser(getRequest());
                                InspectDysodiaReview inspectDysodiaReview = new InspectDysodiaReview();
                                result = result && inspectDysodiaReview.set("user_id", user.get("id")).set("create_time", ParaUtils.sdf2.format(new Date()))
                                        .set("result", getPara("result")).set("type", getPara("type"))
                                        .set("solid_id", getPara("id")).set("remark", getPara("remark")).save();
                                if (getParaToInt("result") == 1) {
                                    int size = Task.taskDao.find("SELECT DISTINCT t.*\n" +
                                            "FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                            "`db_inspect_air` a,\n" +
                                            "`db_inspect_dysodia` dy,\n" +
                                            "`db_inspect_soil` so,\n" +
                                            "`db_inspect_solid` sd,\n" +
                                            "`db_inspect_water` wt\n" +
                                            "WHERE t.id=" + task_id +
                                            " AND c.task_id=t.id AND s.company_id=c.id\n" +
                                            "AND (\n" +
                                            "(a.sample_id=s.id AND a.process<4) OR\n" +
                                            "(dy.sample_id=s.id AND dy.process<4) OR\n" +
                                            "(so.sample_id=s.id AND so.process<4) OR\n" +
                                            "(sd.sample_id=s.id AND sd.process<4) OR\n" +
                                            "(wt.sample_id=s.id AND wt.process<4)\n" +
                                            ")").size();
                                    if (size == 0) {
                                        Task task = Task.taskDao.findById(task_id);
                                        result = result && task.set("process", ProcessKit.getTaskProcess("firstReview")).update();
                                    }
                                    result = result && inspectDysodia.set("check_id", inspectDysodiaReview.get("id")).set("process", 4).update();
                                } else {
                                    result = result && inspectDysodia.set("check_id", inspectDysodiaReview.get("id")).set("process", 0).update();
                                }
                            } else return false;
                            break;
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
     * 保存一审结果
     */
    public void firstReview() {
        try {
            Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Task task = Task.taskDao.findById(getPara("task_id"));
                    if (task != null) {
                        RecordFirstReview recordFirstReview = new RecordFirstReview();
                        Map paraMap = getParaMap();
                        Boolean flag = true;
                        for (Object key : paraMap.keySet()) {
                            if (key.toString().trim().matches("^condition.*")) {
                                flag = flag && paraMap.get(key) == 1;
                            }
                            recordFirstReview.set(key.toString(), getPara(key.toString()));
                        }
                        Boolean result = recordFirstReview
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("create_time", ParaUtils.sdf.format(new Date()))
                                .set("flag",1)
                                .save();
                        if (!result) return false;
                        if (flag) {
                            //审核通过,进入二审
                            task.set("process", ProcessKit.getTaskProcess("secondReview"));
                            LoggerKit.addTaskLog(task.getInt("id"), "一审通过", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                        } else {
                            //审核拒绝，回到编辑状态,并将所有的process变成1（修改）状态
                            task.set("process", ProcessKit.getTaskProcess("lab"));
                            LoggerKit.addTaskLog(task.getInt("id"), "一审拒绝", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                            List<InspectAir> airList = InspectAir.inspectAir.find("SELECT DISTINCT t.* FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                    "`db_inspect_air` a\n" +
                                    "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND s.company_id=c.id AND a.sample_id=s.id");
                            List<InspectDysodia> dysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT DISTINCT t.* FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                    "`db_inspect_dysodia` a\n" +
                                    "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND s.company_id=c.id AND a.sample_id=s.id");
                            List<InspectWater> waterList = InspectWater.inspectWaterDao.find("SELECT DISTINCT t.* FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                    "`db_inspect_water` a\n" +
                                    "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND s.company_id=c.id AND a.sample_id=s.id");
                            List<InspectSoil> soilList = InspectSoil.inspectSoilDao.find("SELECT DISTINCT t.* FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                    "`db_inspect_soil` a\n" +
                                    "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND s.company_id=c.id AND a.sample_id=s.id");
                            List<InspectSoild> soildList = InspectSoild.inspectSoildDao.find("SELECT DISTINCT t.* FROM `db_task` t,`db_company` c,`db_sample` s,\n" +
                                    "`db_inspect_solid` a\n" +
                                    "WHERE t.id=" + task.get("id") + " AND c.task_id=t.id AND s.company_id=c.id AND a.sample_id=s.id");
                            for (InspectAir obj : airList) {
                                result = result && obj.set("process", 1).update();
                            }
                            for (InspectDysodia obj : dysodiaList) {
                                result = result && obj.set("process", 1).update();
                            }
                            for (InspectWater obj : waterList) {
                                result = result && obj.set("process", 1).update();
                            }
                            for (InspectSoil obj : soilList) {
                                result = result && obj.set("process", 1).update();
                            }
                            for (InspectSoild obj : soildList) {
                                result = result && obj.set("process", 1).update();
                            }
                        }
                        task.set("record_first_review_id", recordFirstReview.get("id"));
                        return result;
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 保存二审结果
     **/
    public void secondReview() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                RecordFirstReview recordFirstReview =new RecordFirstReview();
                Map paraMap = getParaMap();
                Boolean flag = true;
                for (Object key : paraMap.keySet()) {
                    if (key.toString().trim().matches("^condition.*")) {
                        flag = flag && paraMap.get(key) == 1;
                    }
                    recordFirstReview.set(key.toString(), getPara(key.toString()));
                }
                Boolean result = true;
                result = result && recordFirstReview.set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                        .set("create_time", ParaUtils.sdf.format(new Date()))
                        .set("flag",2)
                        .save();
                if (!result) return;
                if (flag) {
                    result = result && task.set("process", ProcessKit.getTaskProcess("report")).update();
                    LoggerKit.addTaskLog(task.getInt("id"), "二审通过", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                } else {
                    result = result && task.set("process", ProcessKit.getTaskProcess("firstReview")).update();
                    LoggerKit.addTaskLog(task.getInt("id"), "二审拒绝", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                }
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

}
