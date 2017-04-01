package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Sample;
import com.lims.model.Task;
import com.lims.model.Type;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/3/29.
 */
public class SampleController extends Controller {
  /**  public void list() {
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
                        case "apply_sample":
                            param +=" AND category=1 ";
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
            Page<Sample> samplePage  = Sample.sampleDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_sample` " + param + " ORDER BY create_time DESC");
            List<Sample> sampleList = samplePage.getList();


            Map results = toJson(sampleList);
            results.put("currentPage", currentPage);
            results.put("totalPage", samplePage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<Sample> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Sample sample : entityList) {
                result.add(toJsonSingle(sample));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Sample entry) {
        Map temp = new HashMap();
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

    public static String Identify(int id) {
        try {
            Task task = Task.taskDao.findById(id);
            String identifier = task.get("identifier");
            identifier.toUpperCase();

            DecimalFormat df = new DecimalFormat("0000");
            String identify = "";
            String  last;



            return identifier;
        } catch (Exception e) {
            return null;
        }

    }**/


}
