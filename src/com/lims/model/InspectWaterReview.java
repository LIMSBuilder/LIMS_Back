package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/6/8.
 */
public class InspectWaterReview extends Model<InspectWaterReview> {
    public  static  InspectWaterReview inspectWaterReviewDao =new InspectWaterReview();

    public Map toJSON() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("Name",User.userDao.findById(this.get("user_id")).get("name"));
        temp.put("create_time",this.get("create_time"));
        temp.put("result",this.get("result"));
        temp.put("type",this.get("type"));
        temp.put("water_id",this.get("water_id"));
        temp.put("remark",this.get("remark"));
        return temp;
    }
}
