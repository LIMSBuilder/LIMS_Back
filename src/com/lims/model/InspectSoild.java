package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/5/14.
 */
public class InspectSoild extends Model<InspectSoild> {
    public static InspectSoild inspectSoildDao = new InspectSoild();

    public Map toJSON() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("sample", Sample.sampleDao.findById(this.get("sample_id")));
        temp.put("result", this.get("result"));
        temp.put("volume", this.get("volume"));
        temp.put("flow", this.get("flow"));
        temp.put("concentration", this.get("concentration"));
        temp.put("discharge", this.get("discharge"));
        temp.put("inspect_id", this.get("inspect_id"));
        temp.put("process", this.get("process"));
        temp.put("type", "solid");
        temp.put("flag", this.get("flag") == null ? null : this.get("flag"));
        temp.put("flag2", this.get("flag2") == null ? null : this.get("flag2"));
        temp.put("flag3", this.get("flag3") == null ? null : this.get("flag3"));
        return temp;
    }
}
