package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.Contract;
import com.lims.model.ServiceContract;
import com.lims.model.Task;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/8/12.
 */
public class TotalController extends Controller {
    public void contract() {
        try {
            Map total = new HashMap();
            List<Map> temp = new ArrayList<>();
            List<Contract> contractList = Contract.contractDao.find("select * from `db_contract`");
            for (Contract contract : contractList) {
                temp.add(contract.Json());
            }
            List<Map> service = new ArrayList<>();
            List<ServiceContract> serviceContractList = ServiceContract.serviceContractDao.find("select * from ``db_service_contract");
            for (ServiceContract serviceContract : serviceContractList) {
                service.add(serviceContract.Json());
            }
            total.put("contract", temp);
            total.put("service", service);
            renderJson(total);
        } catch (Exception e) {
            renderError(500);
        }

    }


    public void task() {
        try {
            int contract_id = getParaToInt("contract_id");
            Map total = new HashMap();
            List<Task> taskList = Task.taskDao.find("select * from `db_task` where contract_id = '" + contract_id + "'OR  service_id=" + contract_id);
            if (taskList.size() == 0) {
                renderJson(RenderUtils.CODE_EMPTY);
            } else {
                List<Map> temp = new ArrayList<>();
                for (Task task : taskList) {
                    temp.add(task.toJsonSingle());
                    total.put("task", temp);
                }
                renderJson(total);
            }
        } catch (Exception e) {
            renderError(500);
        }

    }

    public void tasklist() {
        try {
            int task_id = getParaToInt("task_id");
            Task task = Task.taskDao.findById(task_id);
            Map total = new HashMap();
            if (task != null) {


            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }
}
