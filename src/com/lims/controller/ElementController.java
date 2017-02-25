package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Department;
import com.lims.model.Element;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by caiwenhong on 2017/2/25.
 */
public class ElementController extends Controller {
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
            Page<Element> monitor_elementPage = Element.elementDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_element`" + param);
            List<Element> monitor_elementList = monitor_elementPage.getList();
            Map results = toJson(monitor_elementList);
            results.put("currentPage", currentPage);
            results.put("totalPage", monitor_elementPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    public Map toJson(List<Element> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Element monitor_element : entityList) {
                result.add(toJsonSingle(monitor_element));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Element monitor_element) {
        Map<String, Object> element = new HashMap<>();
        element.put("id", monitor_element.get("id"));
        element.put("name", monitor_element.get("name"));
        element.put("path", monitor_element.get("path"));
        return element;
    }

    public void create() {
        try {
            String name = getPara("name");
            String path = getPara("path");
            if (Element.elementDao.find("select * from `db_element` where name='" + name + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                Element element = new Element();
                boolean result = element.set("name", name).set("path", path).save();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            }
        } catch (Exception e) {
            renderError(500);
        }

    }

    public void delete() {
        try {
            int id = getParaToInt("id");
            boolean result = Element.elementDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findById() {
        try {
            int id = getParaToInt("id");
            Element monitor_element = Element.elementDao.findById(id);
            if (monitor_element != null) {
                renderJson(toJsonSingle(monitor_element));
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
            String name = getPara("name");
            String path = getPara("path");
            Element monitor_element = Element.elementDao.findById(id);
            monitor_element.set("id", id).set("name", name);
            if (!path.equals("")) {
                monitor_element.set("path", path);
            }
            boolean result = monitor_element.update();
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
                result = result && Element.elementDao.deleteById(id);
                if (!result) break;
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void total() {
        try {
            List<Element> elementList = Element.elementDao.find("select * from `db_element`");
            renderJson(toJson(elementList));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void deleteTemplate() {
        try {
            int id = getParaToInt("id");
            Element element = Element.elementDao.findById(id);
            if (element != null) {
                Boolean result = element.set("path", "").update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }


}
