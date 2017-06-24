package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.lims.model.*;
import com.lims.utils.LoggerKit;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenyangyang on 2017/3/29.
 */
public class SampleController extends Controller {
    /**
     * 现在采用这个接口，以前所有list接口全部废弃（自行删除）
     * 传入:company_id
     * 传出:{results:[xxxx]}
     */
    public void list() {
        try {
            int company_id = getParaToInt("company_id");
            List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_sample` WHERE company_id=" + company_id);
            renderJson(toJson(sampleList));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void fetchProject() {
        try {
            int company_id = getParaToInt("company_id");
            List<Record> recordList = Db.find("SELECT p.*,m.element_id,m.name FROM `db_company` c,`db_item` i,`db_item_project` p,`db_monitor_project` m WHERE c.id=" + company_id + " AND i.company_id=c.id AND i.id=p.item_id AND m.id=p.project_id");
            //List result = new ArrayList();
            Map<Integer, List<Map>> result = new HashMap<>();
            for (Record record : recordList) {
                Map temp = new HashMap();
                temp.put("name", record.get("name"));
                temp.put("id", record.get("id"));
                if (result.containsKey(record.get("element_id"))) {
                    //存在
                    result.get(record.get("element_id")).add(temp);
                } else {
                    List<Map> t = new ArrayList();
                    t.add(temp);
                    //不存在
                    result.put(record.getInt("element_id"), t);
                }
            }

            List back = new ArrayList();
            for (Integer key : result.keySet()) {
                Element element = Element.elementDao.findById(key);
                Map p = new HashMap();
                p.put("element_id", element.get("id"));
                p.put("name", element.get("name"));
                p.put("project", result.get(key));
                back.add(p);
            }

            renderJson(back);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void saveSampleSigle() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int sample_id = getParaToInt("id");
                    Sample sample = Sample.sampleDao.findById(sample_id);
                    if (sample != null) {
                        if (getPara("isbalance[id]") != null) {
                            sample.set("balance", getPara("isbalance[id]"));
                        }
                        Boolean result = sample.set("name", getPara("name")).set("point", getPara("point")).set("other", getPara("other")).set("creater", ParaUtils.getCurrentUser(getRequest()).get("id")).set("create_time", ParaUtils.sdf.format(new Date())).set("process", 0).update();
                        List<SampleProject> sampleProjects = SampleProject.sampleprojrctDao.find("SELECT * FROM `db_sample_project` WHERE sample_id=" + sample_id);
                        for (SampleProject sp : sampleProjects) {
                            result = result && SampleProject.sampleprojrctDao.deleteById(sp.get("id"));
                            if (!result) return false;
                        }

                        Integer[] projectList = getParaValuesToInt("project[]");
                        for (int id : projectList) {
                            SampleProject sampleProject = new SampleProject();
                            result = result && sampleProject.set("sample_id", sample_id).set("item_project_id", id).save();
                            if (!result) return false;
                        }
                        return result;
                    } else
                        return false;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 样品编号生成
     * 【小写字母变成大写字母】
     * 例如WSA0001
     * self_identify:自送样  scene_identify:现场采样
     **/

    public static String createIdentify(int id) {
        try {
            String identify = "";
            String character = "";
            Task task = Task.taskDao.findById(id);
            Type type = Type.typeDao.findById(task.get("type"));
            String identifier = type.get("identifier");
            identify += identifier.toUpperCase();//将数据库表中的type的identifer小写转大写
            int sample_type = task.get("sample_type");
            Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
            if (encode == null) {
//            数据库中没有第一条记录，则创建它
                Encode entry = new Encode();
                entry.set("contract_identify", 0).set("character", "A");
                if (sample_type == 0) {
                    entry.set("self_identify", 1).set("scene_identify", 0);
                } else {
                    entry.set("self_identify", 0).set("scene_identify", 1);
                }
                entry.save();
                identify = identify + "A" + String.format("%04d", 1);
            } else {
                int identify_Encode = 0;
                if (sample_type == 0) {
                    //自送样
                    identify_Encode = (encode.get("self_identify") == null ? 0 : encode.getInt("self_identify")) + 1;
                    encode.set("self_identify", identify_Encode).update();
                } else {
                    //现场采样
                    identify_Encode = (encode.get("scene_identify") == null ? 0 : encode.getInt("scene_identify")) + 1;
                    character = encode.get("character");

                    if (identify_Encode == 9999) {

                    }
                    encode.set("scene_identify", identify_Encode == 9999 ? 0 : identify_Encode).set("character", identify_Encode == 9999 ? driver(character) : character).update();

                }
                identify += String.format("%04d", identify_Encode);
            }
            return identify;
        } catch (Exception e) {
            return null;
        }

    }


    /**
     * 作用：字母+1
     *
     * @param driver
     * @return
     */
    public static String driver(String driver) {
        if (driver != null && driver.length() > 0) {
            char[] charArray = driver.toCharArray();
            AtomicInteger z = new AtomicInteger(0);
            for (int i = charArray.length - 1; i > -1; i--) {
                if (charArray[i] == 'Z') {
                    z.set(z.incrementAndGet());
                } else {
                    if (z.intValue() > 0 || i == charArray.length - 1) {
                        AtomicInteger atomic = new AtomicInteger(charArray[i]);
                        charArray[i] = (char) atomic.incrementAndGet();
                        z.set(0);
                    }
                }
            }

            return String.valueOf(charArray);
        } else {
            return "A";
        }
    }


    /**
     * 自送样编号创建
     * 每次调用均返回一个自送样编号
     *
     * @return 自送样编号
     */
    public String createSelfIdentify() {
        try {
            String identify = "Z";
            Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
            if (encode == null) {
                //数据库中没有第一条记录，则创建它
                Encode entry = new Encode();
                entry.set("contract_identify", 0).set("self_identify", 1).set("scene_identify", 0).save();
                identify = identify + "-" + String.format("%04d", 1);
            } else {
                //当前已存在一条记录
                int identify_Encode = (encode.get("self_identify") == null ? 0 : encode.getInt("self_identify")) + 1;
                encode.set("self_identify", identify_Encode).update();
                identify += String.format("%04d", identify_Encode);
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
                    int company_id = getParaToInt("company_id");
                    Company company = Company.companydao.findById(company_id);
                    int task_id = company.get("task_id");
                    Task task = Task.taskDao.findById(task_id);
                    if (task != null) {
                        int count = getParaToInt("count");
                        for (int i = 0; i < count; i++) {
                            String identify = createIdentify(task_id);
                            Sample sample = new Sample();
                            result = result && sample.set("identify", identify).set("company_id", company_id).save();
                            if (!result) return false;
                        }
                    } else return false;
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
            int item_id = getParaToInt("id");
            List<Log> logList = Log.logDao.find("select * from `db_log`  where item_id =" + item_id + "orderby create_time  DESC");
            renderJson(toLogJson(logList));
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
     * 提供自送样样品信息保存接口
     **/
    public void selfCreate() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int company_id = getParaToInt("company_id");
                    Company company = Company.companydao.findById(company_id);
                    if (company != null) {
                        Boolean result = true;
                        String identify = createSelfIdentify();
                        Sample sample = new Sample();
                        result = result && sample
                                .set("identify", identify)
                                .set("name", getPara("name"))
                                .set("character", getPara("character"))
                                .set("condition", getPara("condition"))
                                .set("process", 0)
                                .set("company_id", company_id)
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("create_time", ParaUtils.sdf2.format(new Date()))
                                .save();
                        Integer[] projectList = getParaValuesToInt("project[]");
                        for (int id : projectList) {
                            SampleProject sampleProject = new SampleProject();
                            sampleProject
                                    .set("sample_id", sample.get("id"))
                                    .set("item_project_id", id);
                            result = result && sampleProject.save();
                            if (!result) return false;

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

    /***
     * 完成自送样登记
     * */
    public void register() {
        try {

            boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int task_id = getParaToInt("task_id");
                    Task task = Task.taskDao.findById(task_id);
                    Boolean result = true;
                    List<Sample> sampleList = Sample.sampleDao.find("select * from `db_sample` where task_id = " + task_id + " AND creater= " + ParaUtils.getCurrentUser(getRequest()).get("id") + " and process = 0");
                    for (Sample sample : sampleList) {
                        result = result && sample.set("process", ProcessKit.getSampleProcess("create")).update();
                        if (!result) return false;
                    }
                    result = result && task.set("sample_creater", ParaUtils.getCurrentUser(getRequest()).get("id")).set("sample_time", ParaUtils.sdf.format(new Date())).update();
                    return result;
                }

            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
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


    public void getProjectByCategory() {
        try {
            int task_id = getParaToInt("id");
            List<Record> recordList = Db.find("SELECT DISTINCT p.*,m.element_id FROM `db_company` c,`db_item` i,`db_task` t,`db_item_project` p,`db_monitor_project` m WHERE t.id=c.task_id AND t.id=" + task_id + " AND c.id=i.company_id AND i.id =p.item_id AND m.id=p.project_id");
            Map<Integer, List<Map>> listMap = new HashMap<>();
            for (Record record : recordList) {
                int element_id = record.get("element_id");
                int project_id = record.get("project_id");
                if (listMap.containsKey(element_id)) {
                    listMap.get(element_id).add(MonitorProject.monitorProjectdao.findById(project_id).toJsonSingle());
                } else {
                    List<Map> temp = new ArrayList<>();
                    temp.add(MonitorProject.monitorProjectdao.findById(project_id).toJsonSingle());
                    listMap.put(element_id, temp);
                }
            }
            List results = new ArrayList();
            for (Integer key : listMap.keySet()) {
                Element element = Element.elementDao.findById(key);
                Map result = new HashMap();
                result.put("element_id", element.get("id"));
                result.put("name", element.get("name"));
                result.put("project", listMap.get(key));
                results.add(result);
            }
            Map t = new HashMap();
            t.put("results", results);
            renderJson(t);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 得到自送样细节列表
     **/

    public void getSelfSampleList() {
        try {
            int company_id = getParaToInt("company_id");
            List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_sample` WHERE company_id=" + company_id);
            List<Map> result = new ArrayList<>();
            for (Sample sample : sampleList) {
                result.add(sample.toSimpleJson());
            }
            Map t = new HashMap();
            t.put("results", result);
            renderJson(t);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 删除样品
     **/
    public void deleteSample() {
        try {
            int sample_id = getParaToInt("sample_id");
            Boolean result = Sample.sampleDao.deleteById(sample_id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }

    }

    /**
     * 修改自送样样品接口
     **/
    public void changeSample() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int sample_id = getParaToInt("id");
                    Sample sample = Sample.sampleDao.findById(sample_id);
                    if (sample != null) {
                        Boolean result = true;
                        result = result && sample
                                .set("name", getPara("name"))
                                .set("character", getPara("character"))
                                .set("condition", getPara("condition"))
                                .set("process", ProcessKit.getSampleProcess("create"))
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("create_time", ParaUtils.sdf2.format(new Date()))
                                .update();
                        List<SampleProject> sampleProjects = SampleProject.sampleprojrctDao.find("SELECT * FROM `db_sample_project` WHERE sample_id=" + sample_id);
                        for (SampleProject p : sampleProjects) {
                            result = result && SampleProject.sampleprojrctDao.deleteById(p.get("id"));
                            if (!result) return false;
                        }
                        Integer[] projectList = getParaValuesToInt("project[]");
                        for (int id : projectList) {
                            SampleProject sampleProject = new SampleProject();
                            sampleProject
                                    .set("sample_id", sample.get("id"))
                                    .set("item_project_id", id);
                            result = result && sampleProject.save();
                            if (!result) return false;
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
     * 删除全部样品接口
     **/
    public void deleteAll() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Integer[] selected = getParaValuesToInt("selected[]");
                    Boolean result = true;
                    for (int i = 0; i < selected.length; i++) {
                        int id = selected[i];
                        result = result && Sample.sampleDao.deleteById(id);
                        if (!result) break;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void createSample() {
        try {
            String id = getPara("id");
            Task task = Task.taskDao.findFirst("SELECT * FROM `db_task` WHERE id=" + id);
            if (task != null) {
                getRequest().setAttribute("task", task);
                render("/template/create_selfsample.jsp");
            } else renderNull();
        } catch (Exception e) {
            renderError(500);
        }

    }
}
