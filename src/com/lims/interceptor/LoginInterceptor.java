package com.lims.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.lims.model.User;
import com.lims.utils.ParaUtils;

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
        } else
            invocation.invoke();
    }
}
