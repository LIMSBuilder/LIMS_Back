package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/5/14.
 */
public class Inspect extends Model<Inspect> {
    public static Inspect inspectDao = new Inspect();

    public  Map toSingleJson(){
        Map result = new HashMap();
        result.put("type",this.get("type"));
        return result;
    }
}
