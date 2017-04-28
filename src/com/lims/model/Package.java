package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/4/28.
 */
public class Package extends Model<Package> {
    public static Package packageDao = new Package();

    public Map toSingleJson() {
        Map temp = new HashMap();
        for (String key : this._getAttrNames()) {
            temp.put(key, this.get(key));
        }
        return temp;
    }
}
