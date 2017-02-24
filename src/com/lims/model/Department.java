package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/24.
 */
public class Department extends Model<Department> {
    public static Department departmentdao = new Department();

    public Map toJsonSingle() {
        Map result = new HashMap();
        result.put("id", this.getInt("id"));
        result.put("name", this.get("name"));
        return result;
    }
}
