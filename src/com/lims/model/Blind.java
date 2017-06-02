package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/5/4.
 */
//盲样
public class Blind extends Model <Blind> {
    public  static  Blind blindDao =new Blind();
    public Map toJsonSingle(){
        Map result =new HashMap();
        result.put("id",this.get("id"));
        result.put("task_id",this.get("task_id"));
        result.put("project_id",this.get("project_id"));
        result.put("blind",this.get("blind"));
        return result;
    }

}
