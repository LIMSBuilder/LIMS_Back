package com.lims.model;

import com.jfinal.plugin.activerecord.Model;
import com.lims.utils.ParaUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qulongjun on 2017/3/4.
 */
public class Mail extends Model<Mail> {
    public static Mail mailDao = new Mail();

    public Map getMailInfo() {
        Map temp = new HashMap();
        temp.put("id", get("id"));
        temp.put("title", get("title"));
        temp.put("content", get("content"));
        temp.put("create_time", get("create_time"));
        temp.put("sender", User.userDao.findById(get("send_id")).toSimpleJson());
        temp.put("create_desp", ParaUtils.getPrettyTime(get("create_time").toString()));
        List<MailFile> mailFileList = MailFile.mailFileDao.find("SELECT * FROM `db_mail_file` WHERE mail_id=" + get("id"));
        temp.put("path", mailFileList);
        return temp;
    }

    public Map getSimpleMapInfo() {
        Map temp = new HashMap();
        temp.put("id", get("id"));
        temp.put("title", get("title"));
        temp.put("sender", get("send_id") == null ? null : User.userDao.findById(get("send_id")).toSimpleJson());
        temp.put("create_desp", ParaUtils.getPrettyTime(get("create_time").toString()));
        return temp;
    }
}
