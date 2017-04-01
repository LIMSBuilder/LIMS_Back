package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
import org.junit.Test;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/3/29.
 */
public class SampleController extends Controller {
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


    /**
     * 样品编号生成
     * 【小写字母变成大写字母】
     * 例如WS0001
     * self_identify:自送样  scene_identify:现场采样
     **/

    public static String createIdentify(int id, int flag, String prefix) {
        try {
            String identify = "";
            Task task = Task.taskDao.findById(id);
            if (flag == 0) {
                Type type = task.get("type");
                String identifier = type.get("identifier");
                identify += identifier.toUpperCase();//将数据库表中的type的identifer小写转大写
            } else {
                identify = prefix.toUpperCase();
            }
            int sample_type = task.get("sample_type");
            Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
            if (encode == null) {
//            数据库中没有第一条记录，则创建它
                Encode entry = new Encode();
                entry.set("contract_identify", 0);
                if (sample_type == 0) {
                    entry.set("self_identify", 1).set("scene_identify", 0);
                } else {
                    entry.set("self_identify", 0).set("scene_identify", 1);
                }
                entry.save();
                identify = identify + "-" + String.format("%03d", 1);
            } else {
                int identify_Encode = 0;
                if (sample_type == 0) {
                    //自送样
                    identify_Encode = (encode.get("self_identify") == null ? 0 : encode.getInt("self_identify")) + 1;
                    encode.set("self_identify", identify_Encode).update();
                } else {
                    //现场采样
                    identify_Encode = (encode.get("scene_identify") == null ? 0 : encode.getInt("scene_identify")) + 1;
                    encode.set("scene_identify", identify_Encode).update();
                }
                identify += String.format("%03d", identify_Encode);
            }
            return identify;
        } catch (Exception e) {
            return null;
        }

    }

    public void apply() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    int task_id = getParaToInt("task_id");
                    int item_id = getParaToInt("item_id");
                    int prefix = getParaToInt("prefix");
                    String prefix_text = getPara("prefix_text");
                    prefix_text = prefix_text.toUpperCase();
                    int count = getParaToInt("count");
                    for (int i = 0; i > count; i++) {
                        Sample sample = new Sample();
                        result = result && sample.set("identify", createIdentify(task_id, prefix, prefix_text)).save();
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
 * 样品申请数量
 * **/
//    public  void countProcess(){
//        try {
//            Map result =new HashMap();
//
//        }catch (Exception e)
//        {
//            renderError(500);
//        }
//    }

}
