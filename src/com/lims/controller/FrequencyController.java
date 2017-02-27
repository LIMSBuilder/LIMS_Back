package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Frequency;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by caiwenhong on 2017/2/25.
 */
public class FrequencyController extends Controller {
    public void list() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            if (rowCount == 0) {
                rowCount = ParaUtils.getRowCount();
            }
            String param = " WHERE 1=1 ";
            Object[] keys = condition.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                Object value = condition.get(key);
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Frequency> frequencyPage = Frequency.frequencyDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_frequency`" + param);
            List<Frequency> frequencyList = frequencyPage.getList();
            Map results = toJson(frequencyList);
            results.put("currentPage", currentPage);
            results.put("totalPage", frequencyPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);

        } catch (Exception e) {
            renderError(500);
        }
    }


    public Map toJson(List<Frequency> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (Frequency monitor_frequency : entityList) {
                results.add(toJsonSingle(monitor_frequency));
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Frequency monitor_frequency) {
        Map<String, Object> frequency = new HashMap<>();
        Map<String, Object> total = new HashMap<>();
        frequency.put("id", monitor_frequency.get("id"));
        frequency.put("count", monitor_frequency.get("count"));
        frequency.put("times", monitor_frequency.get("times"));
        frequency.put("unit", monitor_frequency.get("unit"));
        frequency.put("notice", monitor_frequency.get("notice"));
        String value = " ";
        if (monitor_frequency.get("unit").equals("one")) {
            value = "仅" + monitor_frequency.get("count") + "次";
        } else {
            String unit = Frequency.UnitMap.get(monitor_frequency.get("unit")).toString();
            value = monitor_frequency.get("count") + "次/" + monitor_frequency.get("times") + unit;

        }

        frequency.put("total", value);
        return frequency;
    }

    public void create() {
        try {
            int count = getParaToInt("count");
            int times = getParaToInt("times");
            String unit = getPara("unit");
            int notice = getParaToInt("notice");
            if (Frequency.frequencyDao.find("select * from `db_frequency` where count='" + count + "'and  times='" + times + "' and unit='" + unit + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                Frequency frequency = new Frequency();
                boolean result = frequency.set("count", count).set("times", times).set("unit", unit).set("notice", notice).save();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Frequency.frequencyDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findById() {
        try {
            int id = getParaToInt("id");
            Frequency frequency = Frequency.frequencyDao.findById(id);
            if (frequency != null) {
                renderJson(toJsonSingle(frequency));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);

            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void change() {
        try {
            int id = getParaToInt("id");
            int count = getParaToInt("count");
            int times = getParaToInt("times");
            String unit = getPara("unit");
            int notice = getParaToInt("notice");
            Frequency frequency = Frequency.frequencyDao.findById(id);
            boolean result = frequency.set("id", id).set("count", count).set("times", times).set("unit", unit).set("notice", notice).update();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void deleteAll() {
        try {
            Integer[] selected = getParaValuesToInt("selected[]");
            Boolean result = true;
            for (int i = 0; i < selected.length; i++) {
                int id = selected[i];
                result = result && Frequency.frequencyDao.deleteById(id);
                if (!result) break;
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }
}