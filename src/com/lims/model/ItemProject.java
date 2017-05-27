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
        for (String name : monitorProject._getAttrNames()) {
            if (name.equals("element_id")) {
                temp.put("element", Element.elementDao.findById(monitorProject.get("element_id")));
            }
            temp.put(name, monitorProject.get(name));
        }
        temp.put("project_id",this.get("project_id"));
        temp.put("id",this.get("id"));
        temp.put("isPackage", this.get("isPackage"));
        temp.put("item_project_id", this.get("id"));
        temp.put("process", this.get("process"));
        temp.put("flag",this.get("flag"));
        Inspect inspect = Inspect.inspectDao.findFirst("SELECT * FROM `db_inspect` WHERE item_project_id=" + this.get("id"));
        temp.put("inspect", inspect);
//        return monitorProject.toJsonSingle();
        return temp;
    }
}
