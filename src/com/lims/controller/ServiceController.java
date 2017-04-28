package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Encode;
import com.lims.model.ServiceContract;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by qulongjun on 2017/4/28.
 */
public class ServiceController extends Controller {
    public void list() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            if (rowCount == 0) {
                rowCount = ParaUtils.getRowCount();
            }
            String param = " WHERE 1=1 ";
            Object[] keys = condition.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                Object value = condition.get(key);
                if (key.equals("process")) { //process=wait_change
                    switch (value.toString()) {
                        default:
                            param += " AND " + key + " = " + value;
                    }
                    continue;
                }

                if (key.equals("keyWords")) {
                    param += (" AND ( identify ='" + value + "' OR name like \"%" + value + "%\" )");
                    continue;
                }
                if (key.equals("review_me")) {
                    User user = ParaUtils.getCurrentUser(getRequest());
                    param += " AND reviewer =" + user.get("id");
                    continue;
                }
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<ServiceContract> contractPage = ServiceContract.serviceContractDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_service_contract`" + param + " ORDER BY create_time DESC");
            List<ServiceContract> contractList = contractPage.getList();
            Map results = toJson(contractList);
            results.put("currentPage", currentPage);
            results.put("totalPage", contractPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    public Map toJson(List<ServiceContract> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (ServiceContract contract : entityList) {
                result.add(toJsonSingle(contract));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(ServiceContract entry) {
        Map temp = new HashMap();
        temp.put("id", entry.get("id"));
        temp.put("path", entry.get("path"));
        temp.put("name", entry.get("name"));
        temp.put("review", entry.get("review"));
        temp.put("state", entry.get("state"));
        temp.put("creater", User.userDao.findById(entry.get("creater")).toSimpleJson());
        temp.put("create_time", entry.get("create_time"));
        temp.put("identify", entry.get("identify"));
        return temp;
    }


    /**
     * 上传服务合同
     */
    public void createService() {
        try {
            int review = getParaToInt("review");//是否技术评审
            String path = getPara("path");//服务合同路径
            String name = getPara("name");//合同名称
            ServiceContract serviceContract = new ServiceContract();
            serviceContract
                    .set("path", path)
                    .set("name", name)
                    .set("review", review)
                    .set("state", 0)
                    .set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"))
                    .set("create_time", ParaUtils.sdf.format(new Date()))
                    .set("identify", createIdentify());
            Boolean result = serviceContract.save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 合同编号生成
     * <p>
     * 年份+ - + 4位流水编号，如 2017-001  2017-002  以此类推
     * <p>
     * 需要考虑：年份更新需要自动更新当前年份，且将流水号恢复初始值1号
     **/
    public String createIdentify() {
        String identify = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        identify = sdf.format(new Date());
        Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
        if (encode == null) {
//            数据库中没有第一条记录，则创建它
            Encode entry = new Encode();
            entry.set("contract_identify", 1).set("self_identify", 0).set("scene_identify", 0).save();
            identify = identify + "-" + String.format("%03d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify = identify + "-" + String.format("%03d", identify_Encode);
        }
        return identify;
    }
}
