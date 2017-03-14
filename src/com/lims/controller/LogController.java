package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.Contract;
import com.lims.model.ContractReview;
import com.lims.model.Task;
import com.lims.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责不同的日志处理
 */
public class LogController extends Controller {

    public void contractLog() {
        try {
            int id = getParaToInt("id");
            Contract contract = Contract.contractDao.findById(id);
            List temp = new ArrayList();
            if (contract != null) {
                //创建合同
                Map process = new HashMap();
                process.put("log_time", contract.get("create_time"));
                User user = User.userDao.findById(contract.get("creater"));
                if (user != null) {
                    process.put("log_msg", user.get("name") + "【创建】合同");
                } else {
                    process.put("log_msg", "某人创建了合同");
                }
                temp.add(process);

                if (contract.get("review_id") != null) {
                    //若review_id 不为null，即至少被审核拒绝过一次了
                    List<ContractReview> contractReviewList = ContractReview.contractReviewDao.find("SELECT * FROM `db_contract_review` WHERE contract_id=" + contract.get("id"));
                    for (ContractReview contractReview : contractReviewList) {
                        Map reviewProcess = new HashMap();
                        reviewProcess.put("log_msg", User.userDao.findById(contractReview.get("reviewer")).get("name") + "【审核拒绝】合同");
                        reviewProcess.put("log_time", contractReview.get("review_time"));
                        temp.add(reviewProcess);
                    }
                }
                if (contract.get("process") != -1 && contract.get("process") != 1 && contract.get("reviewer") != null) {
                    //若当前不在待修改或待审核状态，则说明当前审核通过
                    Map reviewProcess = new HashMap();
                    reviewProcess.put("log_msg", User.userDao.findById(contract.get("reviewer")).get("name") + "【审核通过】合同");
                    reviewProcess.put("log_time", contract.get("review_time"));
                    temp.add(reviewProcess);
                }

                renderJson(temp);

            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void taskLog() {
        try {
            int id = getParaToInt("id");
            Task task = Task.taskDao.findById(id);
            List temp = new ArrayList();
            if (task != null) {
                //创建任务
                Map process = new HashMap();
                process.put("log_time", task.get("create_time"));
                User user = User.userDao.findById(task.get("creater"));
                if (user != null) {
                    process.put("log_msg", user.get("name") + "【创建】任务书");
                } else {
                    process.put("log_msg", "某人创建了任务书");
                }
                temp.add(process);
                renderJson(temp);

            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }
}
