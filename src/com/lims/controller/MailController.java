package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Mail;
import com.lims.model.MailFile;
import com.lims.model.Receiver;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;
import org.junit.Test;
import org.ocpsoft.prettytime.PrettyTime;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by qulongjun on 2017/3/4.
 * state:0 - 未读邮件 1-已读邮件 2-星标邮件 3-回收站邮件
 * type:0 - 普通邮件   1-系统邮件
 */
public class MailController extends Controller {
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Mail mail = new Mail();
                    Boolean result = true;
                    mail
                            .set("title", getPara("title"))
                            .set("content", getPara("content"))
                            .set("create_time", ParaUtils.sdf.format(new Date()))
                            .set("send_id", ParaUtils.getCurrentUser(getRequest()).get("id"));
                    result = result && mail.save();
                    if (!result) return false;
                    Integer[] receiverList = getParaValuesToInt("receiver[]");
                    for (int id : receiverList) {
                        Receiver receiver = new Receiver();
                        receiver.set("receiver_id", id)
                                .set("state", 0)
                                .set("type", 0)
                                .set("mail_id", mail.get("id"));
                        result = result && receiver.save();
                        if (!result) return false;
                    }
                    String[] pathList = getParaValues("path[]");
                    for (String path : pathList) {
                        MailFile mailFile = new MailFile();
                        mailFile.set("mail_id", mail.get("id")).set("file_path", path);
                        result = result && mailFile.save();
                        if (!result) return false;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void list() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            if (rowCount == 0) {
                rowCount = ParaUtils.getRowCount();
            }
            User user = ParaUtils.getCurrentUser(getRequest());
            String param = " where r.mail_id=m.id AND r.receiver_id=" + user.get("id") + " ";
            Object[] keys = condition.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                Object value = condition.get(key);
                if (key.equals("title")) {
                    param += (" AND m." + key + " like \"%" + value + "%\"");
                    continue;
                }
                if (key.equals("type")) {
                    switch (value.toString()) {
                        case "inbox":
                            param += (" AND r.state !=3 ");
                            break;
                        case "star":
                            param += (" AND r.state =2 ");
                        case "trash":
                            param += (" AND r.state =3 ");
                    }
                    continue;
                }

                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Receiver> receiverPage = Receiver.receiverDao.paginate(currentPage, rowCount, "SELECT r.*", "FROM `db_receiver` r,`db_mail` m " + param);
            List<Receiver> receiverList = receiverPage.getList();
            Map results = toJson(receiverList);
            results.put("currentPage", currentPage);
            results.put("rowCount", rowCount);
            results.put("totalPage", receiverPage.getTotalPage());
            results.put("concdition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }

    }

    public Map toJson(List<Receiver> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List results = new ArrayList();
            for (Receiver receiver : entityList) {
                Map temp = toJsonSingle(receiver);
                results.add(temp);
            }
            json.put("results", results);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Receiver receiver) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", receiver.get("id"));
        map.put("state", receiver.get("state"));
        map.put("read_time", receiver.get("read_time"));
        map.put("type", receiver.get("type"));
        map.put("mail", Mail.mailDao.findById(receiver.get("mail_id")).getMailInfo());
        return map;
    }


    public void changeState() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Integer[] selected = getParaValuesToInt("selected[]");
                    Boolean result = true;
                    for (int id : selected) {
                        Receiver receiver = Receiver.receiverDao.findById(id);
                        if (receiver != null) {
                            result = result && receiver.set("state", getPara("state")).update();
                        } else return false;
                        if (!result) break;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delete() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Integer[] selected = getParaValuesToInt("selected[]");
                    Boolean result = true;
                    for (int id : selected) {
                        result = result && Receiver.receiverDao.deleteById(id);
                        if (!result) break;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void findById() {
        try {
            int id = getParaToInt("id");
            Receiver receiver = Receiver.receiverDao.findById(id);
            User user = ParaUtils.getCurrentUser(getRequest());
            if (receiver != null && receiver.get("receiver_id") == user.get("id")) {
                Boolean result = true;
                if (receiver.get("state") == 0) {
                    result = receiver.set("read_time", ParaUtils.sdf.format(new Date())).set("state", 1).update();
                }
                Map temp = toJsonSingle(receiver);
                temp.put("code", result ? 200 : 502);
                renderJson(temp);
            } else renderJson(RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

}
