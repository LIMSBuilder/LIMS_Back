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
        Role role = Role.roledao.findById(this.get("roleId"));
        temp.put("id", this.get("id"));
        temp.put("name", this.get("name"));
        temp.put("nick", this.get("nick"));
        temp.put("portrait", this.get("portrait"));
        temp.put("role", role.toJSON());
        temp.put("cardId",this.get("cardId"));
//        temp.put("lastLogin", this.get("lastLogin"));
        return temp;
    }
}
