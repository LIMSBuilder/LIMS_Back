package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.lims.model.*;
import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/5/19.
 */
public class LabController extends Controller {
    /**
     * 样品交接表
     **/
    public void selfList() {
        try {
            int task_id = getParaToInt("id");
            Map total = new HashMap();
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                total.put("sample_type", task.get("sample_type"));
                total.put("client_unit", task.get("client_unit"));
                total.put("task_identify", task.get("identify"));
                total.put("type", Type.typeDao.findById(task.get("type")));
                total.put("time", task.get("sample_time"));
                total.put("sample_creater", User.userDao.findById(task.get("sample_creater")));
                List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_task` t,`db_company` c,`db_sample` s \n" +
                        "WHERE t.id=" + task_id + " AND c.task_id=t.id AND s.company_id=c.id ORDER BY s.identify");
                total.put("count", sampleList.size());
                if (sampleList.size() != 0) {
                    String first = sampleList.get(0).getStr("identify");
                    String last = sampleList.get(sampleList.size() - 1).getStr("identify");
                    total.put("firstIdentify", first);
                    total.put("lastIdentify", last);
//                    total.put("identify", first + "~" + last);
                }


            }

            Map<List, List> back = new HashMap<>();
            List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_task` t,`db_company` c,`db_sample` s \n" +
                    "WHERE t.id=" + task_id + " AND c.task_id=t.id AND s.company_id=c.id ORDER BY s.identify");
            for (Sample sample : sampleList) {
                List<MonitorProject> records = MonitorProject.monitorProjectdao.find("SELECT m.* FROM `db_sample` s,`db_sample_project` p,`db_item_project` i,`db_monitor_project` m\n" +
                        "WHERE s.id=" + sample.get("id") + " AND p.sample_id=s.id AND p.item_project_id=i.id AND i.project_id=m.id");
                List<Map> b = new ArrayList<>();
                for (MonitorProject monitorProject : records) {
                    b.add(monitorProject.toJsonSingle());
                }
                if (back.containsKey(b)) {
                    //当前已经存在这分析项目的组合
                    back.get(b).add(sample);
                } else {
                    List t = new ArrayList();
                    t.add(sample);
                    back.put(b, t);
                }
            }
            List<Map> mapList = new ArrayList<>();
            for (List s : back.keySet()) {
                Map m = new HashMap();
                m.put("projects",s);
                m.put("samples",back.get(s));
                mapList.add(m);
            }


            total.put("items", mapList);


//            List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p\n" +
//                    "WHERE t.id=" + task_id + " AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id");
//            List result = new ArrayList();
//            for (ItemProject itemProject : itemProjectList) {
//                Map item = new HashMap();
//                item.put("name", Element.elementDao.findById(MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("element_id")).get("name"));
//                item.put("name2", MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("name"));
//                int count1 = Sample.sampleDao.find("SELECT s.* FROM `db_sample` s ,`db_sample_project` p WHERE p.sample_id = s.id AND p.item_project_id=" + itemProject.get("id")).size();
//                item.put("cou", count1);
//                if (task.get("sample_type") == 0) {
//                    List char1 = new ArrayList<>();
//                    List<Sample> sampleList1 = Sample.sampleDao.find("SELECT s.* FROM `db_sample` s ,`db_sample_project` p WHERE   s.id=p.sample_id AND p.item_project_id =" + itemProject.get("id"));
//                    for (Sample sample : sampleList1) {
//                        Map ch = new HashMap();
//                        ch.put("character", sample.get("character"));
//                        char1.add(ch);
//                        item.put("characterList", char1);
//                    }
//                }
//                result.add(item);
//            }
//            total.put("items", result);
            renderJson(total);

        } catch (Exception e) {
            renderError(500);
        }
    }


}
