package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/5/14.
 */
public class InspectSoil extends Model<InspectSoil> {
    public static InspectSoil inspectSoilDao = new InspectSoil();


    public Map toJSON() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("sample", Sample.sampleDao.findById(this.get("sample_id")));
        temp.put("result", this.get("result"));
        temp.put("point", this.get("point"));
        temp.put("remark", this.get("remark"));
        temp.put("inspect_id", this.get("inspect_id"));
        temp.put("process", this.get("process"));
        temp.put("type", "soil");
        return temp;
    }
}