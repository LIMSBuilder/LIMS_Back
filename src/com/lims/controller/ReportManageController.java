package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Element;
import com.lims.model.Report;
import com.lims.model.ReportManage;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/6/18.
 */
public class ReportManageController extends Controller {
    /***
     * 创建报告模块(保存)
     * **/
    public void create() {
        try {
            String name = getPara("name");
            String path = getPara("path");
            if (ReportManage.reportManage.find("select * from `db_report_manage` where name='" + name + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                ReportManage reportManage = new ReportManage();
                boolean result = reportManage.set("name", name).set("path", path).save();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 删除模板
     **/
    public void delete() {
        try {
            int id = getParaToInt("id");
            boolean result = ReportManage.reportManage.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 显示LIst
     **/
    public void List() {
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
            Page<ReportManage> reportManagePage = ReportManage.reportManage.paginate(currentPage, rowCount, "SELECT *", "FROM `db_report_manage`" + param);
            List<ReportManage> reportManageList = reportManagePage.getList();
            Map results = toJson(reportManageList);
            results.put("currentPage", currentPage);
            results.put("totalPage", reportManagePage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<ReportManage> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (ReportManage reportManage : entityList) {
                result.add(toJsonSingle(reportManage));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(ReportManage reportManage) {
        Map<String, Object> report = new HashMap<>();
        report.put("id", reportManage.get("id"));
        report.put("name", reportManage.get("name"));
        report.put("path", reportManage.get("path"));
        return report;
    }

    /**
     * 修改报告模板
     **/
    public void change() {
        try {
            int id = getParaToInt("id");
            String name = getPara("name");
            String path = getPara("path");
            ReportManage reportManage = ReportManage.reportManage.findById(id);
            reportManage.set("id", id).set("name", name);
            if (!path.equals("")) {
                reportManage.set("path", path);
            }
            boolean result = reportManage.update();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 删除所有
     * **/
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

    /**
     * 显示所有
     **/
    public void total() {
        try {
            List<ReportManage> reportManageList = ReportManage.reportManage.find("select * from `db_report_manage`");
            renderJson(toJson(reportManageList));
        } catch (Exception e) {
            renderError(500);
        }
    }

    /***
     * 删除模板
     * **/
    public void deleteTemplate() {
        try {
            int id = getParaToInt("id");
            ReportManage reportManage = ReportManage.reportManage.findById(id);
            if (reportManage != null) {
                Boolean result = reportManage.set("path", "").update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

}
