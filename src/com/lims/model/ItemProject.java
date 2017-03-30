package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/28.
 */
public class ItemProject extends Model<ItemProject> {
    public static ItemProject itemprojectDao = new ItemProject();

    public Map toJsonSingle() {
        Map temp = new HashMap();
        MonitorProject monitorProject = MonitorProject.monitorProjectdao.findById(this.get("project_id"));
        return monitorProject.toJsonSingle();
    }
}
