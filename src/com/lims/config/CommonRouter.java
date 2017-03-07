package com.lims.config;

import com.jfinal.config.Routes;
import com.lims.controller.*;

/**
 * 路由页面
 */
public class CommonRouter extends Routes {
    @Override
    public void config() {
        //API Config
        add("/api/department", DepartmentController.class);
        add("/api/role", RoleController.class);
        add("/api/user", UserController.class);
        add("/api/element", ElementController.class);
        add("api/file", FileController.class);
        add("/api/frequency", FrequencyController.class);
        add("/api/type", TypeController.class);
        add("/api/customer", CustomerController.class);
        add("/api/project", MonitorProjectController.class);

        add("/api/contract", ContractController.class);

        add("/api/calendar", CalendarController.class);
        add("/api/mail", MailController.class);
//        add("/api/contract_item", ContractitemController.class);
//        add("/api/itemproject", ItemProjectController.class);

        add("/api/login", LoginController.class);
        add("/api/log", LogController.class);
    }
}


