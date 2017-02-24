package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by caiwenhong on 2017/2/24.
 */
public class Role extends Model<Role> {
    public static  Role roledao=new Role();
    public  Department getDepartment(){
        return  Department.departmentdao.findById(get("department_id"));
    }
}
