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
                            .set("receive_time", ParaUtils.sdf2.format(new Date())).set("flag2", 1);
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
        int itemId = getParaToInt("project_id");
        List<Certificate> certificateList = Certificate.certificateDao.find("SELECT * FROM `db_lab_certificate` WHERE project_id =" + itemId);
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
        ce.put("lab", certificate.get("lab"));
        ce.put("name", User.userDao.findById(certificate.get("lab")).get("name"));
        return ce;
    }

    /***
     * 任务派遣
     * **/

    public void delivery() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            Map total = new HashMap();
            if (task != null) {
                total.put("task_id", task.get("id"));
                total.put("task_identify", task.get("identify"));
                List<MonitorProject> monitorProjectList = MonitorProject.monitorProjectdao.find("SELECT DISTINCT  m.* FROM`db_task`t, `db_company` c,`db_item` i,`db_item_project` p ,`db_monitor_project` m\n" +
                        "WHERE t.id=" + task_id + " AND c.task_id = t.id AND i.company_id=c.id AND p.item_id=i.id AND m.id = p.project_id");
                List result = new ArrayList();
                for (MonitorProject monitorProject : monitorProjectList) {
                    Map temp = new HashMap();
                    temp.put("project", monitorProject.toJsonSingle());
                    List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_company` c,`db_item` i,`db_item_project` p  WHERE p.project_id = '" + monitorProject.get("id") + "'AND c.task_id= '" + task_id + "'AND i.company_id =c.id AND p.item_id = i.id");
                    List<Map> mapList = new ArrayList<>();
                    for (ItemProject itemProject : itemProjectList) {
                        mapList.add(itemProject.toJsonSingle());
                    }
                    temp.put("item", mapList);
                    result.add(temp);
                }


                total.put("items", result);
            }
            renderJson(total);

        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 保存分析者
     **/
    public void saveAnalysis() {
        try {
            int task_id = getParaToInt("task_id");
            int project_id = getParaToInt("project_id");
            Task task = Task.taskDao.findById(task_id);
            Boolean result = true;
            if (task != null) {
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM`db_task`t, `db_company` c,`db_item` i,`db_item_project` p \n" +
                        "WHERE t.id=" + task_id + " AND c.task_id = t.id AND i.company_id=c.id AND p.item_id=i.id AND p.project_id=" + project_id);
                for (ItemProject itemProject : itemProjectList) {
                    result = result && itemProject.set("labFlag", 1).update();
                    List<Inspect> inspectList = Inspect.inspectDao.find("SELECT * FROM `db_inspect` WHERE item_project_id=" + itemProject.get("id"));
                    for (Inspect inspect : inspectList) {
                        result = result && inspect.set("analyst", getPara("user_id")).set("process", 1).set("checker", getPara("checker_id")).set("reviewer", getPara("reviewer_id")).update();
                    }

                }
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /***
     * 查看分派任务流程
     * */
    public void getInfo() {
        try {
            int item_id = getParaToInt("id");
            ItemProject itemProject = ItemProject.itemprojectDao.findById(item_id);
            Map total = new HashMap();
            Task task = Task.taskDao.findFirst("SELECT t.* FROM `db_item_project` p ,`db_item` i,`db_company` c,`db_task` t\n" +
                    "WHERE p.id =" + item_id + " AND i.id=p.item_id AND c.id =i.company_id AND t.id =c.task_id");
            List<RecordFirstReview> recordFirstReviewList = RecordFirstReview.recordFirstReviewDao.find("SELECT * FROM `db_record_first_review` WHERE task_id= '" + task.get("id") + "' AND flag=1");
            List<Map> results = new ArrayList<>();
            for (RecordFirstReview recordFirstReview : recordFirstReviewList) {
                results.add(recordFirstReview.toJSON());
            }
            total.put("firstReview", results);
            List<RecordFirstReview> recordFirstReviewList1 = RecordFirstReview.recordFirstReviewDao.find("SELECT * FROM `db_record_first_review` WHERE task_id= '" + task.get("id") + "' AND flag=2");
            List<Map> result = new ArrayList<>();
            for (RecordFirstReview recordFirstReview : recordFirstReviewList1) {
                result.add(recordFirstReview.toJSON());
            }
            total.put("secondReview", result);
            List<Inspect> inspectList = Inspect.inspectDao.find("SELECT * FROM `db_inspect` WHERE item_project_id =" + item_id);
            List<Map> inspcct = new ArrayList<>();
            for (Inspect inspect : inspectList) {
                List inspectJson = new ArrayList();
                inspcct.add(inspect.toSingleJson());
                total.put("inspect", inspcct);
                switch (inspect.getStr("type")) {
                    case "water":
                        List<InspectWater> inspectWaterList = InspectWater.inspectWaterDao.find("SELECT * FROM `db_inspect_water` WHERE inspect_id=" + inspect.get("id"));
                        for (InspectWater inspectWater : inspectWaterList) {
                            inspectJson.add(inspectWater.toJSON());
                            List waterReview = new ArrayList();
                            List<InspectWaterReview> inspectWaterReviewList = InspectWaterReview.inspectWaterReviewDao.find("SELECT * FROM `db_inspect_water_review` WHERE water_id=" + inspectWater.get("id"));
                            for (InspectWaterReview inspectWaterReview : inspectWaterReviewList) {
                                waterReview.add(inspectWaterReview.toJSON());

                            }
                            total.put("waterReview", waterReview);
                        }
                        break;
                    case "soil":
                        List<InspectSoil> inspectSoilList = InspectSoil.inspectSoilDao.find("SELECT * FROM `db_inspect_soil` WHERE inspect_id=" + inspect.get("id"));
                        for (InspectSoil inspectSoil : inspectSoilList) {
                            inspectJson.add(inspectSoil.toJSON());
                            List soilReview = new ArrayList();
                            List<InspectSoilReview> inspectSoilReviewList = InspectSoilReview.inspectSoilReviewDao.find("SELECT * FROM `db_inspect_soil_review` WHERE soil_id=" + inspectSoil.get("id"));
                            for (InspectSoilReview inspectSoilReview : inspectSoilReviewList) {
                                soilReview.add(inspectSoilReview.toJSON());

                            }
                            total.put("soilReview",soilReview);
                        }
                        break;
                    case "solid":
                        List<InspectSoild> inspectSoilds = InspectSoild.inspectSoildDao.find("SELECT * FROM `db_inspect_solid` WHERE inspect_id=" + inspect.get("id"));
                        for (InspectSoild inspectSoild : inspectSoilds) {
                            inspectJson.add(inspectSoild.toJSON());
                            List soildReview = new ArrayList();
                            List<InspectSoildReview> inspectSoildReviewList = InspectSoildReview.inspectSoildReviewdao.find("SELECT * FROM `db_inspect_solid_review` WHERE soild_id=" + inspectSoild.get("id"));
                            for (InspectSoildReview inspectSoildReview : inspectSoildReviewList) {
                                soildReview.add(inspectSoildReview.toJSON());

                            }
                            total.put("soildReview",soildReview);
                        }
                        break;

                    case "air":
                        List<InspectAir> inspectAirList = InspectAir.inspectAir.find("SELECT * FROM `db_inspect_air` WHERE inspect_id=" + inspect.get("id"));
                        for (InspectAir inspectAir : inspectAirList) {
                            inspectJson.add(inspectAir.toJSON());
                            List airReview = new ArrayList();
                            List<InspectAirReview> inspectAirReviewList = InspectAirReview.inspectAirReview.find("SELECT * FROM `db_inspect_air_review` WHERE air_id=" + inspectAir.get("id"));
                            for (InspectAirReview inspectAirReview:inspectAirReviewList) {
                                airReview.add(inspectAirReview.toJSON());

                            }
                            total.put("airReview",airReview);
                        }
                        break;
                    case "dysodia":
                        List<InspectDysodia> inspectDysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT * FROM `db_inspect_dysodia` WHERE inspect_id=" + inspect.get("id"));
                        for (InspectDysodia inspectDysodia : inspectDysodiaList) {
                            inspectJson.add(inspectDysodia.toJSON());
                            List dysodiaReview = new ArrayList();
                            List<InspectDysodiaReview> inspectDysodiaReviewList = InspectDysodiaReview.inspectDysodiaReviewdao.find("SELECT * FROM `db_inspect_dysodia_review` WHERE dysodia_id=" + inspectDysodia.get("id"));
                            for (InspectDysodiaReview inspectDysodiaReview:inspectDysodiaReviewList) {
                                dysodiaReview.add(inspectDysodiaReview.toJSON());

                            }
                            total.put("dysodiaReview",dysodiaReview);
                        }
                        break;
                }
                total.put("items", inspectJson);

            }
            renderJson(total);

        } catch (Exception e)

        {
            renderError(500);
        }
    }
}