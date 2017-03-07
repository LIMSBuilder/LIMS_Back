package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Department;
import com.lims.model.Role;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
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
            String nick = getPara("nick");
            if (User.userDao.find("SELECT * FROM `db_user` WHERE cardId='" + cardId + "' OR nick='" + nick + "'").size() != 0) {
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

    public void delete() {
        try {
            int id = getParaToInt("id");
            boolean result = User.userDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void change() {
        try {
            int id = getParaToInt("id");
            String nick = getPara("nick");
            String password = getPara("password");
            String name = getPara("name");
            int roleId = getParaToInt("roleId");
            String cardId = getPara("cardId");
            User user = User.userDao.findById(id);
            boolean result = user.set("id", id).set("nick", nick).set("password", password).set("name", name).set("roleId", roleId).set("cardId", cardId).update();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void reset() {
        try {
            int id = getParaToInt("id");
            User user = User.userDao.findById(id);
            if (user != null) {
                Prop p = PropKit.use("default.properties");
                user.set("password", ParaUtils.EncoderByMd5(p.get("init_password")));
                renderJson(user.update() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else renderJson(RenderUtils.CODE_EMPTY);


        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 操作多条记录用Db.tx进行包裹，要么全成功，否则失败
     */
    public void resetAll() {
        try {
            Prop p = PropKit.use("default.properties");
            final String password = ParaUtils.EncoderByMd5(p.get("init_password"));
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Integer[] selected = getParaValuesToInt("selected[]");
                    Boolean result = true;
                    for (int i = 0; i < selected.length; i++) {
                        int id = selected[i];
                        User user = User.userDao.findById(id);
                        user.set("password", password);
                        result = result && user.update();
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

    public void total() {
        try {
            renderJson(toJson1(User.userDao.find("select * from `db_user`")));

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson1(List<User> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (User user : entityList) {
                results.add(toJsonSingle1(user));
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle1(User user) {
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", user.getInt("id"));
        user1.put("name", user.get("name"));
        return user1;
    }

    /**
     * 变更密码
     */
    public void changePwd() {
        try {
            User user = ParaUtils.getCurrentUser(getRequest());
            if (user != null) {
                String oldPassword = getPara("password");
                String newPassword = getPara("new_password");
                if (user.get("password").equals(ParaUtils.EncoderByMd5(oldPassword))) {
                    Boolean result = user.set("password", ParaUtils.EncoderByMd5(newPassword)).update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                } else renderJson(RenderUtils.CODE_REPEAT);
            } else renderJson(RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 变更头像
     */
    public void changePortait() {
        try {
            User user = ParaUtils.getCurrentUser(getRequest());
            if (user != null) {
                String path = getPara("path");
                Boolean result = user.set("portrait", path).update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else renderJson(RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void changeInfo() {
        try {
            User user = ParaUtils.getCurrentUser(getRequest());
            if (user != null) {
                user
                        .set("name", getPara("name"))
                        .set("tel", getPara("tel"))
                        .set("mail", getPara("mail"))
                        .set("address", getPara("address"))
                        .set("desp", getPara("desp"));
                renderJson(user.update() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else renderJson(RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void changeSetting() {
        try {
            User user = ParaUtils.getCurrentUser(getRequest());
            if (user != null) {
                user
                        .set("isNotice", getPara("isNotice"))
                        .set("showWelcome", getPara("showWelcome"))
                        .set("rowCount", getPara("rowCount"));
                renderJson(user.update() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else renderJson(RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void listByDepartment() {
        try {
            List<Department> departmentList = Department.departmentdao.find("SELECT * FROM `db_department`");
            List<Map> result = new ArrayList<>();
            for (Department department : departmentList) {
                List<User> userList = User.userDao.find("SELECT u.* FROM `db_user` u,`db_role` r WHERE u.roleId=r.id AND r.department_id=" + department.get("id"));
                if (userList.size() != 0) {
                    Map dep = new HashMap();
                    dep.put("id", department.get("id"));
                    dep.put("name", department.get("name"));

                    dep.put("user", toJson1(userList));
                    result.add(dep);
                } else continue;
            }
            Map temp = new HashMap();
            temp.put("results", result);
            renderJson(temp);
        } catch (Exception e) {
            renderError(500);
        }
    }

}
