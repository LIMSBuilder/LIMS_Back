package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/24.
 */
public class User extends Model<User> {
    public static User userDao = new User();

    public Map toSimpleJson() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("name", this.get("name"));
        temp.put("nick", this.get("nick"));
        return temp;
    }
}
