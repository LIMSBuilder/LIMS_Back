package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.*;
import org.apache.poi.ss.formula.functions.T;
import org.junit.internal.runners.ClassRoadie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/6/19.
 */
public class CountProcess extends Controller {

    /***
     * 根据监测类别进行统计项目个数，并显示(可以一种也可以多种监测类别)
     * **/
    public void countMonitor() {
        try {
            Integer[] type = getParaValuesToInt("type[]");
            List<Map> results = new ArrayList<>();
            for (int id : type) {
                Map temp = new HashMap();
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_task` t,`db_company` c,`db_item` i,`db_item_project` p\n" +
                        "WHERE t.type=" + id + " AND c.task_id =t.id AND i.company_id =c.id  AND p.item_id=i.id");
                int count = 0;
                List<Map> map = new ArrayList<>();
                for (ItemProject itemProject : itemProjectList) {
                    count++;
                    map.add(itemProject.toJsonSingle());
                }
                temp.put("items", map);
                temp.put("count", count);
                results.add(temp);
            }
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }

    }

    /**
     * 统计自送样的样品个数
     ***/
    public void countSample() {
        try {
            String timeStart = getPara("timeStart");
            String timeEnd = getPara("timeEnd");
            Map total = new HashMap();
            List<Task> taskList = Task.taskDao.find("SELECT * FROM `db_task` WHERE create_time >= '" + timeStart + "'AND create_time<= '" + timeEnd + "'AND sample_type =0");
            List<Map> result = new ArrayList<>();
            if (taskList != null) {
                int count = 0;
                for (Task task : taskList) {
                    Map temp = new HashMap();
                    List<Sample> sampleList = Sample.sampleDao.find("SELECT * FROM `db_company` c,`db_sample` s WHERE c.task_id=" + task.get("id") + "s.company_id=c.id");
                    List<Map> map = new ArrayList<>();
                    for (Sample sample : sampleList) {
                        count++;
                        map.add(sample.toSimpleJson());
                    }
                    temp.put("count", count);
                    temp.put("sample", map);
                    result.add(temp);
                }
                renderJson(result);
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 根据合同编号查找合同
     * **/
    public void contract() {
        try {
            String identify = getPara("identify");
            Contract contract = Contract.contractDao.findFirst("SELECT * FROM `db_contract` WHERE identify =" + identify);
            if (contract != null) {
                renderJson(contract.Json());

            } else {
                ServiceContract serviceContract = ServiceContract.serviceContractDao.findFirst("SELECT * FROM `db_service_contract` WHERE identify =" + identify);
                if (serviceContract != null) {
                    renderJson(serviceContract.Json());
                } else {
                    renderNull();
                }
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     *根据任务编号查找任务
     * **/
    public void task() {
        try {
            String identify = getPara("identify");
            Task task = Task.taskDao.findFirst("SELECT * FROM `db_task` WHERE identify=" + identify);
            if (task != null) {
                renderJson(task.toJsonSingle());
            } else {
                renderNull();
            }

        } catch (Exception e) {
            renderError(500);
        }

    }

    /***
     * 根据委托公司找合同
     * **/
    public void clientUnit() {
        try {
            String clientUnit = getPara("clientUnit");
            List<Contract> contractList = Contract.contractDao.find("SELECT * FROM `db_contract` WHERE client_unit =" + clientUnit);
            List<Map> result = new ArrayList<>();

            if (contractList != null) {

                for (Contract contract : contractList) {
                    result.add(contract.Json());
                }

            }
            List<ServiceContract> serviceContractList = ServiceContract.serviceContractDao.find("SELECT * FROM `db_service_contract` WHERE name =" + clientUnit);
            if (serviceContractList != null) {
                for (ServiceContract serviceContract : serviceContractList) {
                    result.add(serviceContract.Json());
                }
            }
            renderJson(result);
            if ((contractList == null) && (serviceContractList == null)) {
                renderNull();
            }
        } catch (
                Exception e)

        {
            renderError(500);
        }
    }

    /***
     * 根据委托公司找到任务书
     * **/

    public void taskByClientunit() {
        try {
            String clientUnit = getPara("clientUnit");
            List<Task> taskList = Task.taskDao.find("SELECT * FROM `db_task` WHERE client_unit = " + clientUnit);
            List<Map> result = new ArrayList<>();
            if (taskList != null) {
                for (Task task : taskList) {
                    result.add(task.toJsonSingle());
                }
                renderJson(result);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 时间区间寻找合同
     * **/
    public void contractByTime() {
        try {
            String timeStart = getPara("timeStart");
            String timeEnd = getPara("timeEnd");
            List<Contract> contractList = Contract.contractDao.find("SELECT * FROM `db_contract` WHERE create_time >= '" + timeStart + "'AND create_time<= " + timeEnd);
            List<Map> result = new ArrayList<>();
            if (contractList != null) {
                for (Contract contract : contractList) {
                    result.add(contract.Json());
                }

            }
            List<ServiceContract> serviceContractList = ServiceContract.serviceContractDao.find("SELECT * FROM `db_service_contract` WHERE create_time >= '" + timeStart + "'AND create_time<= " + timeEnd);
            if (serviceContractList != null) {
                for (ServiceContract serviceContract : serviceContractList) {
                    result.add(serviceContract.Json());
                }

            }
            renderJson(result);
            if ((contractList == null) && (serviceContractList == null)) {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }

    }

    /***
     * 时间区间找任务
     * ***/
    public void taskByTime() {
        try {
            String timeStart = getPara("timeStart");
            String timeEnd = getPara("timeEnd");
            List<Task> taskList = Task.taskDao.find("SELECT * FROM `db_task` WHERE create_time >= '" + timeStart + "'AND create_time<= " + timeEnd);
            List<Map> result = new ArrayList<>();
            if (taskList != null) {
                for (Task task : taskList) {
                    result.add(task.toJsonSingle());
                }
                renderJson(result);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 根据价格找合同
     * **/
    public void contractByMoney() {
        try {
            String moneyStart = getPara("moneyStart");
            String moneyEnd = getPara("moneyEnd");
            List<Contract> contractList = Contract.contractDao.find("SELECT * FROM `db_contract` WHERE payment >= '" + moneyStart + "'AND payment <= " + moneyEnd);
            List<Map> result = new ArrayList<>();
            if (contractList != null) {
                for (Contract contract : contractList) {
                    result.add(contract.Json());
                }
                renderJson(result);
            } else {
                renderNull();
            }
        } catch (Exception e) {
            renderError(500);
        }
    }


}
