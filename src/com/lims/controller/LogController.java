package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.Contract;
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
                    process.put("log_msg", user.get("name") + "创建了合同");
                } else {
                    process.put("log_msg", "某人创建了合同");
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
