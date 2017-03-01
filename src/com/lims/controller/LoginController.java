package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.List;

/**
 * Created by qulongjun on 2017/3/1.
 */
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
}
