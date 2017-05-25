package com.lims.controller;

import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
import org.apache.poi.ss.formula.functions.T;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by qulongjun on 2017/5/3.
 */
public class QualityController extends Controller {
    public void list() {
        try {
            int task_id = getParaToInt("task_id");
            Task task=Task.taskDao.findById(task_id);
            Map total =new HashMap();
            total.put("client_unit",task.get("client_unit"));
            total.put("identify",task.get("identify"));
            total.put("flag",task.get("flag"));
//            total.put("client_unit", task.get("client_unit"));
//            total.put("type", Type.typeDao.findById(task.get("type")));
//            total.put("time", task.get("sample_time"));
//            total.put("sample_creater", User.userDao.findById(task.get("sample_creater")));
//            total.put("package", task.get("package"));
//            total.put("receive_type", task.get("receive_type"));
//            total.put("additive", task.get("additive"));
//            List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_task` t,`db_company` c,`db_sample` s \n" +
//                    "WHERE t.id=" + task_id + " AND c.task_id=t.id AND s.company_id=c.id ORDER BY s.identify");
//            total.put("count", sampleList.size());
//            if (sampleList.size() != 0) {
//                String first = sampleList.get(0).getStr("identify");
//                String last = sampleList.get(sampleList.size() - 1).getStr("identify");
//                total.put("firstIdentify", first);
//                total.put("lastIdentify", last);
//                    total.put("identify", first + "~" + last);
//            }
//            Map<List, List> back = new HashMap<>();
//            List<Sample> sampleList1 = Sample.sampleDao.find("SELECT s.* FROM `db_task` t,`db_company` c,`db_sample` s \n" +
//                    "WHERE t.id=" + task_id + " AND c.task_id=t.id AND s.company_id=c.id ORDER BY s.identify");
//            for (Sample sample : sampleList1) {
//                List<MonitorProject> records = MonitorProject.monitorProjectdao.find("SELECT m.* FROM `db_sample` s,`db_sample_project` p,`db_item_project` i,`db_monitor_project` m\n" +
//                        "WHERE s.id=" + sample.get("id") + " AND p.sample_id=s.id AND p.item_project_id=i.id AND i.project_id=m.id");
//                List<Map> b = new ArrayList<>();
//                for (MonitorProject monitorProject : records) {
//                    b.add(monitorProject.toJsonSingle());
//                }
//                if (back.containsKey(b)) {
//                    当前已经存在这分析项目的组合
//                    back.get(b).add(sample);
//                } else {
//                    List t = new ArrayList();
//                    t.add(sample);
//                    back.put(b, t);
//                }
//            }
//            List<Map> mapList = new ArrayList<>();
//            for (List s : back.keySet()) {
//                Map m = new HashMap();
//                m.put("projects", s);
//                m.put("samples", back.get(s));
//                List<Map> de = new ArrayList();
//                for (int i = 0; i < back.get(s).size(); i++) {
//                    List<Description> descriptionList = Description.descriptionDao.find("SELECT * FROM `db_sample_deseription` WHERE sample_id =" + ((Sample) (back.get(s).get(i))).get("id"));
//                    for (Description description : descriptionList) {
//                        de.add(description.toJsonSingle());
//                    }
//
//                }
//                m.put("item", de);
//
//                mapList.add(m);
//
//            }
//            total.put("results",mapList);

            List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM`db_task`t, `db_company` c,`db_item` i,`db_item_project` p \n" +
                    "WHERE t.id=" + task_id + " AND c.task_id = t.id AND i.company_id=c.id AND p.item_id=i.id");
            List result = new ArrayList();
            for (ItemProject itemProject : itemProjectList) {
                Map temp = new HashMap();
                temp = itemProject.toJsonSingle();
                List<Sample> sampleList2 = Sample.sampleDao.find("SELECT s.* FROM `db_company` c,`db_sample` s,`db_sample_project` p WHERE c.task_id='" + task_id + "'AND s.company_id =c.id AND p.sample_id=s.id AND p.item_project_id=" + itemProject.get("id"));
                List<Map> re = new ArrayList<>();
                int count = 0;
                for (Sample sample : sampleList2) {
                    re.add(sample.toSimpleJson());
                    if (sample.get("balance") != null) {
                        count++;
                    }
                }
                List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE item_project_id=" + itemProject.get("id"));
                List<Map> mapList1 = new ArrayList<>();
                for (Lib lib : libList) {
                    Sample sample = Sample.sampleDao.findById(lib.get("sample_id"));
                    mapList1.add(sample.toSimpleJson());
                }
                temp.put("lab", mapList1);

                List<Tag> tagList = Tag.tagDao.find("select * from `db_tag` where item_project_id=" + itemProject.get("id"));
                List<Map> ta = new ArrayList<>();
                for (Tag tag : tagList) {
                    Sample sample = Sample.sampleDao.findById(tag.get("sample_id"));
                    ta.add(sample.toSimpleJson());
                }
                temp.put("tag", ta);
                Blind blind = Blind.blindDao.findFirst("select * from `db_blind` where item_project_id=" + itemProject.get("id"));
                temp.put("blind", blind != null ? blind.get("count") : 0);
                temp.put("sample", re);
                temp.put("item_project_id", itemProject.getInt("id"));
                temp.put("sceneCount", count);
                result.add(temp);
            }
            total.put("items",result);
            renderJson(total);
        } catch (Exception e) {
            renderError(500);
        }
    }

    //质控统计表创建保存
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int item_project_id = getParaToInt("id");
                    int count = getParaToInt("blind");
                    ItemProject itemProject = ItemProject.itemprojectDao.findById(item_project_id);
                    Boolean result = true;
                    result = result && itemProject.set("process", 1).update();//0-未登记 ，1已登记
                    if (itemProject != null) {
                        Blind blind = new Blind();

                        blind.set("item_project_id", item_project_id).set("count", count);
                        result = result && blind.save();
                        Integer[] libList = getParaValuesToInt("lab[]");
                        if (libList == null) {

                        } else {
                            for (int id : libList) {
                                Lib lib = new Lib();
                                lib.set("sample_id", id)
                                        .set("item_project_id", item_project_id);
                                result = result && lib.save();
                                if (!result) return false;
                            }
                        }
                        Integer[] tagList = getParaValuesToInt("tag[]");
                        if (tagList == null) {

                        } else {
                            for (int id : tagList) {
                                Tag tag = new Tag();
                                tag.set("sample_id", id)
                                        .set("item_project_id", item_project_id);
                                result = result && tag.save();
                                if (!result) return false;

                            }
                        }
                        return result;

                    } else return false;
                }
            });

            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 全部保存
     **/
    public void allSave() {

        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    String[] itemList = getParaValues("items[]");
                    boolean result = true;
                    for (String item : itemList) {

                        Map temp = Jackson.getJson().parse(item, Map.class);

                        ItemProject itemProject = ItemProject.itemprojectDao.findById(temp.get("item_project_id"));
                        if (itemProject != null) {

                            List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE item_project_id=" + temp.get("item_project_id"));
                            for (Lib lib : libList) {
                                result = result && Lib.libDao.deleteById(lib.get("id"));
                            }
                            List<Tag> tagList = Tag.tagDao.find("SELECT * FROM `db_tag` WHERE  item_project_id =" + temp.get("item_project_id"));
                            for (Tag tag : tagList) {
                                result = result && Tag.tagDao.deleteById(tag.get("id"));
                            }
                            List<Blind> blindList = Blind.blindDao.find("SELECT * FROM `db_blind` WHERE  item_project_id =" + temp.get("item_project_id"));
                            for (Blind blind : blindList) {
                                result = result && Blind.blindDao.deleteById(blind.get("id"));
                            }
                        }
                        result = result && itemProject.set("process", 1).update();


                        Blind blind = new Blind();
                        blind.set("count", temp.get("blind")).set("item_project_id", temp.get("item_project_id"));
                        result = result && blind.save();
                        List<Integer> libList = (List<Integer>) temp.get("labs");
                        if (libList == null) {
                        } else {
                            for (int id : libList) {
                                Lib lib = new Lib();
                                lib.set("sample_id", id)
                                        .set("item_project_id", temp.get("item_project_id"));
                                result = result && lib.save();
                                if (!result) return false;
                            }
                        }

                        List<Integer> tagList = (List<Integer>) temp.get("tags");
                        if (tagList == null) {
                        } else {
                            for (int id : tagList) {
                                Tag tag = new Tag();
                                tag.set("sample_id", id)
                                        .set("item_project_id", temp.get("item_project_id"));
                                result = result && tag.save();
                                if (!result) return false;

                            }
                        }
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
     * 清空质量控制统计表
     **/
    public void clear() {
        try {

            int item_project_id = getParaToInt("id");
            ItemProject itemProject = ItemProject.itemprojectDao.findById(item_project_id);
            Boolean result = true;
            result = result && itemProject.set("process", null).update();
            if (itemProject != null) {

                List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE item_project_id=" + item_project_id);
                for (Lib lib : libList) {
                    result = result && Lib.libDao.deleteById(lib.get("id"));
                }
                List<Tag> tagList = Tag.tagDao.find("SELECT * FROM `db_tag` WHERE  item_project_id =" + item_project_id);
                for (Tag tag : tagList) {
                    result = result && Tag.tagDao.deleteById(tag.get("id"));
                }
                List<Blind> blindList = Blind.blindDao.find("SELECT * FROM `db_blind` WHERE  item_project_id =" + item_project_id);
                for (Blind blind : blindList) {
                    result = result && Blind.blindDao.deleteById(blind.get("id"));
                }
            }

            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 修改质量控制表
     **/
    public void change() {
        try {
            boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int item_project_id = getParaToInt("id");
                    int count = getParaToInt("blind");
                    Boolean result = true;
                    ItemProject itemProject = ItemProject.itemprojectDao.findById(item_project_id);
                    result = result && itemProject.set("process", 1).update();

                    if (itemProject != null) {
                        List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE item_project_id=" + item_project_id);
                        for (Lib lib : libList) {
                            result = result && Lib.libDao.deleteById(lib.get("id"));
                        }
                        List<Tag> tagList = Tag.tagDao.find("SELECT * FROM `db_tag` WHERE  item_project_id =" + item_project_id);
                        for (Tag tag : tagList) {
                            result = result && Tag.tagDao.deleteById(tag.get("id"));
                        }
                        List<Blind> blindList = Blind.blindDao.find("SELECT * FROM `db_blind` WHERE  item_project_id =" + item_project_id);
                        for (Blind blind : blindList) {
                            result = result && Blind.blindDao.deleteById(blind.get("id"));
                        }
                        Blind blind = new Blind();
                        blind.set("item_project_id", item_project_id).set("count", count);
                        result = result && blind.save();

                        Integer[] libList1 = getParaValuesToInt("lab[]");
                        if (libList1 == null) {
                        } else {
                            for (int id : libList1) {
                                Lib lib = new Lib();
                                lib.set("sample_id", id)
                                        .set("item_project_id", item_project_id);
                                result = result && lib.save();
                                if (!result) return false;
                            }
                        }
                        Integer[] tagList1 = getParaValuesToInt("tag[]");
                        if (tagList1 == null) {
                        } else {
                            for (int id : tagList1) {
                                Tag tag = new Tag();
                                tag.set("sample_id", id)
                                        .set("item_project_id", item_project_id);
                                result = result && tag.save();
                                if (!result) return false;

                            }
                        }
                        return result;

                    } else return false;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 是否完成质控 ,流转到实验室进行拆分
     **/


    public void finishQuality() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            boolean result = true;
            if (task != null) {
                int itemProjectSize = ItemProject.itemprojectDao.find("SELECT p.* From `db_task` t,`db_company` c,`db_item` i,`db_item_project` p WHERE t.id= '" + task_id + "' AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id AND p.process is NULL").size();
                if (itemProjectSize != 0) {
                    //还有没有质控
                    renderJson(RenderUtils.CODE_UNIQUE);
                } else {
                    result = result && task.set("process", ProcessKit.getTaskProcess("quality")).set("flag",1).update();
                }
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }

    }


}

