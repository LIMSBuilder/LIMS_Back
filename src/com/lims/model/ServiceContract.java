package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/4/27.
 */
public class ServiceContract extends Model<ServiceContract> {
    public static ServiceContract serviceContractDao = new ServiceContract();
    public Map Json() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("identify", this.get("identify"));
        temp.put("path", this.get("path"));
        temp.put("name", this.get("name"));
        temp.put("review", this.get("review"));
        temp.put("state", this.get("state"));
        temp.put("review_id", this.get("review_id") == null ? null : User.userDao.findById(this.get("review_id")).get("name"));
        temp.put("review_time", this.get("review_time"));
        temp.put("create_time", this.get("create_time"));
        temp.put("creater", this.get("creater") == null ? null : User.userDao.findById(this.get("creater")).get("name"));
        temp.put("reviewer", this.get("reviewer") == null ? null : User.userDao.findById(this.get("reviewer")).get("name"));
        temp.put("update_time", this.get("update_time"));
        temp.put("changer", this.get("changer") == null ? null : User.userDao.findById(this.get("changer")).get("name"));
        return temp;
    }

}
