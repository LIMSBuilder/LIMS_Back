package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/5/14.
 */
public class Inspect extends Model<Inspect> {
    public static Inspect inspectDao = new Inspect();

    public Map toSingleJson() {
        Map result = new HashMap();
        result.put("id", this.get("id"));
        result.put("sender", User.userDao.findById(this.get("sender")).get("name"));
        result.put("receiver", User.userDao.findById(this.get("receiver")).get("name"));
        result.put("receive_time", this.get("receive_time"));
        result.put("send_time", this.get("sample_time"));
        result.put("type", this.get("type"));
        result.put("process", this.get("process"));
        result.put("analyst", this.get("analyst") == null ? null : User.userDao.findById(this.get("analyst")).get("name"));
        result.put("analysis_time", this.get("analysis_time"));
        result.put("checker", this.get("checker") == null ? null : User.userDao.findById(this.get("checker")).get("name"));
        result.put("reviewer", this.get("reviewer") == null ? null : User.userDao.findById(this.get("reviewer")).get("name"));
        result.put("check_time", this.get("check_time"));
        result.put("review_time", this.get("review_time"));
        return result;
    }
}
