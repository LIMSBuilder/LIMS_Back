package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by qulongjun on 2017/3/10.
 */
public class Task extends Model<Task> {
    public static Task taskDao = new Task();
}
