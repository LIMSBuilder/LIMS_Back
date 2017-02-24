package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Department;
import com.lims.model.Role;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/24.
 */
public class RoleController extends Controller {
    public void list() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            if (rowCount == 0) {
                rowCount = ParaUtils.getRowCount();
            }
            String param = " where 1=1 ";
            Object[] keys = condition.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                Object value = condition.get(key);
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Role> rolePage = Role.roledao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_role`" + param);
            List<Role> roleList = rolePage.getList();
            Map results = toJson(roleList);
            results.put("currentPage", currentPage);
            results.put("rowCount", rowCount);
            results.put("totalPage", rolePage.getTotalPage());
            results.put("concdition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<Role> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (Role role : entityList) {
                Map temp = toJsonSingle(role);
                results.add(temp);
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Role role) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getInt("id"));
        map.put("name", role.get("name"));
        //roleList.put("department", role.getDepartment());
        map.put("department", Department.departmentdao.findById(role.getInt("department_id")).toJsonSingle());
        return map;
    }

    public void create() {
        try {
            String name = getPara("name");
            int department_id = getParaToInt("department_id");
            Role role = new Role();
            if (name != null && department_id != 0) {
                if (Role.roledao.find("select * from `db_role` where name= '" + name + "' and department_id=" + department_id).size() != 0) {
                    renderJson(RenderUtils.CODE_REPEAT);
                    return;
                }
            }
            boolean result = role.set("name", name).set("department_id", department_id).save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delete() {
        try {
            int id = getParaToInt("id");
            boolean result = Role.roledao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void change() {
        try {
            int id = getParaToInt("id");
            String name = getPara("name");
            int department_id = getParaToInt("department_id");
            Role role = Role.roledao.findById(id);
            boolean result = role.set("id", id).set("name", name).set("department_id", department_id).update();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findById() {
        try {
            int id = getParaToInt("id");
            Role role = Role.roledao.findById(id);
            if (role != null) {
                renderJson(toJsonSingle(role));
            } else {
                renderJson(RenderUtils.CODE_REPEAT);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 全删除
     */
    public void deleteAll() {
        try {
            Integer[] selected = getParaValuesToInt("selected[]");
            Boolean result = true;
            for (int i = 0; i < selected.length; i++) {
                int id = selected[i];
                result = result && Role.roledao.deleteById(id);
                if (!result) break;
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }
    public  void findByDepartment(){
        try{
            int department_id=getParaToInt("department_id");
            if(department_id!=0)
            {
                List<Role>  roleList=Role.roledao.find("select * from `db_role` where department_id =" + department_id);
                renderJson(toJson(roleList));
            }
            else
            {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        }
        catch (Exception e)
        {
            renderError(500);
        }
    }
}