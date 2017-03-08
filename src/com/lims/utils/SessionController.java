package com.lims.utils;

import com.lims.config.CommonConfig;
import com.lims.model.User;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by qulongjun on 2017/3/6.
 */
public class SessionController implements HttpSessionListener, HttpSessionAttributeListener {

    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
        String name = httpSessionBindingEvent.getName();
        if (name.equals("user")) {
            User user = User.userDao.findById(httpSessionBindingEvent.getValue());
            //这里增加部门信息管理
            if (CommonConfig.userList.indexOf(user.toSimpleJson()) == -1) {
                CommonConfig.userList.add(user.toSimpleJson());
            }
        }

    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        String name = httpSessionBindingEvent.getName();
        if (name.equals("user")) {
            CommonConfig.userList.remove(User.userDao.findById(httpSessionBindingEvent.getValue()).toSimpleJson());
        }
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
        String name = httpSessionBindingEvent.getName();
        if (name.equals("user")) {
            User user = User.userDao.findById(httpSessionBindingEvent.getValue());
            if (CommonConfig.userList.indexOf(user.toSimpleJson()) == -1) {
                CommonConfig.userList.add(user.toSimpleJson());
            }
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        System.out.println("创建session");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        System.out.println("销毁session");
    }
}
