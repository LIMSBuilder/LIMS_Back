package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by qulongjun on 2017/2/26.
 */
public class Customer extends Model<Customer> {
    public static Customer customerDao = new Customer();
}
