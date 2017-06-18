package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/6/17.
 */
public class Report extends Model<Report> {
    public static Report report=new Report();

    public Map Json(){
        Map temp =new HashMap();
        temp.put("id",this.get("id"));
        temp.put("company_id",this.get("company_id"));
        temp.put("type",this.get("type"));
        temp.put("report_path",this.get("report_path"));
        temp.put("creater",this.get("signer")==null?null:User.userDao.findById(this.get("signer")).get("name"));
        temp.put("create_time",this.get("sign_time"));
        temp.put("flag",this.get("flag"));
        return temp;
    }
}
