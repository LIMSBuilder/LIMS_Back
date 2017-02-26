package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Customer;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qulongjun on 2017/2/26.
 */
public class CustomerController extends Controller {
    public void create() {
        try {
            Customer customer = new Customer();
            if (Customer.customerDao.find("SELECT * FROM `db_customer` WHERE client_unit='" + getPara("client_unit") + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
                return;
            }
            Boolean result = customer
                    .set("client_unit", getPara("client_unit"))
                    .set("client", getPara("client"))
                    .set("client_address", getPara("client_address"))
                    .set("client_tel", getPara("client_tel"))
                    .set("client_fax", getPara("client_fax"))
                    .set("client_code", getPara("client_code"))
                    .save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

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
                if (key.equals("keyword")) {
                    param += ("AND ( client_unit  like \"%" + value + "%\" OR client  like \"%" + value + "%\" OR client_address  like \"%" + value + "%\" OR client_tel  like \"%" + value + "%\" OR client_code  like \"%" + value + "%\" OR client_fax like \"%" + value + "%\"  ) ");
                    continue;
                }
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Customer> customerPage = Customer.customerDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_customer`" + param);
            List<Customer> customerList = customerPage.getList();
            Map results = toJson(customerList);
            results.put("currentPage", currentPage);
            results.put("totalPage", customerPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    public Map toJson(List<Customer> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Customer customer : entityList) {
                result.add(toJsonSingle(customer));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(Customer customer) {
        Map<String, Object> types = new HashMap<>();
        types.put("id", customer.getInt("id"));
        types.put("client_unit", customer.get("client_unit"));
        types.put("client", customer.get("client"));
        types.put("client_address", customer.get("client_address"));
        types.put("client_tel", customer.get("client_tel"));
        types.put("client_fax", customer.get("client_fax"));
        types.put("client_code", customer.get("client_code"));
        return types;
    }


    public void change() {
        try {
            int id = getParaToInt("id");
            Customer customer = Customer.customerDao.findById(id);
            if (customer != null) {
                Boolean result = customer
                        .set("client_unit", getPara("client_unit"))
                        .set("client", getPara("client"))
                        .set("client_address", getPara("client_address"))
                        .set("client_tel", getPara("client_tel"))
                        .set("client_fax", getPara("client_fax"))
                        .set("client_code", getPara("client_code"))
                        .update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delete() {
        try {
            Boolean result = Customer.customerDao.deleteById(getPara("id"));
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void deleteAll() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    Integer[] selected = getParaValuesToInt("selected[]");
                    for (int id : selected) {
                        result = result && Customer.customerDao.deleteById(id);
                        if (!result) break;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findById() {
        try {
            Customer customer = Customer.customerDao.findById(getParaToInt("id"));
            renderJson(toJsonSingle(customer));
        } catch (Exception e) {
            renderError(500);
        }
    }
}
