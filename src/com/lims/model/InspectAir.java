package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/5/14.
 */
public class InspectAir extends Model<InspectAir> {
    public static InspectAir inspectAir = new InspectAir();

    public Map toJSON() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("sample", Sample.sampleDao.findById(this.get("sample_id")));
        temp.put("result", this.get("result"));
        temp.put("volume", this.get("volume"));
        temp.put("concentration", this.get("concentration"));
        temp.put("inspect_id", this.get("inspect_id"));
        temp.put("process", this.get("process"));
        temp.put("type", "air");
        temp.put("flag", this.get("flag") == null ? null : this.get("flag"));
        temp.put("flag2", this.get("flag2") == null ? null : this.get("flag2"));
        temp.put("flag3", this.get("flag3") == null ? null : this.get("flag3"));
        return temp;
    }
}
