package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
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

                 for (int i=0;i<back.get(s).size();i++){
                     Map temp =new HashMap();
                     List<Description> descriptionList = Description.descriptionDao.find("SELECT * FROM `db_sample_deseription` WHERE sample_id =" + ((Sample)(back.get(s).get(i))).get("id"));
                     if (descriptionList.size() != 0) {
                         temp.put("id", descriptionList.get(0).get("id"));
                         temp.put("process", descriptionList.get(0).get("process"));
                         temp.put("sample_id", descriptionList.get(0).get("sample_id"));
                         temp.put("saveState", descriptionList.get(0).get("saveState"));
                         temp.put("saveCharacter", descriptionList.get(0).get("saveCharacter"));
                         m.put("item",temp);
                     }
                 }


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
                Sample sample =Sample.sampleDao.findById(id);
                if(sample!=null){
                    sample.set("process",3);
                    result =result &&sample.update();
                }
                Description description = new Description();
                description.set("sample_id", id)
                        .set("saveCharacter", saveCharacter)
                        .set("saveState", saveState)
                          .set("process",1);

                result = result && description.save();
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
                int size = Sample.sampleDao.find("SELECT s.* FROM `db_company` c,`db_sample` s WHERE c.task_id=" + task_id + "  AND s.company_id =c.id AND s.process!=2").size();
                if (size != 0) {
                    renderJson(RenderUtils.CODE_NOTEMPTY);
                }
               else {
                    User user = ParaUtils.getCurrentUser(getRequest());
                    task.set("package", getPara("package"))
                            .set("receive_type", getPara("receive_type"))
                            .set("additive", getPara("additive"))
                            .set("sample_receiver", user.get("id"))
                            .set("process", ProcessKit.getTaskProcess("lab"))
                            .set("receive_time", ParaUtils.sdf2.format(new Date()));
                    result = result && task.update();
                }
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }
    /***
     * 修改
     * */
    public void changeReceipt() {
        try {
            Integer[] projectlist = getParaValuesToInt("samplesID[]");
            String saveCharacter = getPara("saveCharacter");
            String saveState = getPara("saveState");
            Boolean result = true;
            for (int id : projectlist) {
                Description description = new Description();
                description.set("sample_id", id)
                        .set("saveCharacter", saveCharacter)
                        .set("saveState", saveState);
                result = result && description.update();
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 返回可以做相对应项目人员名单
     * **/
    public  void labUserList(){
        int itemId =getParaToInt("item_id");
        List<Certificate> certificateList =Certificate.certificateDao.find("SELECT c.* FROM `db_lab_certificate` c ,`db_item_project` p WHERE  c.project_id =p.project_id  AND p.id = "+itemId);
       if(certificateList!=null){
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
        ce.put("name",User.userDao.findById(certificate.get("lab")).get("name"));
        return ce;
    }

    /***
     * 将同一个任务书中分析项目相同的合并
     * **/
    public void projectList() {

        try {
            int task_id =getParaToInt("id");
            Task task =Task.taskDao.findById(task_id);
            if (task!=null){
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p\n" +
                        "WHERE t.id=" + task_id + " AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id");
                List result=new ArrayList();
                for (ItemProject itemProject:itemProjectList){
                    Map temp =new HashMap();
                    temp = itemProject.toJsonSingle();


                }
                renderJson(result);
            }
            renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }
}
