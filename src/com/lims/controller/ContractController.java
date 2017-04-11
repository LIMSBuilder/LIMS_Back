package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;

import com.lims.utils.*;
import org.junit.Test;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public class ContractController extends Controller {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formate_date = new SimpleDateFormat("yyyy-MM-dd");

    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Map paraMaps = getParaMap();
                    Contract contract = new Contract();
                    Boolean result = true;
                    System.out.println(paraMaps.get("item[]"));
                    for (Object key : paraMaps.keySet()) {
                        switch (key.toString()) {
                            case "in_room":
                                contract.set("in_room", ((String[]) paraMaps.get(key))[0].equals("true") ? 1 : 0);
                                break;
                            case "secret":
                                contract.set("secret", ((String[]) paraMaps.get(key))[0].equals("true") ? 1 : 0);
                                break;
                            case "id":
                                break;//不知道为什么会传一个id过来，待观察
                            default:
                                if (key.toString().indexOf("item") != -1) {
                                    continue;
                                }
                                contract.set(key.toString(), ((String[]) paraMaps.get(key))[0]);
                        }

                    }
                    User user = ParaUtils.getCurrentUser(getRequest());
                    contract.set("identify", createIdentify()).set("create_time", sdf.format(new Date())).set("creater", user.get("id")).set("process", ProcessKit.getContractProcess("create"));
                    result = result && contract.save();
                    String[] items = getParaValues("project_items[]");
                    for (String item : items) {
                        Map temp = Jackson.getJson().parse(item, Map.class);
                        Contractitem contractitem = new Contractitem();

//                        List points = (ArrayList) temp.get("point");
//                        String point = "";
//                        if (points != null) {
//                            for (int i = 0; i < points.size(); i++) {
//                                point += points.get(i);
//                                if (i != points.size() - 1) {
//                                    point += ",";
//                                }
//                            }
//                        }
                        result = result && contractitem.set("element", ((Map) temp.get("element")).get("id")).set("company", temp.get("company")).set("point", temp.get("point")).set("contract_id", contract.get("id")).set("other", temp.get("other")).set("frequency", ((Map) (temp.get("frequency"))).get("id")).save();
                        if (!result) break;
                        List<Map> projectList = (ArrayList) temp.get("project");
                        if (projectList != null) {
                            for (int m = 0; m < projectList.size(); m++) {
                                Map project = projectList.get(m);
                                ItemProject entry = new ItemProject();
                                entry.set("item_id", contractitem.get("id")).set("project_id", project.get("id"));
                                entry.set("isPackage", project.get("isPackage") != null && project.get("isPackage") == true ? 1 : 0);
                                result = result && entry.save();
                                if (!result) break;
                            }
                        }
                        if (!result) break;
                    }
                    //{{user}}于{{create_time}}创建了合同
                    LoggerKit.addContractLog(contract.getInt("id"), "创建了合同", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 合同编号生成
     * <p>
     * 年份+ - + 4位流水编号，如 2017-001  2017-002  以此类推
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
            identify = identify + "-" + String.format("%04d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify = identify + "-" + String.format("%04d", identify_Encode);
        }
        return identify;
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
                if (key.equals("process")) { //process=wait_change
                    switch (value.toString()) {
//                        case "after_receivesmall":
//                            param += " AND (process = " + ProcessKit.getContractProcess("review") + ") AND payment<50000 ";
//                            break;
//                        case "after_receiveBig":
//                            param += " AND (process = " + ProcessKit.getContractProcess("review") + ") AND payment>50000 ";
//                            break;
//                        case "reviewsmall":
//                            小额审核
//                            param += "AND process != " + ProcessKit.getContractProcess("stop") + " AND process !=" + ProcessKit.getContractProcess("finish") + " AND payment<50000";
//                            break;
//                        case "reviewBig":
//                            大额审核
//                            param += "AND process != " + ProcessKit.getContractProcess("stop") + " AND process !=" + ProcessKit.getContractProcess("finish") + " AND payment>50000";
//                            break;
                        case "reviewBig":
                            param += "AND payment>50000";
                            break;
                        case "waitReviewBig":
                            param += " AND (process = " + ProcessKit.getContractProcess("create") + ") AND payment>50000 ";
                            break;
                        case "afterReviewBig":
                            param += " AND (process = " + ProcessKit.getContractProcess("review") + ") AND payment>50000 ";
                            break;
                        case "beforeReviewBig":
                            param += " AND (process = " + ProcessKit.getContractProcess("change") + ") AND payment>50000 ";
                            break;
                        case "reviewSmall":
                            param += "AND payment<50000";
                            break;
                        case "waitReviewSmall":
                            param += " AND (process = " + ProcessKit.getContractProcess("create") + ") AND payment<50000 ";
                            break;
                        case "afterReviewSmall":
                            param += " AND (process = " + ProcessKit.getContractProcess("review") + ") AND payment<50000 ";
                            break;
                        case "beforeReviewSmall":
                            param += " AND (process = " + ProcessKit.getContractProcess("change") + ") AND payment<50000 ";
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
                if (key.equals("review_me")) {
                    User user = ParaUtils.getCurrentUser(getRequest());
                    param += " AND reviewer =" + user.get("id");
                    continue;
                }
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Contract> contractPage = Contract.contractDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_contract`" + param + " ORDER BY create_time DESC");
            List<Contract> contractList = contractPage.getList();
            Map results = toJson(contractList);
            results.put("currentPage", currentPage);
            results.put("totalPage", contractPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    public Map toJson(List<Contract> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Contract contract : entityList) {
                result.add(toJsonSingle(contract));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Contract entry) {
        Map temp = new HashMap();
        /**for (String key : entry._getAttrNames()) {
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

         }**/
        temp.put("id", entry.get("id"));
        temp.put("process", entry.get("process"));
        temp.put("identify", entry.get("identify"));
        temp.put("name", entry.get("name"));
        temp.put("aim", entry.get("aim"));
        temp.put("create_time", entry.get("create_time"));
        temp.put("client_unit", entry.get("client_unit"));
        return temp;
    }


    public void getItems() {
        try {
            int id = getParaToInt("contract_id");
            Contract contract = Contract.contractDao.findById(id);
            if (contract != null) {
                renderJson(contract.getItems());
            } else renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Contract.contractDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findById() {
        try {
            int id = getParaToInt("id");
            Contract contract = Contract.contractDao.findById(id);
            if (contract != null) {
                renderJson(toJsonSingle(contract));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findByIdentify() {
        try {
            String identify = getPara("identify");

            Contract contract = Contract.contractDao.findFirst("select * from db_contracter where identify = '" + identify + "'");
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void defaultInfo() {
        try {
            Default defaultModel = Default.defaultDao.findFirst("SELECT * FROM `db_default`");
            Boolean result = true;
            if (defaultModel != null) {
                //更新
                defaultModel
                        .set("trustee_unit", getPara("trustee_unit"))
                        .set("trustee_address", getPara("trustee_address"))
                        .set("trustee_tel", getPara("trustee_tel"))
                        .set("trustee_code", getPara("trustee_code"))
                        .set("trustee_fax", getPara("trustee_fax"));
                result = result && defaultModel.update();
            } else {
                //创建新的Default
                Default temp = new Default();
                temp
                        .set("trustee_unit", getPara("trustee_unit"))
                        .set("trustee_address", getPara("trustee_address"))
                        .set("trustee_tel", getPara("trustee_tel"))
                        .set("trustee_code", getPara("trustee_code"))
                        .set("trustee_fax", getPara("trustee_fax"));
                result = result && temp.save();
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void fetchDefault() {
        try {
            Default defaultModel = Default.defaultDao.findFirst("SELECT * FROM `db_default`");
            if (defaultModel != null) {
                renderJson(defaultModel);
            } else renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 大额审核通过
     ***/
    public void review() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    int same = getParaToInt("same");
                    int contract1 = getParaToInt("contract");
                    int guest = getParaToInt("guest");
                    int pack = getParaToInt("package");
                    int company = getParaToInt("company");
                    int money = getParaToInt("money");
                    int time = getParaToInt("time");
                    int result1 = getParaToInt("result");// 1-accept 0-reject
                    Contract contract = Contract.contractDao.findById(id);
                    if (contract != null) {
                        User user = ParaUtils.getCurrentUser(getRequest());
                        ContractReview contractReview = new ContractReview();
                        Boolean result = true;
                        result = result && contractReview.set("contract_id", contract.get("id")).set("reject_msg", getPara("msg")).set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("same", same).set("contract", contract1).set("guest", guest).set("package", pack).set("company", company).set("money", money).set("time", time).set("result", result1).save();
                        if (!result) return false;
                        if (getParaToInt("result") == 1) {
                            LoggerKit.addContractLog(contract.getInt("id"), "审核通过", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                            result = result && contract.set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("process", ProcessKit.getContractProcess("review")).set("review_id", contractReview.get("id")).update();

                        } else {
                            LoggerKit.addContractLog(contract.getInt("id"), "审核拒绝", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                            result = result && contract.set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("review_id", contractReview.get("id")).set("process", ProcessKit.getContractProcess("change")).set("review_id", contractReview.get("id")).update();
                        }
                        return result;
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
     * 小额审核通过
     **/
    public void reviewsmall() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    int result1 = getParaToInt("result");// 1-accept 0-reject
                    Contract contract = Contract.contractDao.findById(id);
                    if (contract != null) {
                        User user = ParaUtils.getCurrentUser(getRequest());
                        ContractReview contractReview = new ContractReview();
                        Boolean result = true;
                        result = result && contractReview.set("contract_id", contract.get("id")).set("reject_msg", getPara("msg")).set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("result", result1).save();
                        if (!result) return false;
                        if (getParaToInt("result") == 1) {
                            LoggerKit.addContractLog(contract.getInt("id"), "审核通过", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                            result = result && contract.set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("process", ProcessKit.getContractProcess("review")).set("review_id", contractReview.get("id")).update();

                        } else {
                            LoggerKit.addContractLog(contract.getInt("id"), "审核拒绝", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                            result = result && contract.set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("review_id", contractReview.get("id")).set("process", ProcessKit.getContractProcess("change")).set("review_id", contractReview.get("id")).update();
                        }
                        return result;
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
     * 获取所有审核记录
     * 包括通过的和拒绝的
     */
    public void getReviewList() {
        try {
            Contract contract = Contract.contractDao.findById(getPara("id"));
            if (contract.getInt("process") != 1 && contract.get("reviewer") != null) {
                //只要不是1就表示已经进入过流程了,判断reviewer是否存在防止中止的情况
                List<ContractReview> contractReviewList = ContractReview.contractReviewDao.find("SELECT * FROM `db_contract_review` WHERE contract_id=" + contract.get("id"));
                Map temp = toReviewJson(contractReviewList);
                if (contract.getInt("process") > 1) {
                    Map acept = new HashMap();
                    acept.put("reviewer", User.userDao.findById(contract.get("reviewer")).toSimpleJson());
                    acept.put("review_time", contract.get("review_time"));
                    temp.put("accept", acept);
                }
                renderJson(temp);
            } else
                renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }


    public Map toReviewJson(List<ContractReview> contractReviewList) {
        List temp = new ArrayList();
        for (ContractReview contractReview : contractReviewList) {
            temp.add(toReviewJsonSingle(contractReview));
        }
        Map result = new HashMap();
        result.put("result", temp);
        return result;
    }

    public Map toReviewJsonSingle(ContractReview contractReview) {
        Map temp = new HashMap();
        temp.put("id", contractReview.get("id"));
        temp.put("msg", contractReview.get("reject_msg"));
        temp.put("reviewer", User.userDao.findById(contractReview.get("reviewer")).toSimpleJson());
        temp.put("review_time", contractReview.get("review_time"));
        temp.put("result", contractReview.get("result"));
        return temp;
    }


    /**
     * 获取大额当前共有多少条需审核记录
     */
    public void getWaitReviewCount() {
        try {
            List<Contract> contractList = Contract.contractDao.find("SELECT * FROM `db_contract` WHERE process=" + ProcessKit.getContractProcess("create") + " and payment>50000 ");
            Map temp = new HashMap();
            temp.put("count", contractList.size());
            renderJson(temp);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 获取小额当前共有多少条需审核记录
     */
    public void getWaitSmallReviewCount() {
        try {
            List<Contract> contractList = Contract.contractDao.find("SELECT * FROM `db_contract` WHERE process=" + ProcessKit.getContractProcess("create") + " and payment<50000 ");
            Map temp = new HashMap();
            temp.put("count", contractList.size());
            renderJson(temp);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据review_id获取review对象记录（单个）
     */
    public void reviewDetail() {
        try {
            ContractReview contractReview = ContractReview.contractReviewDao.findById(getPara("id"));
            if (contractReview != null) {
                renderJson(toReviewJsonSingle1(contractReview));
            } else
                renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toReviewJsonSingle1(ContractReview contractReview) {
        Map temp = new HashMap();
        temp.put("id", contractReview.get("id"));
        temp.put("msg", contractReview.get("reject_msg"));
        temp.put("same", contractReview.get("same"));
        temp.put("contract", contractReview.get("contract"));
        temp.put("guest", contractReview.get("guest"));
        temp.put("package", contractReview.get("package"));
        temp.put("company", contractReview.get("company"));
        temp.put("money", contractReview.get("money"));
        temp.put("time", contractReview.get("time"));
        temp.put("reviewer", User.userDao.findById(contractReview.get("reviewer")).toSimpleJson());
        temp.put("review_time", contractReview.get("review_time"));
        temp.put("result", contractReview.get("result"));
        return temp;
    }

    /**
     *
     */
    public void contractDetails() {
        try {
            Contract contract = Contract.contractDao.findById(getPara("id"));
            if (contract != null) {
                renderJson(toContractDetailJSON(contract));
            } else
                renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * @param entry
     * @return
     */
    public Map toContractDetailJSON(Contract entry) {
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

    public void countProcess() {
        try {
            Map temp = ProcessKit.ContractMap;
            Map result = new HashMap();
            for (Object key : temp.keySet()) {
                int count = Contract.contractDao.find("SELECT * FROM `db_contract` WHERE process=" + temp.get(key)).size();
                result.put(key, count);
            }
            //total
            result.put("total", Contract.contractDao.find("SELECT * FROM `db_contract`").size());
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void deleteContract() {
        try {
            int id = getParaToInt("id");
            Boolean result = Contract.contractDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void stopContract() {
        try {

            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    Contract contract = Contract.contractDao.findById(id);
                    boolean result = true;
                    boolean taskresult = true;
                    if (contract != null) {
                        result = contract.set("process", -2).update();
                        Task task = Task.taskDao.findFirst("select * from `db_task` where contract_id =" + contract.get("id"));
                        if (task != null) {
                            taskresult = task.set("process", ProcessKit.TaskMap.get("stop")).update();
                        }
                    }
                    LoggerKit.addContractLog(contract.getInt("id"), "中止了合同", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result && taskresult;
                }
            });

            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (
                Exception e)

        {
            renderError(500);
        }
    }


    public void route() {
        renderJsp("/index.jsp");
    }

    /**
     * 打印合同
     */
    public void createContract() {
        try {
            String id = getPara("id");
            Contract contract = Contract.contractDao.findFirst("SELECT * FROM `db_contract` WHERE id=" + id);
            if (contract != null) {
                getRequest().setAttribute("contract", contract);
                render("/template/create_contract.jsp");
            } else renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void readItemFile() {
        try {
            String path = getPara("path");
            ExcelRead read = new ExcelRead();
            String[] titles = {"id", "company", "element", "pointList", "projectList", "frequency"};
            List<Map> result = read.readExcel(path, titles);
            List returnBack = new ArrayList();
            for (Map temp : result) {
                String companyStr = temp.get("company").toString();
                String elementStr = temp.get("element").toString();
                String[] pointList = temp.get("pointList").toString().split(" ");
                String[] projectList = temp.get("projectList").toString().split(" ");
                String frequencyStr = temp.get("frequency").toString();
                Map json = new HashMap();
                Map freMap = new HashMap();
                freMap.put("total", frequencyStr);
                json.put("frequency", freMap);
                json.put("company", companyStr);
                json.put("other", companyStr);
                Element element = Element.elementDao.findFirst("SELECT * FROM `db_element` WHERE name='" + elementStr + "'");
                if (element != null) {
                    json.put("element", element);
                }
                json.put("point", pointList.length);
                List projectTemp = new ArrayList();
                for (String projectName : projectList) {
                    MonitorProject project = MonitorProject.monitorProjectdao.findFirst("SELECT * FROM `db_monitor_project` WHERE name='" + projectName + "'");
                    if (project != null) {
                        projectTemp.add(project);
                    }
                }
                json.put("project", projectTemp);
                returnBack.add(json);
            }
            renderJson(returnBack);
        } catch (Exception e) {
            renderError(500);
        }
    }

}
