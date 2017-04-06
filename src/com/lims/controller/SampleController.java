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
                    Sample sample = new Sample();
                    for (int i = 0; i > count; i++) {

                        result = result && sample.set("identify", createIdentify(task_id, prefix, prefix_text)).save();
                    }
                    LoggerKit.addSampleLog(sample.getInt("id"), "申请编号", ParaUtils.getCurrentUser(getRequest()).getInt("id"));

                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
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

    public Map toJsonSingle(Sample sample) {
        Map<String, Object> types = new HashMap<>();
        types.put("id", sample.getInt("id"));
        types.put("identify", sample.get("identify"));
        types.put("category", sample.get("category"));
        types.put("name", sample.get("name"));
        types.put("character", sample.get("character"));
        types.put("isbalance", sample.get("isbalance"));
        types.put("task_id", sample.get("task_id"));
        types.put("item_id", sample.get("item_id"));
        types.put("create_time", sample.get("create_time"));
        types.put("creater", sample.get("creater"));
        return types;
    }

    public void findByTask() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                List<Sample> sampleList = Sample.sampleDao.find("select * from `db_sample` where task_id =" + getPara(task_id));
                Map results = toJson(sampleList);
                renderJson(results);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void fingByItem() {
        try {
            int item_id = getParaToInt("item_id");
            ItemProject itemProject = ItemProject.itemprojectDao.findById(item_id);
            if (itemProject != null) {
                List<Sample> sampleList = Sample.sampleDao.find("select * from `db_sample` where item_id =" + getPara(item_id));
                Map results = toJson(sampleList);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 申请样品日志
     ***/
    public void applyLog() {
        try {
            int item_id = getParaToInt("item_id");
            Log log = Log.logDao.findById(item_id);
            if (log != null) {
                List<Log> logList = Log.logDao.find("select * from `db_log`  where item_id =" + log.get("item_id") + "orderby create_time  DESC");
                Map results = toLogJson(logList);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toLogJson(List<Log> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Log log : entityList) {
                result.add(toLogJsonSingle(log));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toLogJsonSingle(Log log) {
        Map<String, Object> types = new HashMap<>();
        types.put("user_id", log.getInt("user_id"));
        types.put("msg", log.get("msg"));
        types.put("create_time", log.get("create_time"));

        return types;
    }

    /**
     * 统计待申请的任务数
     **/
    public void countProcess() {
        try {
            User user = new User();
            int count = Task.taskDao.find("SELECT DISTINCT t.*", "FROM `db_task` t,`db_contract_item` i,`db_contract` c,`db_item_join_user` u  WHERE ((i.task_id=t.id AND t.process=2 AND t.sample_type=1) OR (i.task_id is NULL AND t.identify=c.identify AND t.process=2 AND t.sample_type=1)) AND (i.charge_id=" + user.get("id") + " OR (i.id=u.contract_item_id AND u.join_id=" + user.get("id") + ") and  i.process = 0)").size();
            Map temp = new HashMap();
            temp.put("beforeApply", temp);
            renderJson(temp);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 提供自送样样品信息保存接口
     **/
    public void selfCreate() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                String identify = createIdentify(task_id, getParaToInt("prefix"), getPara("prefix_text"));
                Sample sample = new Sample();
                sample.set("identify", identify).set("name", getPara("name")).set("character", getPara("character")).set("condition", getPara("condition")).set("process", ProcessKit.getSampleProcess("create"));
                renderJson(sample.save() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 获取当前task_id的所有Sample信息
     **/
    public void selfSampleList() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_sample` WHERE task_id =" + task_id);
                Map results = toJson(sampleList);
                renderJson(results);
            } else renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }

}
