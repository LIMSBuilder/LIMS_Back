package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;
import com.lims.utils.LoggerKit;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
import org.junit.Test;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by qulongjun on 2017/3/10.
 */
public class TaskController extends Controller {

    /**
     * 自定义任务书创建
     */
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Map paraMaps = getParaMap();
                    Task task = new Task();
                    Boolean result = true;
                    for (Object key : paraMaps.keySet()) {
                        switch (key.toString()) {
                            case "id":
                                break;//不知道为什么会传一个id过来，待观察
                            default:
                                if (key.toString().indexOf("item") != -1) {
                                    continue;
                                }
                                task.set(key.toString(), ((String[]) paraMaps.get(key))[0]);
                        }

                    }
                    User user = ParaUtils.getCurrentUser(getRequest());
                    task.set("identify", createIdentify()).set("sample_type", getPara("sample_type")).set("create_time", ParaUtils.sdf.format(new Date())).set("creater", user.get("id")).set("process", ProcessKit.getTaskProcess("create"));
                    result = result && task.save();
                    String[] items = getParaValues("project_items[]");
                    for (String item : items) {
                        Map temp = Jackson.getJson().parse(item, Map.class);
                        Contractitem contractitem = new Contractitem();
                        List points = (ArrayList) temp.get("point");
                        String point = "";
                        if (points != null) {
                            for (int i = 0; i < points.size(); i++) {
                                point += points.get(i);
                                if (i != points.size() - 1) {
                                    point += ",";
                                }
                            }
                        }

                        result = result && contractitem.set("element", ((Map) temp.get("element")).get("id")).set("company", temp.get("company")).set("point", point).set("task_id", task.get("id")).set("other", temp.get("other")).set("is_package", temp.get("is_package")).save();
                        if (!result) break;
                        List<Map> projectList = (ArrayList) temp.get("project");
                        if (projectList != null) {
                            for (int m = 0; m < projectList.size(); m++) {
                                Map project = projectList.get(m);
                                ItemProject entry = new ItemProject();
                                entry.set("item_id", contractitem.get("id")).set("project_id", project.get("id"));
                                result = result && entry.save();
                                if (!result) break;
                            }
                        }
                        if (!result) break;
                    }
                    LoggerKit.addTaskLog(task.getInt("id"), "创建了任务", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据合同创建
     */
    public void createByContract() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Task task = new Task();
                    Contract contract = Contract.contractDao.findById(getPara("contract_id"));
                    if (contract != null) {
                        Boolean result = task
                                .set("sample_type", getPara("sample_type"))
                                .set("contract_id", getPara("contract_id"))
                                .set("process", ProcessKit.getTaskProcess("create"))
                                .set("create_time", ParaUtils.sdf.format(new Date()))
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("identify", contract.get("identify"))
                                .set("client_unit", contract.get("client_unit"))
                                .set("client_code", contract.get("client_code"))
                                .set("client_tel", contract.get("client_tel"))
                                .set("client", contract.get("client"))
                                .set("client_fax", contract.get("client_fax"))
                                .set("client_address", contract.get("client_address"))
                                .set("name", contract.get("name"))
                                .set("aim", contract.get("aim"))
                                .set("type", contract.get("type"))
                                .set("way", contract.get("way"))
                                .set("wayDesp", contract.get("wayDesp"))
                                .set("other", contract.get("other"))
                                .save();
                        result = result && contract.set("process", ProcessKit.getContractProcess("finish")).update();
                        return result;
                    } else
                        return false;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

//    public String createIdentify() {
//        String identify = "";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
//        identify = sdf.format(new Date());
//        Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
//        if (encode == null) {
//            数据库中没有第一条记录，则创建它
//            Encode entry = new Encode();
//            entry.set("contract_identify", 0).save();
//            identify += String.format("%04d", 1);
//        } else {
//            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
//            encode.set("contract_identify", identify_Encode).update();
//            identify += String.format("%04d", identify_Encode);
//        }
//        return identify;
//    }


    public void list() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            if (rowCount == 0) {
                rowCount = ParaUtils.getRowCount();
            }
            String param = " WHERE 1=1 ";
            Object[] keys = condition.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                Object value = condition.get(key);
                if (key.equals("process")) {
                    switch (value.toString()) {
                        case "before_dispath":
                            param += " AND sample_type=1 AND process=" + ProcessKit.getTaskProcess("create") + " ";
                            break;
                        case "after_dispath":
                          //  param += " AND sample_type=1 AND process!=" + ProcessKit.getTaskProcess("create") + " ";
                            param += " AND sample_type=1 AND process != " + ProcessKit.getTaskProcess("create") + " AND process !=" + ProcessKit.getTaskProcess("stop") + " ";
                            break;
                        case "total_dispatch":
                            param += "AND sample_type=1 AND process !="+ProcessKit.getTaskProcess("stop");
                            break;
                        case "apply_sample":
                            param += " AND sample_type=1 AND process=" + ProcessKit.getTaskProcess("dispatch") + " ";
                            break;

                        default:
                            param += " AND " + key + " = " + value;
                    }
                    continue;
                }
                if (key.equals("keyWords")) {
                    param += (" AND ( identify ='" + value + "' OR name like \"%" + value + "%\" OR client_unit like \"%" + value + "%\")");
                    continue;
                }
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_task` " + param + " ORDER BY create_time DESC");
            List<Task> taskList = taskPage.getList();


            Map results = toJson(taskList);
            results.put("currentPage", currentPage);
            results.put("totalPage", taskPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }


    public Map toJson(List<Task> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Task task : entityList) {
                result.add(toJsonSingle(task));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Task entry) {
        Map temp = new HashMap();
//        for (String key : entry._getAttrNames()) {
//            switch (key) {
//                case "type":
//                    temp.put("type", Type.typeDao.findById(entry.get(key)));
//                    break;
//                default:
//                    temp.put(key, entry.get(key));
//            }
//
//        }
        temp.put("id", entry.get("id"));
        temp.put("name", entry.get("name"));
        temp.put("create_time", entry.get("create_time"));
        temp.put("client_unit", entry.get("client_unit"));
        temp.put("identify", entry.get("identify"));
        temp.put("process", entry.get("process"));
        temp.put("sample_type", entry.get("sample_type"));

        return temp;
    }


    public void getItems() {
        try {
            int id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(id);
            if (task.get("contract_id") != null) {
                Contract contract = Contract.contractDao.findById(task.get("contract_id"));
                if (contract != null) {
                    renderJson(contract.getItems());
                } else renderJson(RenderUtils.CODE_EMPTY);
            } else {
                if (task != null) {
                    renderJson(task.getItems());
                } else renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delivery() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    String jsons = getPara("result");//前端传数组过来
                    String task_id = getPara("id");
                    List<Map> temp = Jackson.getJson().parse(jsons, List.class);
                    Boolean result = true;
                    for (int i = 0; i < temp.size(); i++) {
                        Map entry = temp.get(i);
                        int item_id = (int) entry.get("id");
                        int charge = (int) entry.get("charge");
                        List<Integer> belongs = (ArrayList) entry.get("belongs");
                        Contractitem contractitem = Contractitem.contractitemdao.findById(item_id);
                        result = result && contractitem.set("charge_id", charge).update();
                        if (!result) return false;
                        for (int j = 0; j < belongs.size(); j++) {
                            int user = belongs.get(j);
                            ItemJoin itemJoin = new ItemJoin();
                            itemJoin.set("contract_item_id", item_id);
                            itemJoin.set("join_id", user);
                            result = result && itemJoin.save();
                            if (!result) break;
                        }
                    }
                    if (!result) return false;
                    Task task = Task.taskDao.findById(task_id);
                    result = result && task.set("process", ProcessKit.TaskMap.get("dispatch")).update();
                    LoggerKit.addTaskLog(task.getInt("id"), "派遣任务", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void countProcess() {
//        try {
//            Map temp = ProcessKit.TaskMap;
//            Map result = new HashMap();
//            for (Object key : temp.keySet()) {
//                int count = Task.taskDao.find("select * from `db_task` where process =" + temp.get(key) +  "'and  sample_type=1'" ).size();
//                result.put(count, key);
//            }
//            result.put("total", Task.taskDao.find("select * from `db_task` where sample_type=1").size());
//            renderJson(result);
//        } catch (Exception e) {
//            renderError(500);
//        }
        try {
            int count = Task.taskDao.find("SELECT * FROM `db_task` WHERE process =" + ProcessKit.TaskMap.get("create") + " AND sample_type=1").size();
            Map temp = new HashMap();
            temp.put("create", count); //待任务派遣个数
            renderJson(temp);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void countTotal() {
        try {
            Map result = new HashMap();
            result.put("total", Task.taskDao.find("select * from `db_task`").size());
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void monitorItem() {
        try {
            int id = getParaToInt("id");
            List temp = new ArrayList();
            List<ItemProject> projectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project` WHERE item_id=" + id);
//            List<Map> mapList = new ArrayList<>();
//            for (ItemProject project : projectList) {
//                Map t = new HashMap();
//                t.put("id", project.get("id"));
//                t.put("project", MonitorProject.monitorProjectdao.findById(project.get("project_id")));
//                t.put("item_id", project.get("item_id"));
//                mapList.add(t);
//            }
//            result.put("project", mapList);
            for (ItemProject itemProject : projectList) {
                temp.add(itemProject.toJsonSingle());
            }
            renderJson(temp);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void taskDetails()

    {
        try {
            int id = getParaToInt("id");
            Task task = Task.taskDao.findById(id);
            if (task != null) {
                renderJson(toTaskDetailJSON(task));
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toTaskDetailJSON(Task entry) {
        Map temp = new HashMap();
        for (String key : entry._getAttrNames()) {
            switch (key) {
                case "trustee":
                    temp.put("trustee", entry.get(key) == null ? "" : User.userDao.findById(entry.get(key)).toSimpleJson());
                    break;
                case "type":
                    temp.put("type", entry.get(key) == null ? "" : Type.typeDao.findById(entry.get(key)));
                    break;
                default:
                    temp.put(key, entry.get(key));
            }
        }
        return temp;
    }

    public void stopTask() {
        try {
            boolean result = Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
//                    int Result = getParaToInt("contract_result");
                    Task task = Task.taskDao.findById(id);
                    Boolean result = true;
//                    Boolean contractResult = true;
                    if (task != null) {
                        result = task.set("process", -2).update();
//                        Contract contract = Contract.contractDao.findFirst("select * from `db_contrat` where  id=" + task.get("contract_id"));
//                        if (contract != null) {
//                            switch (Result) {
//                                case 0:
//                                    contractResult = contract.set("process", ProcessKit.ContractMap.get("review")).update();
//                                    break;
//                                case 1:
//                                    contractResult = contract.set("process", ProcessKit.ContractMap.get("stop")).update();
//                                    break;
//                                case 2:
//                                    break;
//                            }
//                        }
                    }
                    LoggerKit.addTaskLog(task.getInt("id"), "中止了任务", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void deleteTask() {
        try {
            int id = getParaToInt("id");
            boolean result = Task.taskDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 任务编号生成
     * <p>
     * 年份+ - + 三位流水编号，如 2017-001  2017-002  以此类推
     * <p>
     * 需要考虑：年份更新需要自动更新当前年份，且将流水号恢复初始值1号
     **/
    public String createIdentify() {
        String identify = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        identify = sdf.format(new Date());
        Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
        if (encode == null) {
//            数据库中没有第一条记录，则创建它
            Encode entry = new Encode();
            entry.set("contract_identify", 1).set("self_identify", 0).set("scene_identify", 0).save();
            identify = identify + "-" + String.format("%03d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify = identify + "-" + String.format("%03d", identify_Encode);
        }
        return identify;
    }



}
