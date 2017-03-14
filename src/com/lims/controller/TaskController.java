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

    public String createIdentify() {
        String identify = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        identify = sdf.format(new Date());
        Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
        if (encode == null) {
            //数据库中没有第一条记录，则创建它
            Encode entry = new Encode();
            entry.set("contract_identify", 0).save();
            identify += String.format("%04d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify += String.format("%04d", identify_Encode);
        }
        return identify;
    }


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
                            param += " AND sample_type=1 AND process!=" + ProcessKit.getTaskProcess("create") + " ";
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
//        if (entry.get("contract_id") != null) {
//            //来自合同
//            Contract contract = Contract.contractDao.findById(entry.get("contract_id"));
//            if (contract != null) {
//                temp.put("id", entry.get("id"));
//                temp.put("contract_id", entry.get("contract_id"));
//                temp.put("process", entry.get("process"));
//                temp.put("create_time", entry.get("create_time"));
//                temp.put("creater", User.userDao.findById(entry.get("creater")));
//
//                temp.put("type", Type.typeDao.findById(contract.get("type")));
//                temp.put("identify", contract.get("identify"));
//                temp.put("client_unit", contract.get("client_unit"));
//                temp.put("client_code", contract.get("client_code"));
//                temp.put("client_tel", contract.get("client_tel"));
//                temp.put("client", contract.get("client"));
//                temp.put("client_fax", contract.get("client_fax"));
//                temp.put("client_address", contract.get("client_address"));
//                temp.put("name", contract.get("name"));
//                temp.put("aim", contract.get("aim"));
//                temp.put("type", entry.get("type") == null ? "" : Type.typeDao.findById(entry.get("type")));
//                temp.put("way", contract.get("way"));
//                temp.put("wayDesp", contract.get("wayDesp"));
//                temp.put("other", contract.get("other"));
//            }
//        } else {
//            //来自自定义
//
//        }
        for (String key : entry._getAttrNames()) {
            switch (key) {
                case "type":
                    temp.put("type", Type.typeDao.findById(entry.get(key)));
                    break;
                default:
                    temp.put(key, entry.get(key));
            }

        }
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
}
