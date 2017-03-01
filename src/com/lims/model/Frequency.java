package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/25.
 */
public class Frequency extends Model<Frequency> {
    public static Frequency frequencyDao = new Frequency();
    public static Map UnitMap = new HashMap() {{
        put("minute", "分钟");
        put("hour", "小时");
        put("day", "天");
        put("week", "周");
        put("month", "月");
        put("quarter", "季");
        put("year", "年");

    }};

}
