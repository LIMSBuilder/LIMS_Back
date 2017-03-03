package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/24.
 */
public class Role extends Model<Role> {
    public static Role roledao = new Role();

    public Department getDepartment() {
        return Department.departmentdao.findById(get("department_id"));
    }

    public Map toJSON() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("name", this.get("name"));
        temp.put("department", this.getDepartment());
        return temp;
    }
}
