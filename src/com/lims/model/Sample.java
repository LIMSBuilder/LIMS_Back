package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/3/29.
 */
public class Sample extends Model<Sample> {
    public static Sample sampleDao = new Sample();

    public Map toSimpleJson() {
        Map temp = new HashMap();
        for (String key : this._getAttrNames()) {
            if (key.equals("balance")) {
                temp.put("balance", Sample.sampleDao.findById(this.get("balance")));
                continue;
            }
            temp.put(key, this.get(key));
        }
        List<SampleProject> sampleProjectList = SampleProject.sampleprojrctDao.find("SELECT * FROM `db_sample_project` WHERE sample_id=" + this.get("id"));
        List<Map> projectList = new ArrayList<>();
        for (SampleProject sampleProject : sampleProjectList) {
            ItemProject itemProject = ItemProject.itemprojectDao.findById(sampleProject.get("item_project_id"));
            MonitorProject project = MonitorProject.monitorProjectdao.findById(itemProject.get("project_id"));
            Map t = project.toJsonSingle();
            t.put("item_project_id", itemProject.get("id"));
            projectList.add(t);
        }
        temp.put("project", projectList);
        return temp;

    }
}
