package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.Map;

/**
 * Created by caiwenhong on 2017/3/1.
 */
public class Identify extends Model<Identify> {
    public  static Identify identifyDao =new Identify();
    public  int  getId(){
        return  this.getInt("contract_identify")+1;
    }
    public  Boolean setId(int id){
        Boolean result=this.findById(1).set("contract_identify",id).update();
        return  result;
    }

}
