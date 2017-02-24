package com.lims.config;

import com.jfinal.config.Routes;
import com.lims.controller.DepartmentController;
import com.lims.controller.RoleController;

/**
 * 路由页面
 */
public class CommonRouter extends Routes {
    @Override
    public void config() {
        //API Config
        add("/api/department", DepartmentController.class);
        add("/api/role", RoleController.class);

    }
}
