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
            Task task = Task.taskDao.findById(task_id);
            Map total = new HashMap();
            total.put("client_unit", task.get("client_unit"));
            total.put("identify", task.get("identify"));

            total.put("flag", task.get("flag"));
            List<MonitorProject> monitorProjectList = MonitorProject.monitorProjectdao.find("SELECT DISTINCT  m.* FROM`db_task`t, `db_company` c,`db_item` i,`db_item_project` p ,`db_monitor_project` m\n" +
                    "WHERE t.id=" + task_id + " AND c.task_id = t.id AND i.company_id=c.id AND p.item_id=i.id AND m.id = p.project_id");

            List<Map> results = new ArrayList<>();
            for (MonitorProject monitorProject : monitorProjectList) {
                Map temp = new HashMap();
                temp.put("project", monitorProject.toJsonSingle());
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_company` c,`db_item` i,`db_item_project` p  WHERE p.project_id = '" + monitorProject.get("id") + "'AND c.task_id= '" + task_id + "'AND i.company_id =c.id AND p.item_id = i.id");
                List<Map> map=new ArrayList<>();
                for (ItemProject itemProject:itemProjectList){
                    map.add(itemProject.toJsonSingle());
                }
                temp.put("items",map);
                String in = "(";
                for (int i = 0; i < itemProjectList.size(); i++) {
                    in += itemProjectList.get(i).get("id");
                    if (i != itemProjectList.size() - 1) {
                        in += ",";
                    }
                }
                in += ")";
                List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_sample_project` p,`db_sample` s WHERE p.item_project_id in " + in + " AND p.sample_id=s.id");
                temp.put("sampleList", toJson(sampleList));
                int count1 = 0;
                for (Sample sample : sampleList) {
                    if (sample.get("balance") != null) {
                        count1++;
                    }

                }
                List<Sample> lib = Sample.sampleDao.find("SELECT * FROM `db_sample` s \n" +
                        "WHERE s.id in (SELECT l.sample_id FROM `db_lib` l WHERE  l.task_id =' " + task_id + "'AND l.project_id ='" + monitorProject.get("id") + "') ");

                temp.put("libList",toJson(lib));

                List<Sample> tag = Sample.sampleDao.find("SELECT * FROM `db_sample` s \n" +
                        "WHERE s.id in (SELECT l.sample_id FROM `db_tag` l WHERE  l.task_id =' " + task_id + "'AND l.project_id ='" + monitorProject.get("id") + "') ");
                temp.put("tagList", toJson(tag));
                List<Blind> blindList = Blind.blindDao.find("SELECT * FROM `db_blind` WHERE task_id =' " + task_id + "' AND project_id =" + monitorProject.get("id"));
                if (blindList.size() != 0) {
                    temp.put("blind", blindList.get(0).get("count"));
                } else {
                    temp.put("blind", 0);
                }
                temp.put("sceneCount", count1);
                results.add(temp);
            }

            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public List toJson(List<Sample> entityList) {
        List result = new ArrayList();
        try {
            for (Sample sample : entityList) {
                result.add(toJsonSingle(sample));
            }
            //json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return result;
    }

    public Map toJsonSingle(Sample sample) {
        Map<String, Object> types = new HashMap<>();
        types.put("id", sample.getInt("id"));
        types.put("identify", sample.get("identify"));
        types.put("category", sample.get("category"));
        types.put("name", sample.get("name"));
        types.put("character", sample.get("character"));
        types.put("condition", sample.get("condition"));
        types.put("isbalance", Sample.sampleDao.findById(sample.get("balance")));
        types.put("task_id", sample.get("task_id"));
        types.put("item_id", sample.get("item_id"));
        types.put("create_time", sample.get("create_time"));
        types.put("creater", sample.get("creater"));
        types.put("process", sample.get("process"));
        types.put("other", sample.get("other"));
        types.put("point", sample.get("point"));

        List<SampleProject> sampleProjectList = SampleProject.sampleprojrctDao.find("SELECT * FROM `db_sample_project` WHERE sample_id=" + sample.get("id"));

        List project = new ArrayList();
        List temp = new ArrayList();
        for (SampleProject sp : sampleProjectList) {
            Map m = new HashMap();
            m.put("id", sp.get("item_project_id"));
            m.put("name", MonitorProject.monitorProjectdao.findById(ItemProject.itemprojectDao.findById(sp.get("item_project_id")).get("project_id")).get("name"));
            project.add(sp.get("item_project_id"));
            temp.add(m);

        }
        types.put("project", project);
        types.put("projectList", temp);
        return types;
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

                        List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_company` c,`db_item` i,`db_item_project` p WHERE c.task_id ='" + temp.get("task_id") + "' AND i.company_id =c.id AND p.item_id =i.id AND p.project_id =" + temp.get("project_id"));
                        if (itemProjectList != null) {

                            List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE task_id ='" + temp.get("task_id") + "'AND project_id=" + temp.get("project_id"));
                            for (Lib lib : libList) {
                                result = result && Lib.libDao.deleteById(lib.get("id"));
                            }
                            List<Tag> tagList = Tag.tagDao.find("SELECT * FROM `db_tag` WHERE  task_id ='" + temp.get("task_id") + "'AND project_id=" + temp.get("project_id"));
                            for (Tag tag : tagList) {
                                result = result && Tag.tagDao.deleteById(tag.get("id"));
                            }
                            List<Blind> blindList = Blind.blindDao.find("SELECT * FROM `db_blind` WHERE  task_id ='" + temp.get("task_id") + "'AND project_id=" + temp.get("project_id"));
                            for (Blind blind : blindList) {
                                result = result && Blind.blindDao.deleteById(blind.get("id"));
                            }
                        }
                        for (ItemProject itemProject : itemProjectList) {
                            result = result && itemProject.set("process", 1).update();

                        }

                        Blind blind = new Blind();
                        blind.set("count", temp.get("blind")).set("task_id", temp.get("task_id")).set("project_id", temp.get("project_id"));
                        result = result && blind.save();
                        List<Integer> libList = (List<Integer>) temp.get("labs");
                        if (libList == null) {
                        } else {
                            for (int id : libList) {
                                Lib lib = new Lib();
                                lib.set("sample_id", id)
                                        .set("task_id", temp.get("task_id"))
                                        .set("project_id", temp.get("project_id"));
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
                                        .set("task_id", temp.get("task_id"))
                                        .set("project_id", temp.get("project_id"));
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

            int task_id = getParaToInt("task_id");
            int project_id = getParaToInt("project_id");
            Boolean result = true;
            List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_company` c,`db_item` i,`db_item_project` p WHERE c.task_id='" + task_id + "' AND i.company_id =c.id AND p.item_id =i.id AND p.project_id =" + project_id);
            for (ItemProject itemProject : itemProjectList) {
                result = result && itemProject.set("process", null).update();
//                List<Sample> sampleList =Sample.sampleDao.find("SELECT * FROM `db_sample` s,`db_sample_project`p WHERE s.id = p.sample_id AND p.item_project_id ="+itemProject.get("id"));
//                for (Sample sample:sampleList)
//                {
//                    result = result && sample.set("process",4).update();
//                }
            }
            if (itemProjectList != null) {

                List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE task_id ='" + task_id + "' AND project_id=" + project_id);
                for (Lib lib : libList) {
                    result = result && Lib.libDao.deleteById(lib.get("id"));
                }
                List<Tag> tagList = Tag.tagDao.find("SELECT * FROM `db_tag` WHERE  task_id ='" + task_id + "' AND project_id=" + project_id);
                for (Tag tag : tagList) {
                    result = result && Tag.tagDao.deleteById(tag.get("id"));
                }
                List<Blind> blindList = Blind.blindDao.find("SELECT * FROM `db_blind` WHERE  task_id ='" + task_id + "' AND project_id=" + project_id);
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
                    int task_id = getParaToInt("task_id");
                    int count = getParaToInt("blind");
                    int project_id = getParaToInt("project_id");
                    Boolean result = true;
                    List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_company` c,`db_item` i,`db_item_project` p WHERE c.task_id ='" + task_id + "' AND i.company_id =c.id AND p.item_id =i.id AND p.project_id =" + project_id);
                    for (ItemProject itemProject : itemProjectList) {
                        result = result && itemProject.set("process", 1).update();
//                        List<Sample> sampleList =Sample.sampleDao.find("SELECT * FROM `db_sample` s,`db_sample_project`p WHERE s.id = p.sample_id AND p.item_project_id ="+itemProject.get("id"));
//                        for (Sample sample:sampleList)
//                        {
//                            result = result && sample.set("process",4).update();
//                        }

                    }
                    if (itemProjectList != null) {
                        List<Lib> libList = Lib.libDao.find("SELECT * FROM `db_lib` WHERE task_id ='" + task_id + "' AND project_id=" + project_id);
                        for (Lib lib : libList) {
                            result = result && Lib.libDao.deleteById(lib.get("id"));
                        }
                        List<Tag> tagList = Tag.tagDao.find("SELECT * FROM `db_tag` WHERE  task_id ='" + task_id + "' AND project_id=" + project_id);
                        for (Tag tag : tagList) {
                            result = result && Tag.tagDao.deleteById(tag.get("id"));
                        }
                        List<Blind> blindList = Blind.blindDao.find("SELECT * FROM `db_blind` WHERE  task_id ='" + task_id + "' AND project_id=" + project_id);
                        for (Blind blind : blindList) {
                            result = result && Blind.blindDao.deleteById(blind.get("id"));
                        }
                        Blind blind = new Blind();
                        blind.set("task_id", task_id).set("count", count).set("project_id", project_id);
                        result = result && blind.save();

                        Integer[] libList1 = getParaValuesToInt("lab[]");
                        if (libList1 == null) {
                        } else {
                            for (int id : libList1) {
                                Lib lib = new Lib();
                                lib.set("sample_id", id)
                                        .set("task_id", task_id)
                                        .set("project_id", project_id);
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
                                        .set("task_id", task_id)
                                        .set("project_id", project_id);
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
                    List<ItemProject> itemProjectList =ItemProject.itemprojectDao.find("SELECT p.* From `db_task` t,`db_company` c,`db_item` i,`db_item_project` p WHERE t.id= '" + task_id + "' AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id ");
                    for (ItemProject itemProject:itemProjectList){
                        result =result && itemProject.set("flag",1).update();

                    }
                    result = result && task.set("process", ProcessKit.getTaskProcess("quality")).set("flag", 1).update();

                }
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }

    }


}

