package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/5/21.
 */
public class Description extends Model<Description> {
    public  static Description descriptionDao =new Description();
    public Map toJsonSingle() {
        Map result = new HashMap();
        result.put("id", this.getInt("id"));
        result.put("saveCharacter", this.get("saveCharacter"));
        result.put("saveState",this.get("saveState"));
        result.put("process",this.get("process"));
        return result;
    }
}
