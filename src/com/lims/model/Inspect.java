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
        result.put("id",this.get("id"));
        result.put("sender",User.userDao.findById(this.get("sender")).get("name"));
        result.put("receiver",User.userDao.findById(this.get("receiver")).get("name"));
        result.put("receive_time",this.get("receive_time"));
        result.put("send_time",this.get("sample_time"));
        result.put("type",this.get("type"));
        result.put("process",this.get("process"));
        return result;
    }
}
