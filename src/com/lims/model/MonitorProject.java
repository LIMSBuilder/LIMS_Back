package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/27.
 */
public class MonitorProject extends Model<MonitorProject> {
    public static MonitorProject monitorProjectdao = new MonitorProject();

    public Map toJsonSingle() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("name", this.get("name"));
        // temp.put("desp", this.get("desp"));
        //temp.put("department", Department.departmentdao.findById(this.get("department_id")));
        // temp.put("element", Element.elementDao.findById("element_id"));
//        temp.put("isPackage",this.get("isPackage"));
        return temp;
    }
}
