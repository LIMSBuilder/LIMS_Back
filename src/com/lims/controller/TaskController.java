package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by qulongjun on 2017/3/10.
 */
public class TaskController extends Controller {

    /**
     * 自定义任务书创建
     */
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Map paraMaps = getParaMap();
                    Task task = new Task();
                    Boolean result = true;
                    for (Object key : paraMaps.keySet()) {
                        switch (key.toString()) {
                            case "id":
                                break;//不知道为什么会传一个id过来，待观察
                            default:
                                if (key.toString().indexOf("item") != -1) {
                                    continue;
                                }
                                task.set(key.toString(), ((String[]) paraMaps.get(key))[0]);
                        }

                    }
                    User user = ParaUtils.getCurrentUser(getRequest());
                    task.set("identify", createIdentify()).set("create_time", ParaUtils.sdf.format(new Date())).set("creater", user.get("id")).set("process", ProcessKit.getTaskProcess("create"));
                    result = result && task.save();
                    String[] items = getParaValues("project_items[]");
                    for (String item : items) {
                        Map temp = Jackson.getJson().parse(item, Map.class);
                        Contractitem contractitem = new Contractitem();
                        List points = (ArrayList) temp.get("point");
                        String point = "";
                        if (points != null) {
                            for (int i = 0; i < points.size(); i++) {
                                point += points.get(i);
                                if (i != points.size() - 1) {
                                    point += ",";
                                }
                            }
                        }

                        result = result && contractitem.set("element", ((Map) temp.get("element")).get("id")).set("company", temp.get("company")).set("point", point).set("task_id", task.get("id")).set("other", temp.get("other")).set("is_package", temp.get("is_package")).save();
                        if (!result) break;
                        List<Map> projectList = (ArrayList) temp.get("project");
                        if (projectList != null) {
                            for (int m = 0; m < projectList.size(); m++) {
                                Map project = projectList.get(m);
                                ItemProject entry = new ItemProject();
                                entry.set("item_id", contractitem.get("id")).set("project_id", project.get("id"));
                                result = result && entry.save();
                                if (!result) break;
                            }
                        }
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

    /**
     * 根据合同创建
     */
    public void createByContract() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Task task = new Task();
                    Contract contract = Contract.contractDao.findById(getPara("contract_id"));
                    if (contract != null) {
                        Boolean result = task.set("contract_id", getPara("contract_id")).set("process", ProcessKit.getTaskProcess("create")).set("create_time", ParaUtils.sdf.format(new Date())).set("creater", ParaUtils.getCurrentUser(getRequest()).get("id")).save();
                        result = result && contract.set("process", ProcessKit.getContractProcess("finish")).update();
                        return result;
                    } else
                        return false;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public String createIdentify() {
        String identify = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        identify = sdf.format(new Date());
        Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
        if (encode == null) {
            //数据库中没有第一条记录，则创建它
            Encode entry = new Encode();
            entry.set("contract_identify", 0).save();
            identify += String.format("%04d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify += String.format("%04d", identify_Encode);
        }
        return identify;
    }
}
