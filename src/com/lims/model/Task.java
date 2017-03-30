package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qulongjun on 2017/3/10.
 */
public class Task extends Model<Task> {
    public static Task taskDao = new Task();
    public Map getItems() {
        Map temp = new HashMap();
        List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_contract_item` WHERE task_id=" + this.get("id"));
        List<Map> maps = new ArrayList<>();
        for (Contractitem item : contractitemList) {
            Map result = new HashMap();
            for (String t : item._getAttrNames()) {
                if (t.equals("element")) {
                    result.put("element", item.get(t) == null ? null : Element.elementDao.findById(item.get(t)));
                    continue;
                }
                if (t.equals("frequency")) {
                    result.put("frequency", item.get(t) == null ? null : Frequency.frequencyDao.findById(item.get(t)).toJsonSingle());
                    continue;
                }
                result.put(t, item.get(t));
            }
         /**   List<ItemProject> projectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project` WHERE item_id=" + item.get("id"));
            List<Map> mapList = new ArrayList<>();
            for (ItemProject project : projectList) {
                Map t = new HashMap();
                t.put("id", project.get("id"));
                t.put("project", MonitorProject.monitorProjectdao.findById(project.get("project_id")));
                t.put("item_id", project.get("item_id"));
                mapList.add(t);
            }
            result.put("project", mapList);**/
            maps.add(result);
        }
        temp.put("items", maps);
        return temp;
    }


}
