package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.Calendar;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.*;

/**
 * 日程计划
 * 0-正常显示
 */
public class CalendarController extends Controller {
    public void create() {
        try {
            Calendar calendar = new Calendar();
            calendar.set("title", getPara("title"))
                    .set("desp", getPara("desp"))
                    .set("start", getPara("start"))
                    .set("end", getPara("end"))
                    .set("backgroundColor", getPara("backgroundColor"))
                    .set("create_time", ParaUtils.sdf.format(new Date()))
                    .set("state", 0)
                    .set("user_id", ParaUtils.getCurrentUser(getRequest()).get("id"));
            renderJson(calendar.save() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void list() {
        try {
            List<Calendar> calendarList = Calendar.calendarDao.find("SELECT * FROM `db_calendar` WHERE user_id=" + ParaUtils.getCurrentUser(getRequest()).get("id"));
            renderJson(toJson(calendarList));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<Calendar> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (Calendar calendar : entityList) {
                Map temp = toJsonSingle(calendar);
                results.add(temp);
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Calendar calendar) {
        Map<String, Object> map = new HashMap<>();
        for (String key : calendar._getAttrNames()) {
            map.put(key, calendar.get(key));
        }
        return map;
    }

    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Calendar.calendarDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void drop() {
        try {
            int id = getParaToInt("id");
            Calendar calendar = Calendar.calendarDao.findById(id);
            if (calendar != null) {
                Boolean result = calendar.set("start", getPara("start")).set("end", getPara("end")).update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else renderJson(RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }
}
