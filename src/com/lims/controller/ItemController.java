package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Task;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/4/1.
 */
public class ItemController extends Controller {
    //样品编号列表
//    public void list() {
//        int rowCount = getParaToInt("rowCount");
//        int currentPage = getParaToInt("currentPage");
//        String condition_temp = getPara("condition");
//        Map condition = ParaUtils.getSplitCondition(condition_temp);
//        if (rowCount == 0) {
//            rowCount = ParaUtils.getRowCount();
//        }
//        String param = "  ";
//        Object[] keys = condition.keySet().toArray();
//        for (int i = 0; i < keys.length; i++) {
//            String key = (String) keys[i];
//            Object value = condition.get(key);
//            if (key.equals("process")) {
//                switch (value.toString()) {
//                    case "apply_sample":
//                        param += " AND i.process in (0,1)";
//                        break;
//                    case "before_apply_sample":
//                        param += " AND i.process = 0";
//                        break;
//                    case "after_apply_sample":
//                        param += " AND i.process = 1";
//                        break;
//                }
//                continue;
//            }
//            if (key.equals("keyWords")) {
//                param += (" AND ( t.identify ='" + value + "' OR t.name like \"%" + value + "%\" OR t.client_unit like \"%" + value + "%\")");
//                continue;
//            }
//        }
//        User user = ParaUtils.getCurrentUser(getRequest());
//        Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT DISTINCT t.*", "FROM `db_task` t,`db_contract_item` i,`db_contract` c,`db_item_join_user` u  WHERE ((i.task_id=t.id AND t.process=2 AND t.sample_type=1) OR (i.task_id is NULL AND t.identify=c.identify AND t.process=2 AND t.sample_type=1)) AND (i.charge_id=" + user.get("id") + " OR (i.id=u.contract_item_id AND u.join_id=" + user.get("id") + "))" + param);
//        List<Task> taskList = taskPage.getList();
//        Map results = toJson(taskList);
//        results.put("currentPage", currentPage);
//        results.put("totalPage", taskPage.getTotalPage());
//        results.put("rowCount", rowCount);
//        results.put("condition", condition_temp);
//        renderJson(results);
//
//    }

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
        temp.put("id", entry.get("id"));
        temp.put("name", entry.get("name"));
        temp.put("create_time", entry.get("create_time"));
        temp.put("client_unit", entry.get("client_unit"));
        temp.put("identify", entry.get("identify"));
        temp.put("process", entry.get("process"));
        temp.put("sample_type", entry.get("sample_type"));
        return temp;
    }



}

