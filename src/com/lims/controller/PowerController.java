package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.RenderUtils;
import com.sun.xml.internal.bind.v2.model.core.ID;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/4/22.
 */
public class PowerController extends Controller {
    /**
     * 权限保存接口
     **/
    public void powerCreate() {
        try {

            String name = getPara("name");
            String path = getPara("path");
            int parent = getParaToInt("parent");
            int type = getParaToInt("type");//0-前端控制，1-后台控制
            Boolean result = true;
            Power power = new Power();
            if (parent != 0) {
                power.set("parent", parent);
            }
            result = result && power.set("name", name).set("path", path).set("type", type).save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void list() {
        try {
            List<Power> powerList = Power.powerDao.find("SELECT * FROM `db_power` where  parent is null");
            List result = new ArrayList();
            for (Power power : powerList) {
                Map temp = new HashMap();
                List<Power> childList = Power.powerDao.find("SELECT * FROM `db_power` WHERE parent=" + power.get("id"));
                temp.put("id", power.get("id"));
                temp.put("name", power.get("name"));
                temp.put("type", power.get("type"));
                temp.put("child", childList);
                temp.put("path",power.get("path"));
                result.add(temp);

            }
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void getIetems() {
        try {
            List<Power> powerList = Power.powerDao.find("SELECT * FROM `db_power`");
            List result = new ArrayList();
            for (Power power : powerList) {
                Map temp = new HashMap();

                temp.put("id", power.get("id"));
                temp.put("name", power.get("name"));
                temp.put("type", power.get("type"));
                temp.put("parent", power.get("parent"));
                temp.put("path",power.get("path"));
                result.add(temp);
            }
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findByRoleId() {
        try {
            int role_id = getParaToInt("role_id");
            Map result = new HashMap();
            result.put("results", getTotalPower());
            List<PowerUser> powerUserList = PowerUser.powerUserDao.find("SELECT * FROM `db_power_user` WHERE role_id=" + role_id);
            List<Integer> ids = new ArrayList<>();
            for (PowerUser powerUser : powerUserList) {
                ids.add(powerUser.getInt("power_id"));
            }
            result.put("active", ids);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public List getTotalPower() {
        List<Power> powerList = Power.powerDao.find("SELECT * FROM `db_power` WHERE parent is NULL");
        List temp = new ArrayList();
        for (Power power : powerList) {
            Map result = toJson(power);
            result.put("child", getChild(power));
            temp.add(result);
        }
        return temp;
    }

    public List getChild(Power power) {
        List<Power> powerList = Power.powerDao.find("SELECT * FROM `db_power` WHERE parent=" + power.get("id"));
        if (powerList.size() == 0) {
            return powerList;
        } else {
            List result = new ArrayList();
            for (Power power1 : powerList) {
                Map temp = toJson(power1);
                temp.put("child", getChild(power1));
                result.add(temp);
            }
            return result;
        }
    }

    public Map toJson(Power power) {
        Map result = new HashMap();
        for (String key : power._getAttrNames()) {
            result.put(key, power.get(key));
        }
        return  result;
    }
}