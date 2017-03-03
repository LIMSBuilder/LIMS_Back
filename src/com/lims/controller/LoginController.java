package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.List;
import java.util.Map;

public class LoginController extends Controller {
    /**
     * 登录验证
     */
    public void check() {
        try {
            String username = getPara("username");
            String password = getPara("password");
            List<User> userList = User.userDao.find("SELECT * FROM `db_user` WHERE nick='" + username + "'");
            if (userList.size() != 0) {
                if (userList.get(0).get("password").equals(ParaUtils.EncoderByMd5(password))) {
                    renderJson(RenderUtils.CODE_SUCCESS);
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
            User user = new User();
            user
                    .set("nick", getPara("nick"))
                    .set("name", getPara("name"))
                    .set("password", ParaUtils.EncoderByMd5(getPara("password")))
                    .set("cardId", getPara("cardId"));
            renderJson(user.save() ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }
}
