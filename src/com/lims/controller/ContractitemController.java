package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Contractitem;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/28.
 */
public class ContractitemController extends Controller {

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
            Page<Contractitem> contractitemPage = Contractitem.contractitemdao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_contract_item`" + param);
            List<Contractitem> contractitemList = contractitemPage.getList();
            Map results = toJson(contractitemList);
            results.put("currentPage", currentPage);
            results.put("totalPage", contractitemPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    public Map toJson(List<Contractitem> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (Contractitem contractitem : entityList) {
                results.add(toJsonSingle(contractitem));
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(Contractitem contractitem) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", contractitem.getInt("id"));
        item.put("company", contractitem.get("company"));
        item.put("point", contractitem.get("point"));
        item.put("contract_id", contractitem.get("contract_id"));
        item.put("other", contractitem.get("other"));
        item.put("is_package", contractitem.get("is_package"));
        return item;
    }

    public void create() {
        try {
            String company = getPara("company");
            String point = getPara("point");
            int contract_id = getParaToInt("contract_id");
            String other = getPara("other");
            int is_package = getParaToInt("is_package");
            if (Contractitem.contractitemdao.find("select * from `db_frequency` where company='" + company + "'and  point='" + point + "' and contract_id='" + contract_id + "' and other= '" + other + "' and is_package='" + is_package + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                Contractitem contractitem = new Contractitem();
                Boolean result = contractitem.set("company", company).set("point", point).set("contract_id", contract_id).set("other", other).set("is_package", is_package).save();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Contractitem.contractitemdao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void change() {
        try {
            int id = getParaToInt("id");
            String company = getPara("company");
            String point = getPara("point");
            int contract_id = getParaToInt("contract_id");
            String other = getPara("other");
            int is_package = getParaToInt("is_package");
            Contractitem contractitem = Contractitem.contractitemdao.findById(id);
            boolean result = contractitem.set("id", id).set("company", company).set("point", point).set("contract_id", contract_id).set("other", other).set("is_package", is_package).update();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findById() {
        try {
            Contractitem contractitem = Contractitem.contractitemdao.findById(getParaToInt("id"));
            if (contractitem != null) {
                renderJson(toJsonSingle(contractitem));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findByContract() {
        try {
            int contract_id = getParaToInt("contract_id");
            if (contract_id != 0) {
                List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_contract_item` where contract_id =" + contract_id);
                renderJson(toJson(contractitemList));
            } else {
                renderJson(RenderUtils.CODE_REPEAT);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void total() {
        try {
            renderJson(toJson(Contractitem.contractitemdao.find("SELECT * FROM `db_contract_item`")));
        } catch (Exception e) {
            renderError(500);
        }
    }
}



