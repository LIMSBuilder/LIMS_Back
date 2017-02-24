package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by caiwenhong on 2017/2/24.
 */
public class User extends Model<User> {
    public static User userDao = new User();
}
