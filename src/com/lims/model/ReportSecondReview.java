package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/6/17.
 */
public class ReportSecondReview extends Model<ReportSecondReview> {
  public  static  ReportSecondReview reportSecondReview =new ReportSecondReview();
  public Map Json(){
    Map temp =new HashMap();
    temp.put("id",this.get("id"));
    temp.put("condition1",this.get("condition1"));
    temp.put("condition2",this.get("condition2"));
    temp.put("condition3",this.get("condition3"));
    temp.put("condition4",this.get("condition4"));
    temp.put("condition5",this.get("condition5"));
    temp.put("condition6",this.get("condition6"));
    temp.put("reviewer",User.userDao.findById(this.get("reviewer")).get("name"));
    temp.put("other",this.get("other"));
    temp.put("review_time",this.get("review_time"));
    temp.put("report_id",this.get("report_id"));
    return  temp;
  }
}

