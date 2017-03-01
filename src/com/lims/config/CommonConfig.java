package com.lims.config;

import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.render.ViewType;
import com.lims.model.*;

/**
 * 主程序入口，Config配置类
 */
public class CommonConfig extends JFinalConfig {

    @Override
    public void configConstant(Constants me) {
        //设置开发模式,如果设置为true,控制台会输出每次请求的Controller action和参数信息
        me.setDevMode(true);
        //设置视图模型
        me.setViewType(ViewType.JSP);
        //查找页面的访问路径
        me.setBaseViewPath("/WEB-INF");
        //设置url参数分隔线
        me.setUrlParaSeparator("-");
        me.setEncoding("utf-8");
        me.setBaseUploadPath("upload");

    }

    @Override
    public void configRoute(Routes me) {
        //前端页面路由
        me.add(new CommonRouter());
    }

    @Override
    public void configPlugin(Plugins me) {
        //C3P0连接池
        C3p0Plugin cp = new C3p0Plugin("jdbc:mysql://115.159.158.89:3306/bdc_lims", "root", "jun920221");
        me.add(cp);
        //数据库插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(cp);
        me.add(arp);
        arp.setShowSql(true);//这句话就是ShowSql
        arp.addMapping("db_department", Department.class);
        arp.addMapping("db_role", Role.class);
        arp.addMapping("db_user", User.class);
        arp.addMapping("db_element", Element.class);
        arp.addMapping("db_frequency", Frequency.class);
        arp.addMapping("db_type", Type.class);
        arp.addMapping("db_customer", Customer.class);
        arp.addMapping("db_monitor_project",MonitorProject.class);
        arp.addMapping("db_contract",Contract.class);
        arp.addMapping("db_contract_item",Contractitem.class);
        arp.addMapping("db_item_project",ItemProject.class);
        arp.addMapping("db_encode",Encode.class);
        //addMap增加数据库树形

    }

    @Override
    public void configInterceptor(Interceptors me) {
//        me.add(new AdminIntercept());
    }

    @Override
    public void configHandler(Handlers me) {

    }
}
