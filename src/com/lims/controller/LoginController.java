package com.lims.controller;

import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.lims.config.CommonConfig;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.servlet.http.HttpSession;
import java.util.*;

@Clear
public class LoginController extends Controller {
    /**
     * 登录验证
     */
    public void check() {
        try {
            LogKit.info("测试log功能看看实现");
            String username = getPara("username");
            String password = getPara("password");
            List<User> userList = User.userDao.find("SELECT * FROM `db_user` WHERE nick='" + username + "'");
            if (userList.size() != 0) {
                if (userList.get(0).get("password").equals(ParaUtils.EncoderByMd5(password))) {
                    getSession().setAttribute("user", userList.get(0).get("id"));

                    Boolean result = userList.get(0).set("lastLogin", ParaUtils.sdf.format(new Date())).update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                } else {
                    renderJson(RenderUtils.CODE_ERROR);
                }
            } else renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void forget() {
        try {
            String username = getPara("username");
            String password = getPara("password");
            String card = getPara("card");
            List<User> userList = User.userDao.find("SELECT * FROM `db_user` WHERE nick='" + username + "'");
            if (userList.size() != 0) {
                if (userList.get(0).get("cardId").equals(card)) {
                    Boolean result = userList.get(0).set("password", ParaUtils.EncoderByMd5(password)).update();
                    renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
                } else {
                    renderJson(RenderUtils.CODE_ERROR);
                }
            } else renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 用户注册
     */
    public void register() {
        try {
            String cardId = getPara("cardId");
            String nick = getPara("nick");
            if (User.userDao.find("SELECT * FROM `db_user` WHERE cardId='" + cardId + "' OR nick='" + nick + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                User user = new User();
                user
                        .set("nick", getPara("nick"))
                        .set("name", getPara("name"))
                        .set("password", ParaUtils.EncoderByMd5(getPara("password")))
                        .set("cardId", getPara("cardId"))
                        .set("isInit", 0);
                renderJson(user.save() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            }
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 获取登录用户的信息
     */
    public void getLogin() {
        try {

            renderJson(ParaUtils.getCurrentUserMap(getRequest()));
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 退出登录
     */
    public void exitLogin() {
        try {
            User user = ParaUtils.getCurrentUser(getRequest());
            getSession().removeAttribute("user");
            renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 获取所有登录信息
     */
    public void getLoginList() {
        List<Object> userList = CommonConfig.userList;
        renderJson(userList);
    }


    /**
     * 账号锁定之后恢复验证
     */
    public void checkPwd() {
        try {
            User user = ParaUtils.getCurrentUser(getRequest());
            if (user != null) {
                if (user.get("password").equals(ParaUtils.EncoderByMd5(getPara("password")))) {
                    renderJson(RenderUtils.CODE_SUCCESS);
                } else renderJson(RenderUtils.CODE_ERROR);
            } else renderJson(RenderUtils.CODE_EMPTY);
        } catch (Exception e) {
            renderError(500);
        }
    }
}
