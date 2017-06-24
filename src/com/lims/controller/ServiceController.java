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

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by qulongjun on 2017/4/28.
 */
public class ServiceController extends Controller {
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
                if (key.equals("state")) { //process=wait_change
                    switch (value.toString()) {
                        case "total":
                            param += "AND review = 1";
                            break;
                        case "before_review":
                            param += "AND (state = " + ProcessKit.getServiceProcess("create") + " ) AND  review = 1";
                            break;
                        case "after_review":
                            param += "AND (state = " + ProcessKit.getServiceProcess("review") + ") AND review = 1";
                            break;
                        case "finish_contract":
                            param += "AND (state = " + ProcessKit.getServiceProcess("finish") + ") AND review = 1";
                            break;
                        case "change_contract":
                            param += "AND (state = " + ProcessKit.getServiceProcess("change") + ") AND review = 1";
                            break;
                        case "stop_contract":
                            param += "AND (state = " + ProcessKit.getServiceProcess("stop") + ") AND review = 1";
                            break;
                        default:
                            param += " AND " + key + " = " + value;
                    }
                    continue;
                }

                if (key.equals("keyWords")) {
                    param += (" AND ( identify ='" + value + "' OR name like \"%" + value + "%\" )");
                    continue;
                }
                if (key.equals("review_me")) {
                    User user = ParaUtils.getCurrentUser(getRequest());
                    param += " AND reviewer =" + user.get("id");
                    continue;
                }
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<ServiceContract> contractPage = ServiceContract.serviceContractDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_service_contract`" + param + " ORDER BY create_time DESC");
            List<ServiceContract> contractList = contractPage.getList();
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

    public Map toJson(List<ServiceContract> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (ServiceContract contract : entityList) {
                result.add(toJsonSingle(contract));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(ServiceContract entry) {
        Map temp = new HashMap();
        temp.put("id", entry.get("id"));
        temp.put("path", entry.get("path"));
        temp.put("name", entry.get("name"));
        temp.put("review", entry.get("review"));
        temp.put("state", entry.get("state"));
        temp.put("creater", User.userDao.findById(entry.get("creater")).toSimpleJson());
        temp.put("create_time", entry.get("create_time"));
        temp.put("identify", entry.get("identify"));
        return temp;
    }


    /**
     * 上传服务合同
     */
    public void createService() {
        try {
            boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int review = getParaToInt("review");//是否技术评审
                    String path = getPara("path");//服务合同路径
                    String name = getPara("name");//合同名称
                    ServiceContract serviceContract = new ServiceContract();
                    boolean result = true;
                    if (getParaToInt("review") == 1) {
                        result = result && serviceContract.set("state", ProcessKit.getServiceProcess("create")).set("path", path)
                                .set("name", name)
                                .set("path", path)
                                .set("review", review)
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("create_time", ParaUtils.sdf.format(new Date()))
                                .set("identify", createIdentify()).save();
                    } else {
                        result = result && serviceContract.set("state", ProcessKit.getServiceProcess("review")).set("path", path)
                                .set("name", name)
                                .set("review", review)
                                .set("path", path)
                                .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("create_time", ParaUtils.sdf.format(new Date()))
                                .set("identify", createIdentify()).save();
                    }
                    LoggerKit.addServiceContractLog(serviceContract.getInt("id"), "创建合同", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 修改服务合同
     **/
    public void change() {
        try {
            int id = getParaToInt("id");
            Boolean result = true;
            int review = getParaToInt("review");//是否技术评审
            String path = getPara("path");//服务合同路径
            String name = getPara("name");//合同名称
            ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(id);
            if (serviceContract != null) {
                if (getParaToInt("review") == 1) {
                    result = result && serviceContract.set("id", id).set("state", ProcessKit.getServiceProcess("create")).set("path", path)
                            .set("name", name)
                            .set("review", review)
                            .set("path", path)
                            .set("changer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                            .set("update_time", ParaUtils.sdf.format(new Date()))
                            .update();
                } else {
                    result = result && serviceContract.set("id", id).set("state", ProcessKit.getServiceProcess("review")).set("path", path)
                            .set("name", name)
                            .set("review", review)
                            .set("path", path)
                            .set("changer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                            .set("update_time", ParaUtils.sdf.format(new Date()))
                            .update();

                }
                LoggerKit.addServiceContractLog(serviceContract.getInt("id"), "创建合同", ParaUtils.getCurrentUser(getRequest()).getInt("id"));

                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据Id 获取数据
     **/
    public void findDetailsList() {
        try {
            int id = getParaToInt("id");
            ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(id);
            if (serviceContract != null) {
                List<ServiceContract> serviceContractList = ServiceContract.serviceContractDao.find("SELECT * FROM `db_service_contract` where id = " + id);
                renderJson(toJsonSingle1(serviceContract));
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJsonSingle1(ServiceContract entry) {
        Map temp = new HashMap();
        temp.put("id", entry.get("id"));
        temp.put("path", entry.get("path"));
        temp.put("name", entry.get("name"));
        temp.put("review", entry.get("review"));
        return temp;
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
            identify = identify + "-" + String.format("%03d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify = identify + "-" + String.format("%03d", identify_Encode);
        }
        return identify;
    }

    /**
     * 审核合同
     * *
     **/
    public void review() {
        try {
            boolean result = Db.tx(new IAtom() {
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
                    int result1 = getParaToInt("result");
                    ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(id);
                    if (serviceContract != null) {
                        User user = ParaUtils.getCurrentUser(getRequest());
                        ContractReview contractReview = new ContractReview();
                        Boolean result = true;
                        result = result && contractReview.set("service_id", serviceContract.getInt("id")).set("reject_msg", getPara("msg")).set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("same", same).set("contract", contract1).set("guest", guest).set("package", pack).set("company", company).set("money", money).set("time", time).set("result", result1).save();
                        if (!result) return false;
                        if (getParaToInt("result") == 1) {
                            LoggerKit.addServiceContractLog(serviceContract.getInt("id"), "审核通过", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                            result = result && serviceContract.set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("state", ProcessKit.getContractProcess("review")).set("review_id", contractReview.get("id")).update();

                        } else {
                            LoggerKit.addServiceContractLog(serviceContract.getInt("id"), "审核拒绝", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                            result = result && serviceContract.set("reviewer", user.get("id")).set("review_time", ParaUtils.sdf.format(new Date())).set("review_id", contractReview.get("id")).set("state", ProcessKit.getContractProcess("change")).update();
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

    /***
     * 中止合同
     * */

    public void stopContract() {
        try {

            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(id);
                    boolean result = true;
                    boolean taskresult = true;
                    if (serviceContract != null) {
                        result = serviceContract.set("state", -2).update();
                        Task task = Task.taskDao.findFirst("select * from `db_task` where contract_id =" + serviceContract.get("id"));
                        if (task != null) {
                            taskresult = task.set("process", ProcessKit.TaskMap.get("stop")).update();
                        }
                    }
                    LoggerKit.addServiceContractLog(serviceContract.getInt("id"), "中止了合同", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
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


    /***
     * 完成合同
     * */
    public void finishContract() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(id);
                    Boolean result = true;
                    if (serviceContract != null && serviceContract.getInt("process") == 2) {
                        result = serviceContract.set("state", 3).update();
                    }
                    LoggerKit.addServiceContractLog(serviceContract.getInt("id"), "完成合同", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 根据编号找到合同
     **/
    public void findById() {
        try {
            int id = getParaToInt("id");
            ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(id);
            if (serviceContract != null) {
                renderJson(toJsonSingle(serviceContract));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 通过ID查找服务合同细节列表
     **/
    public void serviceContractDetails() {
        try {
            int id = getParaToInt("id");
            ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(id);
            if (serviceContract != null) {
                renderJson(toJsonSingle(serviceContract));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }


        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 根据ID删除合同
     **/
    public void deleteserviceContract() {
        try {
            int id = getParaToInt("id");
            Boolean result = ServiceContract.serviceContractDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_EMPTY);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 统计待审核数量
     **/
    public void countreview() {
        try {
            List<ServiceContract> serviceContractList = ServiceContract.serviceContractDao.find("select * from `db_service_contract` where state = " + ProcessKit.getServiceProcess("create") + " AND review = 1 ");
            Map temp = new HashMap();
            temp.put("count", serviceContractList.size());
            renderJson(temp);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 获取所有审核记录
     * 包括通过的和拒绝的
     */
    public void getReviewServiceList() {
        try {
            ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(getPara("id"));
            if (serviceContract.getInt("state") != 1 && serviceContract.get("reviewer") != null) {
                //只要不是1就表示已经进入过流程了,判断reviewer是否存在防止中止的情况
                List<ContractReview> contractReviewList = ContractReview.contractReviewDao.find("SELECT * FROM `db_contract_review` WHERE service_id=" + serviceContract.get("id"));
                Map temp = toReviewJson(contractReviewList);
                if (serviceContract.getInt("state") > 1) {
                    Map acept = new HashMap();
                    acept.put("reviewer", User.userDao.findById(serviceContract.get("reviewer")).toSimpleJson());
                    acept.put("review_time", serviceContract.get("review_time"));
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
     * 下载合同
     **/
    public void downLoadServiceContract() {
        try {
            int service_id = getParaToInt("service_id");
            ServiceContract serviceContract = ServiceContract.serviceContractDao.findById(service_id);
            if (serviceContract != null) {
                renderJson(serviceContract.Json());
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }
}
