package com.lims.config;

import com.jfinal.config.Routes;
import com.lims.controller.*;
import com.lims.model.Dispatch;
import com.lims.model.Package;

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
        add("/api/task", TaskController.class);
        add("/api/login", LoginController.class);
        add("/api/log", LogController.class);
        add("/api/sample", SampleController.class);
        add("/api/item", ItemController.class);
        add("/api/dispatch", DispatchController.class);
        add("/api/delivery", DeliveryController.class);
        add("/api/power", PowerController.class);
        add("/api/equip", EquipmentController.class);
        add("/api/package", PackageController.class);
        add("/api/service", ServiceController.class);
        add("/api/company", CompanyController.class);
        add("/api/quality", QualityController.class);
        add("/api/lab",LabController.class);
        add("/api/certificate",CertificateController.class);
        add("/api/review",ReviewController.class);
    }
}


