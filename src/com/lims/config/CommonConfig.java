package com.lims.config;

import com.jfinal.config.*;
import com.jfinal.ext.handler.UrlSkipHandler;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.render.ViewType;
import com.lims.interceptor.ExceptionIntoLogInterceptor;
import com.lims.interceptor.LoginInterceptor;
import com.lims.interceptor.PowerInterceptor;
import com.lims.model.*;
import com.lims.utils.MessageSender;
import com.lims.utils.WebSocketHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 主程序入口，Config配置类
 */
public class CommonConfig extends JFinalConfig {
    public static List<Object> userList = new ArrayList<Object>();

    @Override
    public void configConstant(Constants me) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        System.setProperty("log_date", sdf.format(new Date()));
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
        arp.addMapping("db_monitor_project", MonitorProject.class);
        arp.addMapping("db_contract", Contract.class);
        arp.addMapping("db_item", Contractitem.class);
        arp.addMapping("db_item_project", ItemProject.class);
        arp.addMapping("db_encode", Encode.class);
        arp.addMapping("db_notice", Notice.class);
        arp.addMapping("db_calendar", Calendar.class);
        arp.addMapping("db_mail", Mail.class);
        arp.addMapping("db_mail_file", MailFile.class);
        arp.addMapping("db_receiver", Receiver.class);
        arp.addMapping("db_default", Default.class);
        arp.addMapping("db_contract_review", ContractReview.class);
        arp.addMapping("db_task", Task.class);

        arp.addMapping("db_sample", Sample.class);
        arp.addMapping("db_sample_project", SampleProject.class);

        arp.addMapping("db_log", Log.class);
        arp.addMapping("db_delivery", Dispatch.class);

        arp.addMapping("db_delivery_user", DispatchUser.class);
        arp.addMapping("db_company", Company.class);
        arp.addMapping("db_delivery", Delivery.class);
        arp.addMapping("db_delivery_user", DeliveryUser.class);
        arp.addMapping("db_sample_record", FileRecord.class);
        arp.addMapping("db_power", Power.class);
        arp.addMapping("db_power_role", PowerUser.class);
        arp.addMapping("db_equipment",Equipment.class);
        //addMap增加数据库树形

    }

    @Override
    public void configInterceptor(Interceptors me) {
//        me.add(new AdminIntercept());
        me.add(new LoginInterceptor());
//        me.add(new PowerInterceptor());
        me.addGlobalActionInterceptor(new ExceptionIntoLogInterceptor());
    }

    @Override
    public void configHandler(Handlers me) {
        //me.add(new MessageSender());
        me.add(new UrlSkipHandler("^/websocket.ws", true));
        me.add(new WebSocketHandler("^/websocket.ws"));

    }
}
