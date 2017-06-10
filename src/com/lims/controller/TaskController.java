package com.lims.controller;

import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;
import com.lims.utils.LoggerKit;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;

import javax.xml.ws.Service;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by qulongjun on 2017/3/10.
 */
public class TaskController extends Controller {

    /**
     * 自定义任务书创建
     */
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Map paraMaps = getParaMap();
                    Task task = new Task();
                    Boolean result = true;
                    for (Object key : paraMaps.keySet()) {
                        switch (key.toString()) {
                            case "id":
                                break;//不知道为什么会传一个id过来，待观察
                            case "serviceId":
                                ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(getPara("serviceId"));
                                if (serviceContract != null) {
                                    task.set("identify", serviceContract.get("identify")).set("service_id", serviceContract.get("id"));
                                }
                                break;
                            default:
                                if (key.toString().indexOf("item") != -1) {
                                    continue;
                                }
                                task.set(key.toString(), ((String[]) paraMaps.get(key))[0]);
                        }

                    }
                    User user = ParaUtils.getCurrentUser(getRequest());
                    if (task.get("identify") == null) {
                        task.set("identify", createIdentify());
                    }
                    task.set("sample_type", getPara("sample_type")).set("importWrite", getPara("importWrite")).set("create_time", ParaUtils.sdf.format(new Date())).set("creater", user.get("id")).set("process", ProcessKit.getTaskProcess("create"));
                    result = result && task.save();
                    String[] items = getParaValues("project_items[]");
                    for (String item : items) {
                        Map temp = Jackson.getJson().parse(item, Map.class);
                        Company company = new Company();
                        result = result && company.set("task_id", task.get("id")).set("company", temp.get("company")).set("flag", temp.get("flag")).set("process", 0).set("creater", ParaUtils.getCurrentUser(getRequest()).getInt("id")).set("create_time", ParaUtils.sdf.format(new Date())).save();
                        List<Map> projectItems = (List<Map>) temp.get("items");
                        for (Map itemMap : projectItems) {
                            Contractitem contractitem = new Contractitem();
                            result = result && contractitem.set("company_id", company.get("id")).set("element", ((Map) itemMap.get("element")).get("id")).set("frequency", ((Map) itemMap.get("frequency")).get("id")).set("point", itemMap.get("point")).set("other", itemMap.get("other")).save();

                            List<Map> project = (List<Map>) itemMap.get("project");
                            for (Map pro : project) {
                                ItemProject itemProject = new ItemProject();
                                result = result && itemProject.set("project_id", pro.get("id")).set("item_id", contractitem.get("id")).set("process", null).set("flag", null).set("labFlag", null).save();
                                if (!result) break;
                            }
                            if (!result) break;
                        }

                    }
                    LoggerKit.addTaskLog(task.getInt("id"), "创建了任务", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据合同创建
     */
    public void createByContract() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Task task = new Task();
                    Contract contract = Contract.contractDao.findById(getPara("contract_id"));
                    if (contract != null) {
                        Boolean isFirst = Task.taskDao.findFirst("select * from `db_task` where  identify ='" + contract.get("identify") + "'") != null;
                        Boolean result = task
                                .set("sample_type", getPara("sample_type"))
                                .set("contract_id", getPara("contract_id"))
                                .set("process", ProcessKit.getTaskProcess("create"))
                                .set("create_time", ParaUtils.sdf.format(new Date()))
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("identify", isFirst ? createIdentify() : contract.get("identify"))
//                                .set("identify", contract.get("identify"))
                                .set("client_unit", contract.get("client_unit"))
                                .set("client_code", contract.get("client_code"))
                                .set("client_tel", contract.get("client_tel"))
                                .set("client", contract.get("client"))
                                .set("client_fax", contract.get("client_fax"))
                                .set("client_address", contract.get("client_address"))
                                .set("name", contract.get("name"))
                                .set("aim", contract.get("aim"))
                                .set("type", contract.get("type"))
                                .set("way", contract.get("way"))
                                .set("wayDesp", contract.get("wayDesp"))
                                .set("other", contract.get("other"))
                                .set("charge", getPara("charge"))
                                .set("importWrite", contract.get("importWrite"))
                                .save();
                        result = result && contract.set("process", ProcessKit.getContractProcess("review")).update();
                        //加入判定，即若当前合同创建了多个任务书，则需要将db_company复制一份新的


                        List<Company> companyList = Company.companydao.find("SELECT * FROM `db_company` WHERE contract_id=" + contract.get("id"));
                        for (Company company : companyList) {
                            if (isFirst) {
                                int company_id = company.get("id");
                                //非第一次创建
                                result = result && company.set("id", null).set("task_id", task.get("id")).set("contract_id", null).set("process", 0).save();
                                if (!result) return false;
                                List<Contractitem> itemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + company_id);
                                for (Contractitem item : itemList) {
                                    int item_id = item.get("id");
                                    result = result && item.set("id", null).set("company_id", company.get("id")).save();
                                    if (!result) return false;
                                    List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project` WHERE item_id=" + item_id);
                                    for (ItemProject itemProject : itemProjectList) {
                                        result = result && itemProject.set("id", null).set("item_id", item.get("id")).set("process", null).set("flag", null).set("labFlag", null).save();
                                        if (!result) return false;
                                    }

                                }

                            } else {
                                result = result && company.set("task_id", task.get("id")).update();
                            }
                            if (!result) return false;
                        }
                        LoggerKit.addTaskLog(task.getInt("id"), "下达任务", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
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
     * 根据服务合同导入
     */
    public void createByService() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Map paraMaps = getParaMap();
                    Task task = new Task();
                    Boolean result = true;
                    for (Object key : paraMaps.keySet()) {
                        switch (key.toString()) {
                            case "id":
                                break;//不知道为什么会传一个id过来，待观察
                            case "serviceId":
                                ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(getPara("serviceId"));
                                if (serviceContract != null) {
                                    task.set("identify", serviceContract.get("identify")).set("service_id", serviceContract.get("id"));
                                }
                                break;
                            default:
                                if (key.toString().indexOf("item") != -1) {
                                    continue;
                                }
                                task.set(key.toString(), ((String[]) paraMaps.get(key))[0]);
                        }

                    }
                    User user = ParaUtils.getCurrentUser(getRequest());
                    task.set("sample_type", getPara("sample_type")).set("create_time", ParaUtils.sdf.format(new Date())).set("creater", user.get("id")).set("process", ProcessKit.getTaskProcess("create"));
                    result = result && task.save();
                    String[] items = getParaValues("project_items[]");
                    for (String item : items) {
                        Map temp = Jackson.getJson().parse(item, Map.class);
                        Company company = new Company();
                        result = result && company.set("task_id", task.get("id")).set("company", temp.get("company")).set("flag", temp.get("flag")).set("process", 0).set("creater", ParaUtils.getCurrentUser(getRequest()).getInt("id")).set("create_time", ParaUtils.sdf.format(new Date())).save();
                        List<Map> projectItems = (List<Map>) temp.get("items");
                        for (Map itemMap : projectItems) {
                            Contractitem contractitem = new Contractitem();
                            result = result && contractitem.set("company_id", company.get("id")).set("element", ((Map) itemMap.get("element")).get("id")).set("frequency", ((Map) itemMap.get("frequency")).get("id")).set("point", itemMap.get("point")).set("other", itemMap.get("other")).save();

                            List<Map> project = (List<Map>) itemMap.get("project");
                            for (Map pro : project) {
                                ItemProject itemProject = new ItemProject();
                                result = result && itemProject.set("project_id", pro.get("id")).set("item_id", contractitem.get("id")).set("process", null).set("flag", null).set("labFlag", null).save();
                                if (!result) break;
                            }
                            if (!result) break;
                        }

                    }
                    LoggerKit.addTaskLog(task.getInt("id"), "创建了任务", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

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
                        case "total":
                            break;
                        case "totalDispatch":
                            param += "AND ( process = " + ProcessKit.getTaskProcess("create") + " OR process = " + ProcessKit.getTaskProcess("dispatch") + ") ";
                            break;
                        //自送样
                        case "apply_sample":
                            param += " AND  sample_type = 0   AND process=" + ProcessKit.getTaskProcess("create") + " ";
                            break;
                        case "delivery":
                            param += "AND sample_type =1 AND process =" + ProcessKit.getTaskProcess("create") + " ";
                            break;
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
            Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_task` " + param + " ORDER BY create_time DESC");
            List<Task> taskList = taskPage.getList();


            Map results = toJson(taskList);
            results.put("currentPage", currentPage);
            results.put("totalPage", taskPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }


    /**
     * 实验结果一审列表
     */
    public void firstReviewList() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            if (rowCount == 0) {
                rowCount = ParaUtils.getRowCount();
            }
            String param = " WHERE process=" + ProcessKit.getTaskProcess("firstReview");
            Page<Task> taskPage = Task.taskDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_task` " + param + " ORDER BY create_time DESC");
            List<Task> taskList = taskPage.getList();
            Map results = toJson(taskList);
            results.put("currentPage", currentPage);
            results.put("totalPage", taskPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public Map toJson(List<Task> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Task task : entityList) {
                result.add(toJsonSingle(task));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Task entry) {
        Map temp = new HashMap();
        temp.put("id", entry.get("id"));
        temp.put("name", entry.get("name"));
        temp.put("create_time", entry.get("create_time"));
        temp.put("client_unit", entry.get("client_unit"));
        temp.put("identify", entry.get("identify"));
        temp.put("process", entry.get("process"));
        temp.put("sample_type", entry.get("sample_type"));
        temp.put("flag", entry.get("flag"));
        return temp;
    }


    public void getItems() {
        try {
            int id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(id);
            if (task.get("contract_id") != null) {
                Contract contract = Contract.contractDao.findById(task.get("contract_id"));
                if (contract != null) {
                    renderJson(contract.getItems());
                } else renderJson(RenderUtils.CODE_EMPTY);
            } else {
                if (task != null) {
                    renderJson(task.getItems());
                } else renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }



    public void countProcess() {

        try {
            int count = Task.taskDao.find("SELECT * FROM `db_task` WHERE process =" + ProcessKit.TaskMap.get("create") + " AND sample_type=1").size();
            Map temp = new HashMap();
            temp.put("create", count); //待任务派遣个数
            renderJson(temp);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void countTotal() {
        try {
            Map result = new HashMap();
            result.put("total", Task.taskDao.find("select * from `db_task`").size());
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void monitorItem() {
        try {
            int id = getParaToInt("id");
            List temp = new ArrayList();
            List<ItemProject> projectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project` WHERE item_id=" + id);
            for (ItemProject itemProject : projectList) {
                temp.add(itemProject.toJsonSingle());
            }
            renderJson(temp);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void taskDetails()

    {
        try {
            int id = getParaToInt("id");
            Task task = Task.taskDao.findById(id);
            if (task != null) {
                renderJson(toTaskDetailJSON(task));
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toTaskDetailJSON(Task entry) {
        Map temp = new HashMap();
        for (String key : entry._getAttrNames()) {
            switch (key) {
                case "trustee":
                    temp.put("trustee", entry.get(key) == null ? "" : User.userDao.findById(entry.get(key)).toSimpleJson());
                    break;
                case "type":
                    temp.put("type", entry.get(key) == null ? "" : Type.typeDao.findById(entry.get(key)));
                    break;
                default:
                    temp.put(key, entry.get(key));
            }
        }
        return temp;
    }

    public void stopTask() {
        try {
            boolean result = Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
//                    int Result = getParaToInt("contract_result");
                    Task task = Task.taskDao.findById(id);
                    Boolean result = true;
//                    Boolean contractResult = true;
                    if (task != null) {
                        result = task.set("process", -2).update();
//                        Contract contract = Contract.contractDao.findFirst("select * from `db_contrat` where  id=" + task.get("contract_id"));
//                        if (contract != null) {
//                            switch (Result) {
//                                case 0:
//                                    contractResult = contract.set("process", ProcessKit.ContractMap.get("review")).update();
//                                    break;
//                                case 1:
//                                    contractResult = contract.set("process", ProcessKit.ContractMap.get("stop")).update();
//                                    break;
//                                case 2:
//                                    break;
//                            }
//                        }
                    }
                    LoggerKit.addTaskLog(task.getInt("id"), "中止了任务", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void deleteTask() {
        try {
            int id = getParaToInt("id");
            boolean result = Task.taskDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 任务编号生成
     * <p>
     * 年份+ - + 四位流水编号，如 2017-001  2017-002  以此类推
     * <p>
     * 需要考虑：年份更新需要自动更新当前年份，且将流水号恢复初始值1号
     **/
    public String createIdentify() {
        String identify = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        identify = sdf.format(new Date());
        Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
        if (encode == null) {
//            数据库中没有第一条记录，则创建它
            Encode entry = new Encode();
            entry.set("contract_identify", 1).set("self_identify", 0).set("scene_identify", 0).save();
            identify = identify + "-" + String.format("%03d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify = identify + "-" + String.format("%03d", identify_Encode);
        }
        return identify;
    }

    /**
     * 任务列表已下达查看按钮
     **/

    public void check() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task.get("contract_id") != null) {
                Contract contract = Contract.contractDao.findById(task.get("contract_id"));
                if (contract != null) {
                    renderJson(contract.getItems());
                } else renderJson(RenderUtils.CODE_EMPTY);
            } else {
                if (task != null) {
                    renderJson(task.getItems());
                } else renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }

    }

    /**
     * 打印任务书
     */
    @Clear
    public void createTask() {
        try {
            String id = getPara("id");
            Task task = Task.taskDao.findFirst("SELECT * FROM `db_task` WHERE id=" + id);
            if (task != null) {
                getRequest().setAttribute("task", task);
                render("/template/create_task.jsp");
            } else renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据Task_id获取项目
     * *
     **/
    public void taskGetItems() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            if (task != null) {
                List<Company> companyList = Company.companydao.find("SELECT * FROM `db_company` WHERE task_id=" + task_id);
                List<Map> result = new ArrayList<>();
                for (Company company : companyList) {
                    result.add(company.toSimpleJSON());
                }
                renderJson(result);
            } else renderNull();

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void getByCompanyId() {
        try {
            int company_id = getParaToInt("company_id");
            Company company = Company.companydao.findById(company_id);
            if (company != null) {
                renderJson(company.toSimpleJSON());
            } else renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void getInspectList() {
        try {
            int id = getParaToInt("task_id");
            List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p\n" +
                    "WHERE t.id=" + id + " AND c.task_id=t.id AND i.company_id=c.id AND p.item_id=i.id");
            List<Map> result = new ArrayList<>();
            for (ItemProject itemProject : itemProjectList) {
                result.add(itemProject.toJsonSingle());
            }
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 创建送检单
     */
    public void createInspect() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int item_project_id = getParaToInt("item_project_id");
                    ItemProject itemProject = ItemProject.itemprojectDao.findById(item_project_id);
                    Boolean result = true;
                    result = result && itemProject.set("inspect", 1).update();

                    Task task = Task.taskDao.findFirst("SELECT t.* FROM `db_item_project` p,`db_item` i,`db_company` c ,`db_task` t\n" +
                            "WHERE p.item_id=i.id AND i.company_id=c.id AND c.task_id=t.id AND p.id=" + item_project_id);
                    if (task != null) {
                        Inspect inspect = new Inspect();
                        if (Inspect.inspectDao.find("SELECT * FROM `db_inspect` WHERE item_project_id=" + item_project_id).size() != 0) {
                            renderJson(RenderUtils.CODE_REPEAT);
                        } else {
                            result = inspect.set("item_project_id", item_project_id).set("type", getPara("type")).set("sender", task.get("sample_creater")).set("receive_time", task.get("receive_time")).set("receiver", task.get("sample_receiver")).set("sample_time", task.get("sample_time")).set("process", 0).save();
                        }
                        if (getPara("type") != null) {
                            List<SampleProject> sampleProjectList = SampleProject.sampleprojrctDao.find("SELECT p.* FROM `db_sample_project` p WHERE p.item_project_id=" + item_project_id);
                            for (SampleProject sampleProject : sampleProjectList) {
                                switch (getPara("type")) {
                                    case "water":
                                        InspectWater water = new InspectWater();
                                        result = result && water.set("sample_id", sampleProject.get("sample_id")).set("inspect_id", inspect.get("id")).set("process", 0).save();
                                        break;
                                    case "soil":
                                        InspectSoil soil = new InspectSoil();
                                        result = result && soil.set("sample_id", sampleProject.get("sample_id")).set("inspect_id", inspect.get("id")).set("process", 0).save();
                                        break;
                                    case "solid":
                                        InspectSoild soild = new InspectSoild();
                                        result = result && soild.set("sample_id", sampleProject.get("sample_id")).set("inspect_id", inspect.get("id")).set("process", 0).save();
                                        break;
                                    case "air":
                                        InspectAir air = new InspectAir();
                                        result = result && air.set("sample_id", sampleProject.get("sample_id")).set("inspect_id", inspect.get("id")).set("process", 0).save();
                                        break;
                                    case "dysodia":
                                        InspectDysodia dysodia = new InspectDysodia();
                                        result = result && dysodia.set("sample_id", sampleProject.get("sample_id")).set("inspect_id", inspect.get("id")).set("process", 0).save();
                                        break;
                                }
                            }
                            return result;
                        } else return false;
                    } else {
                        return false;
                    }

                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 导出送检单
     */
    public void exportInspect() {
        try {
            int inspect_id = getParaToInt("id");
            Inspect inspect = Inspect.inspectDao.findById(inspect_id);
            getRequest().setAttribute("inspect", inspect);
            render("/template/create_inspect.jsp");
        } catch (Exception e) {
            renderError(500);
        }
    }


}
