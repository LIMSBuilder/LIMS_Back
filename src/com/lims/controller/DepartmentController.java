package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Department;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by caiwenhong on 2017/2/23.
 */
public class DepartmentController extends Controller {

    /**
     * 显示部门列表
     * Input 每页显示行数:rowCount，当前页码:currentPage，查询条件:condition
     */
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
            Page<Department> departmentPage = Department.departmentdao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_department`" + param);
            List<Department> departmentList = departmentPage.getList();
            Map results = toJson(departmentList);
            results.put("currentPage", currentPage);
            results.put("totalPage", departmentPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    /**
     * 将多个Department序列化为对象集合
     *
     * @param entityList
     * @return
     */
    public Map toJson(List<Department> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Department department : entityList) {
                result.add(toJsonSingle(department));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    /**
     * 将单个Department序列化为Map对象
     *
     * @param department
     * @return
     */
    public Map toJsonSingle(Department department) {
        Map<String, Object> depart = new HashMap<>();
        depart.put("id", department.getInt("id"));
        depart.put("name", department.get("name"));
        return depart;
    }

    /**
     * 创建部门信息
     * Input 部门名称:name
     */
    public void create() {
        try {

            String name = getPara("name");
            if (Department.departmentdao.find("SELECT * FROM `db_department` WHERE name='" + name + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                Department department = new Department();
                boolean result = department.set("name", name).save();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            }
        } catch (Exception e) {
            renderError(500);
        }

    }


    /**
     * 改变部门信息
     */
    public void change() {
        try {
            int id = getParaToInt("id");
            String name = getPara("name");
            Department department = Department.departmentdao.findById(id);
            boolean result = department.set("name", name).update();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 找到部门信息
     */
    public void findById() {
        try {
            int id = getParaToInt("id");
            Department department = Department.departmentdao.findById(id);
            if (department != null) {
                renderJson(toJsonSingle(department));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 显示记录总条数
     */
    public void total() {
        try {

            List<Department> departmentList = Department.departmentdao.find("SELECT * FROM `db_department`");
            renderJson(toJson(departmentList));
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 删除部门信息
     **/
    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Department.departmentdao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }

    }

    /**
     * 批量删除
     */
    public void deleteAll() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Integer[] selected = getParaValuesToInt("selected[]");
                    Boolean result = true;
                    for (int i = 0; i < selected.length; i++) {
                        int id = selected[i];
                        result = result && Department.departmentdao.deleteById(id);
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
}

