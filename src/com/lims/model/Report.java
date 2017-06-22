package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/6/17.
 */
public class Report extends Model<Report> {
    public static Report report = new Report();

    public Map Json() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("company_id", this.get("company_id"));
        temp.put("type", Element.elementDao.findById(this.get("type")));
        temp.put("report_path", this.get("report_path"));
        temp.put("creater", User.userDao.findById(this.getInt("creater")).toSimpleJson());
        temp.put("create_time", this.get("create_time"));
        temp.put("flag", this.get("flag"));
        return temp;
    }
}
