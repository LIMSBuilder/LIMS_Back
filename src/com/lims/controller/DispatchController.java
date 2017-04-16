package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenyangyang on 2017/4/15.
 */
public class DispatchController extends Controller {

    /**
     * 任务派遣创建接口
     */
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    int charge_id = getParaToInt("charge_id");
                    String date = ParaUtils.sdf2.format(new Date());
                    Integer[] join_id = getParaValuesToInt("join_id[]");
                    String project = getPara("project");
                    Dispatch dispatch = new Dispatch();
                    result = result && dispatch.set("charge_id", charge_id).set("date", date).save();
                    for (int id : join_id) {
                        Dispatch_Joiner dispatch_joiner = new Dispatch_Joiner();
                        result = result && dispatch_joiner.set("dispatch_id", dispatch.get("id")).set("join_id", id).save();
                        if (!result) return false;
                    }
                    List<Map> itemList = Jackson.getJson().parse(project, List.class);
                    for (Map item : itemList) {
                        List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_contract_item` WHERE task_id=" + item.get("task_id") + " AND company = '" + item.get("company") + "'");
                        for (Contractitem contractitem : contractitemList) {
                            Dispatch_Item dispatch_item = new Dispatch_Item();
                            result = result && dispatch_item.set("dispatch_id", dispatch.get("id")).set("item_id", contractitem.get("id")).set("date", date).save();
                            if (!result) return false;
                        }
                        if (!result) return false;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    //
    public void list() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            if (rowCount == 0) {
                rowCount = ParaUtils.getRowCount();
            }
            String param = " ";
            Object[] keys = condition.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                Object value = condition.get(key);
                param += (" AND " + key + " like \"%" + value + "%\"");
            }

            Page<Dispatch_Item> dispatch_itemPage = Dispatch_Item.dispatchItemDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_dispatch_item` where date=" + ParaUtils.sdf2.format(new Date()));
            List<Dispatch_Item> dispatch_itemList = dispatch_itemPage.getList();
            Map results = toJson(dispatch_itemList);
            results.put("currentPage", currentPage);
            results.put("totalPage", dispatch_itemPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<Dispatch_Item> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (Dispatch_Item dispatch_item : entityList) {
                results.add(toJsonSingle(dispatch_item));
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(Dispatch_Item dispatch_item) {
        Map<String, Object> project = new HashMap<>();
        project.put("id", dispatch_item.getInt("id"));


        return project;
    }


    /**
     * 获取个人派遣任务列表
     */
    public void UserDispatchList() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            User user = ParaUtils.getCurrentUser(getRequest());
            Page<Dispatch_Item> dispatch_itemPage = Dispatch_Item.dispatchItemDao.paginate(currentPage, rowCount, "SELECT i.*", " FROM `db_dispatch_item` i,`db_dispatch` d,`db_dispatch_join` j WHERE d.id=i.dispatch_id AND i.date='" + ParaUtils.sdf2.format(new Date()) + "' AND (d.charge_id=" + user.get("id") + " OR (d.id=j.dispatch_id AND j.join_id=" + user.get("id") + ")) " + condition);
            List<Dispatch_Item> dispatch_itemList = dispatch_itemPage.getList();
            Map results = toJson(dispatch_itemList);
            results.put("currentPage", currentPage);
            results.put("totalPage", dispatch_itemPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }

}
