package com.lims.utils;

import com.jfinal.handler.Handler;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.lims.model.Notice;
import com.lims.model.User;
import com.sun.deploy.util.StringUtils;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息发送工具类
 */
public class MessageSender extends Handler{

    /**
     * 新增消息方法
     *
     * @param messageTemplateName messageTemplate.properties中的key
     * @param tokens              键值对，需要填充的Token
     * @param toUser              发送的对象
     * @param endDate             结束时间，null表示一直提醒
     * @return
     */
    public static boolean addMessage(String messageTemplateName, Map<String, String> tokens, User toUser, Date endDate) {
        try {
            if (toUser == null) return false;
            if (tokens == null) tokens = new HashMap<>();
            Prop p = PropKit.use("messageTemplate.properties");
            String template = p.get(messageTemplateName);
            if (template != null) {
                String patternString = "\\$\\{(" + StringUtils.join(tokens.keySet(), "|") + ")\\}";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(template);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
                }
                matcher.appendTail(sb);
                Notice notice = new Notice();
                notice
                        .set("messageTemplateName", sb.toString())
                        .set("to_id", toUser.get("id"))
                        .set("path", p.get(messageTemplateName + "_path"))
                        .set("state", 0)
                        .set("create_time", ParaUtils.sdf.format(new Date()));
                if (endDate != null) notice.set("end_time", ParaUtils.sdf.format(endDate));
                return notice.save();
            } else return false;
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return false;
        }
    }

    @Override
    public void handle(String s, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, boolean[] booleans) {
        System.out.println("执行了Handle");
    }
}
