package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/28.
 */
public class Contractitem extends Model<Contractitem> {
    public static Contractitem contractitemdao = new Contractitem();

    public Map toSimpleJson() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("company_id", this.get("company_id"));
//        temp.put("contract_id", this.get("contract_id"));
        temp.put("other", this.get("other"));
        temp.put("element", Element.elementDao.findById(this.get("element")));
        temp.put("frequency", Frequency.frequencyDao.findById(this.get("frequency")));

//        temp.put("task_id", this.get("task_id"));
//        temp.put("process", this.get("process"));
        temp.put("point", this.get("point"));
//        temp.put("identify",Task.taskDao.findById(this.get("task_id")));


        List<Map> maps = new ArrayList<>();
        List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project` WHERE item_id=" + this.get("id"));
        for (ItemProject itemProject : itemProjectList) {
            MonitorProject monitorProject = MonitorProject.monitorProjectdao.findById(itemProject.get("project_id"));
            Map p = monitorProject.toJsonSingle();
            p.put("item_project_id",itemProject.get("id"));
            p.put("isPackage", itemProject.get("isPackage"));
            maps.add(p);
        }
        temp.put("project", maps);

        return temp;

    }
}
