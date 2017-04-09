package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.lims.model.*;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责不同的日志处理
 */
public class LogController extends Controller {

    /**
     * public void contractLog() {
     * try {
     * int id = getParaToInt("id");
     * Contract contract = Contract.contractDao.findById(id);
     * List temp = new ArrayList();
     * if (contract != null) {
     * //                创建合同
     * Map process = new HashMap();
     * process.put("log_time", contract.get("create_time"));
     * User user = User.userDao.findById(contract.get("creater"));
     * if (user != null) {
     * process.put("log_msg", user.get("name") + "【创建】合同");
     * } else {
     * process.put("log_msg", "某人创建了合同");
     * }
     * temp.add(process);
     * <p>
     * if (contract.get("review_id") != null && contract.get("process")== -1)
     * {
     * //                    若review_id 不为null，即至少被审核拒绝过一次了
     * List<ContractReview> contractReviewList = ContractReview.contractReviewDao.find("SELECT * FROM `db_contract_review` WHERE contract_id=" + contract.get("id"));
     * for (ContractReview contractReview : contractReviewList) {
     * Map reviewProcess = new HashMap();
     * reviewProcess.put("log_msg", User.userDao.findById(contractReview.get("reviewer")).get("name") + "【审核拒绝】合同");
     * reviewProcess.put("log_time", contractReview.get("review_time"));
     * temp.add(reviewProcess);
     * }
     * }
     * if (contract.get("process") != -1 && contract.get("process") != 1 && contract.get("reviewer") != null) {
     * //                    若当前不在待修改或待审核状态，则说明当前审核通过
     * Map reviewProcess = new HashMap();
     * reviewProcess.put("log_msg", User.userDao.findById(contract.get("reviewer")).get("name") + "【审核通过】合同");
     * reviewProcess.put("log_time", contract.get("review_time"));
     * temp.add(reviewProcess);
     * }
     * <p>
     * renderJson(temp);
     * <p>
     * } else {
     * renderNull();
     * }
     * } catch (Exception e) {
     * renderError(500);
     * }
     * }
     * <p>
     * public void taskLog() {
     * try {
     * int id = getParaToInt("id");
     * Task task = Task.taskDao.findById(id);
     * List temp = new ArrayList();
     * if (task != null) {
     * //创建任务
     * Map process = new HashMap();
     * process.put("log_time", task.get("create_time"));
     * User user = User.userDao.findById(task.get("creater"));
     * if (user != null) {
     * process.put("log_msg", user.get("name") + "【创建】任务书");
     * } else {
     * process.put("log_msg", "某人创建了任务书");
     * }
     * temp.add(process);
     * renderJson(temp);
     * <p>
     * } else {
     * renderNull();
     * }
     * } catch (Exception e) {
     * renderError(500);
     * }
     * }
     **/
    //未测试

    /**
     * 合同日志
     **/
    public void contractLog() {
        try {
            int contract_id = getParaToInt("id");
            List<Log> logList = Log.logDao.find("select * from `db_log`  where contract_id = " + contract_id + " order by create_time  DESC");
            renderJson(toJson(logList));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<Log> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Log log : entityList) {
                result.add(toJsonSingle(log));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(Log log) {
        Map<String, Object> types = new HashMap<>();
        types.put("user", User.userDao.findById(log.getInt("user_id")).toSimpleJson());
        types.put("msg", log.get("msg"));
        types.put("create_time", log.get("create_time"));
        return types;
    }

    /**
     * 任务日志
     ***/
    public void taskLog() {
        try {
            int task_id = getParaToInt("id");
            List<Log> logList = Log.logDao.find("select * from `db_log`  where task_id = " + task_id + " order by create_time  DESC");
            renderJson(toJson(logList));


        } catch (Exception e) {
            renderError(500);
        }
    }
}
