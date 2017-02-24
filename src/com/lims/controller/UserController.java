package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Role;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/24.
 */
public class UserController extends Controller {

    public void create() {
        try {
            String cardId = getPara("cardId");
            if (User.userDao.find("SELECT * FROM `db_user` WHERE cardId='" + cardId + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                Map result = getParaMap();
                User user = new User();
                for (Object key : result.keySet()) {
                    String value = ((String[]) result.get(key))[0];
                    if (key.equals("departmentId")) continue;
                    user.set(key.toString(), value);
                }
                Prop p = PropKit.use("default.properties");
                if (user.get("password") == null)
                    user.set("password", ParaUtils.EncoderByMd5(p.get("init_password")));
                renderJson(user.save() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            }
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
            String param = " where u.roleId = r.id ";
            Object[] keys = condition.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                Object value = condition.get(key);
                if (key.equals("keyword")) {
                    //查找关键字
                    param += (" AND ( u.nick like \"%" + value + "%\" OR u.name like \"%" + value + "%\" OR u.cardId like \"%" + value + "%\" ) ");
                    continue;
                }
                if (key.equals("departmentId")) {
                    //根据Department查找
                    param += (" AND r.department_id=" + value);
                    continue;
                }
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<User> userPage = User.userDao.paginate(currentPage, rowCount, "SELECT u.*", "FROM `db_user` u,`db_role` r " + param);
            List<User> userList = userPage.getList();
            Map results = toJson(userList);
            results.put("currentPage", currentPage);
            results.put("rowCount", rowCount);
            results.put("totalPage", userPage.getTotalPage());
            results.put("concdition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<User> userList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (User user : userList) {
                Map temp = toJsonSingle(user);
                results.add(temp);
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(User user) {
        Map<String, Object> map = new HashMap<>();
        for (String key : user._getAttrNames()) {
            if (key.equals("roleId")) {
                Role temp = Role.roledao.findById(user.getInt(key));
                map.put("role", temp);
                map.put("roleId", user.getInt(key));
                map.put("department", temp.getDepartment());
                map.put("departmentId", temp.getDepartment().getInt("id"));
                continue;
            }
            map.put(key, user.get(key));
        }
        return map;
    }

    public void findById() {
        try {
            int id = getParaToInt("id");
            User user = User.userDao.findById(id);
            if (user != null) {
                renderJson(toJsonSingle(user));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }
}
