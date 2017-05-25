package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.T;

import java.util.*;

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
                total.put("package", task.get("package"));
                total.put("receive_type", task.get("receive_type"));
                total.put("additive", task.get("additive"));
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
            List<Sample> sampleList1 = Sample.sampleDao.find("SELECT s.* FROM `db_task` t,`db_company` c,`db_sample` s \n" +
                    "WHERE t.id=" + task_id + " AND c.task_id=t.id AND s.company_id=c.id ORDER BY s.identify");
            for (Sample sample : sampleList1) {
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
                m.put("projects", s);
                m.put("samples", back.get(s));
                List<Map> de = new ArrayList();
                for (int i = 0; i < back.get(s).size(); i++) {
                    List<Description> descriptionList = Description.descriptionDao.find("SELECT * FROM `db_sample_deseription` WHERE sample_id =" + ((Sample) (back.get(s).get(i))).get("id"));
                    for (Description description : descriptionList) {
                        de.add(description.toJsonSingle());
                    }

                }
                m.put("item", de);

                mapList.add(m);

            }
            total.put("items", mapList);


            renderJson(total);

        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 保存样品交接表
     **/
    public void saveReceipt() {
        try {
            Integer[] projectlist = getParaValuesToInt("samplesID[]");
            String saveCharacter = getPara("saveCharacter");
            String saveState = getPara("saveState");
            Boolean result = true;
            for (int id : projectlist) {
                Sample sample = Sample.sampleDao.findById(id);
                if (sample != null) {
                    sample.set("process", 3);
                    result = result && sample.update();
                }
                Description description = Description.descriptionDao.findFirst("select * from `db_sample_deseription` where sample_id =" + id);
                if (description == null) {
                    Description description1 = new Description();
                    description1.set("sample_id", id)
                            .set("saveCharacter", saveCharacter)
                            .set("saveState", saveState)
                            .set("process", 1);

                    result = result && description1.save();
                } else {
                    result = result && Description.descriptionDao.deleteById(description.get("id"));
                    Description description2 = new Description();
                    description2.set("sample_id", id)
                            .set("saveCharacter", saveCharacter)
                            .set("saveState", saveState)
                            .set("process", 1);
                    result = result && description2.save();


                }

            }

            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 保存记录表
     **/
    public void saveAll() {
        try {
            int task_id = getParaToInt("id");
            Task task = Task.taskDao.findById(task_id);
            Boolean result = true;
            if (task != null) {
                int size = Sample.sampleDao.find("SELECT s.* FROM `db_company` c,`db_sample` s WHERE c.task_id=" + task_id + "  AND s.company_id =c.id AND s.process!=3").size();
                if (size != 0) {
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                } else {
                    User user = ParaUtils.getCurrentUser(getRequest());
                    task.set("package", getPara("package"))
                            .set("receive_type", getPara("receive_type"))
                            .set("additive", getPara("additive"))
                            .set("sample_receiver", user.get("id"))
                            .set("receive_time", ParaUtils.sdf2.format(new Date()));
                    result = result && task.update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                }
            }

        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 返回可以做相对应项目人员名单
     **/
    public void labUserList() {
        int itemId = getParaToInt("item_id");
        List<Certificate> certificateList = Certificate.certificateDao.find("SELECT c.* FROM `db_lab_certificate` c ,`db_item_project` p WHERE  c.project_id =p.project_id  AND p.id = " + itemId);
        if (certificateList != null) {
            renderJson(toJson(certificateList));

        }

    }

    public Map toJson(List<Certificate> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Certificate certificate : entityList) {
                result.add(toJsonSingle(certificate));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(Certificate certificate) {
        Map<String, Object> ce = new HashMap<>();
        ce.put("id", certificate.get("id"));
        ce.put("name", User.userDao.findById(certificate.get("lab")).get("name"));
        return ce;
    }
}
    /***
     * 将同一个任务书中分析项目相同的合并
     * **/
//    public void projectList() {
//
//        try {
//            int task_id = getParaToInt("id");
//            Task task = Task.taskDao.findById(task_id);
//            Map total = new HashMap();
//            if (task != null) {
//                Map<Object, List> back = new HashMap<>();
//                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p\n" +
//                        "WHERE t.id=" + task_id + " AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id");
//                for (ItemProject itemProject : itemProjectList)
//                {
//                    if(back.containsKey(itemProject)){
//                        List items =back.get(itemProject);
//                        Map b=new HashMap();
//                        b.put("name",MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("name"));
//                       List de =new ArrayList();
//                        List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_task` t,`db_company` c,`db_sample` s,`db_sample_project` p, WHERE t.id ='" + task_id + "' AND c.task_id = t.id AND s.company_id =c.id AND s.id= p.sample_id AND p.item_project_id= "+itemProject.get("id"));
//                        for (Sample sample:sampleList){
//                            de.add(sample.toSimpleJson());
//                        }
//                        b.put("item",de);
//                        items.add(b);
//                    }
//                    else{
//                        List items =new ArrayList();
//                        Map b=new HashMap();
//                        b.put("name",MonitorProject.monitorProjectdao.findById(itemProject.get("project_id")).get("name"));
//                        List de =new ArrayList();
//                        List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_task` t,`db_company` c,`db_sample` s,`db_sample_project` p, WHERE t.id ='" + task_id + "' AND c.task_id = t.id AND s.company_id =c.id AND s.id= p.sample_id AND p.item_project_id= "+itemProject.get("id"));
//                        for (Sample sample:sampleList){
//                            de.add(sample.toSimpleJson());
//                        }
//                        b.put("item",de);
//                        items.add(b);
//                        back.put(itemProject,items);
//                    }
//
//                }
//
//            }
//            renderJson(total);
//        } catch (Exception e) {
//            renderError(500);
//        }
//    }
//}
