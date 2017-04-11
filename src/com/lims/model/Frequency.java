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


    public Map toJsonSingle() {
        Map<String, Object> frequency = new HashMap<>();
        frequency.put("id", this.get("id"));
        frequency.put("count", this.get("count"));
        frequency.put("times", this.get("times"));
        frequency.put("unit", this.get("unit"));
        frequency.put("notice", this.get("notice"));
        frequency.put("total", this.get("total"));
        return frequency;
    }

}
