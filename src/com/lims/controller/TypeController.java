package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Contract;
import com.lims.model.Task;
import com.lims.model.Type;
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
public class TypeController extends Controller {
    public void create() {
        try {
            Type type = new Type();
            if (Type.typeDao.find("SELECT * FROM `db_type` WHERE name='" + getPara("name") + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
                return;
            }
            Boolean result = type.set("name", getPara("name")).set("identifier", getPara("identifier")).save();
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
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Type> typePage = Type.typeDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_type`" + param);
            List<Type> typeList = typePage.getList();
            Map results = toJson(typeList);
            results.put("currentPage", currentPage);
            results.put("totalPage", typePage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    public Map toJson(List<Type> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Type type : entityList) {
                result.add(toJsonSingle(type));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(Type type) {
        Map<String, Object> types = new HashMap<>();
        types.put("id", type.getInt("id"));
        types.put("name", type.get("name"));
        types.put("identifier", type.get("identifier"));
        return types;
    }


    public void delete() {
        try {
            Boolean result = Type.typeDao.deleteById(getPara("id"));
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
                        result = result && Type.typeDao.deleteById(id);
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
            Type type = Type.typeDao.findById(getParaToInt("id"));
            renderJson(toJsonSingle(type));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void change() {
        try {
            Type type = Type.typeDao.findById(getPara("id"));
            if (type != null) {
                Boolean result = type.set("name", getPara("name")).set("identifier", getPara("identifier")).update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else {
                render(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void total() {
        try {
            renderJson(toJson(Type.typeDao.find("SELECT * FROM `db_type`")));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void contract_total() {
        try {
            renderJson(toJsonContract(Type.typeDao.find("SELECT * FROM `db_type`")));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJsonContract(List<Type> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Type type : entityList) {
                result.add(toJsonContractSingle(type));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonContractSingle(Type type) {
        Map<String, Object> types = new HashMap<>();
        types.put("id", type.getInt("id"));
        types.put("name", type.get("name"));
        types.put("identifier", type.get("identifier"));
        types.put("contract_count", Contract.contractDao.find("SELECT * FROM `db_contract` WHERE type=" + type.get("id")).size());
        return types;
    }

    public void task_total() {

        try {
            renderJson(toJsonTask(Type.typeDao.find("SELECT * FROM `db_type`")));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJsonTask(List<Type> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Type type : entityList) {
                result.add(toJsonTaskSingle(type));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonTaskSingle(Type type) {
        Map<String, Object> types = new HashMap<>();
        types.put("id", type.getInt("id"));
        types.put("name", type.get("name"));
        types.put("identifier", type.get("identifier"));
        types.put("contract_count", Task.taskDao.find("SELECT * FROM `db_task` WHERE type=" + type.get("id")).size());
        return types;
    }

}
