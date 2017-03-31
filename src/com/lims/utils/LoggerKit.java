package com.lims.utils;

import com.lims.model.Contract;
import com.lims.model.Log;
import com.lims.model.Task;
import com.lims.model.User;

import java.util.Date;

/**
 * Created by chenyangyang on 2017/3/31.
 */
public class LoggerKit {
    /**
     * 合同日志接口
     *
     * @param contractId
     * @param msg
     * @param userId
     * @return
     */
    public static boolean addContractLog(int contractId, String msg, int userId) {
        Contract contract = Contract.contractDao.findById(contractId);
        if (contract != null) {
            Log log = new Log();
            Boolean result = log.set("contract_id", contractId).set("msg", msg).set("user_id", userId).set("create_time", ParaUtils.sdf.format(new Date())).save();
            return result;
        } else return false;
    }

    public static boolean addTaskLog(int taskId, String msg, int userId) {
        Task task = Task.taskDao.findById(taskId);
        if (task != null) {
            Log log = new Log();
            Boolean result = log.set("task_id", taskId).set("msg", msg).set("user_id", userId).set("create_time", ParaUtils.sdf.format(new Date())).save();
            return result;
        } else return false;
    }
}

