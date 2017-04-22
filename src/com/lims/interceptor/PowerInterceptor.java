package com.lims.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.lims.model.Power;
import com.lims.model.User;
import com.lims.utils.ParaUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限拦截器
 */
public class PowerInterceptor implements Interceptor {


    @Override
    public void intercept(Invocation invocation) {
        System.out.println(invocation.getController());
        User user = ParaUtils.getCurrentUser(invocation.getController().getRequest());
        if (user != null) {
            List<String> powerList = (ArrayList) invocation.getController().getSession().getAttribute("powerList");
            if (powerList.contains(invocation.getActionKey())) {
                //存在，统一访问
                invocation.invoke();
            } else {
                //不存储，拒绝访问
                invocation.getController().renderError(403);
            }
//            Power power = Power.powerDao.findFirst("SELECT * FROM `db_power_user` u WHERE user_id="+user.get("id")+" AND power_")
        }
    }
}
