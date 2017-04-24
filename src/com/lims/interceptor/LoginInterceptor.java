package com.lims.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.lims.model.PowerUser;
import com.lims.model.User;
import com.lims.utils.ParaUtils;

import java.util.List;

/**
 * 登录验证拦截器
 */
public class LoginInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation invocation) {
        System.out.println(invocation.getController());
        User user = ParaUtils.getCurrentUser(invocation.getController().getRequest());
        if (user == null) {
            //用户未登录
            invocation.getController().renderError(403);
        } else {
//            List<Record> powerList = Db.find("SELECT p.path FROM `db_power` p,`db_power_role` r,`db_user` WHERE u.user_id=" + user.get("id") + " AND r.id =u.role_id  AND r.power_id=p.id");
//            invocation.getController().getSession().setAttribute("powerList", powerList);
            invocation.invoke();
        }

    }
}
