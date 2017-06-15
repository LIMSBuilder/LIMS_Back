package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/6/9.
 */
public class RecordFirstReview extends Model<RecordFirstReview> {
    public static RecordFirstReview recordFirstReviewDao = new RecordFirstReview();

    public Map toJSON() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("condition1", this.get("condition1"));
        temp.put("condition2", this.get("condition2"));
        temp.put("condition3", this.get("condition3"));
        temp.put("condition4", this.get("condition4"));
        temp.put("condition5", this.get("condition5"));
        temp.put("condition6", this.get("condition6"));
        temp.put("remark", this.get("remark") == null ? null : this.get("remark"));
        temp.put("creater",User.userDao.findById(this.get("creater")).get("name"));
        temp.put("task_id",this.get("task_id"));
        temp.put("flag",this.get("flag"));
        return temp;
    }
}
