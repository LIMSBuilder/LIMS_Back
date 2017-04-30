package com.lims.utils;

import com.lims.model.*;

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

    public static boolean addItemLog(int ItemId, String msg, int userId) {
        ItemProject itemProject = ItemProject.itemprojectDao.findById(ItemId);
        if (itemProject != null) {
            Log log = new Log();
            Boolean result = log.set("item_id", ItemId).set("msg", msg).set("user_id", userId).set("create_time", ParaUtils.sdf.format(new Date())).save();
            return  result;
        } else return false;
    }

    public static boolean addSampleLog(int sampleId,String msg, int userId){
        Sample sample =Sample.sampleDao.findById(sampleId);
        if(sample != null){
            Log log = new Log();
            Boolean result = log.set("sample_id",sampleId ).set("msg", msg).set("user_id", userId).set("create_time", ParaUtils.sdf.format(new Date())).save();
            return  result;
        }
        else  return  false;
    }
    public static boolean addServiceContractLog(int serviceContractId,String msg, int userId){
       ServiceContract serviceContract=ServiceContract.serviceContractDao.findById(serviceContractId);
        if(serviceContract!= null){
            Log log = new Log();
            Boolean result = log.set("serviceContract_id",serviceContractId).set("msg", msg).set("user_id", userId).set("create_time", ParaUtils.sdf.format(new Date())).save();
            return  result;
        }
        else  return  false;
    }
}

