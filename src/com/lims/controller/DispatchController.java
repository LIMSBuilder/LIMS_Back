package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenyangyang on 2017/4/15.
 */
public class DispatchController extends Controller {

    /**
     * 任务派遣创建接口
     */
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    Integer[] projectList = getParaValuesToInt("company[]");
                    for (int id : projectList) {
                        Dispatch dispatch = new Dispatch();
                        result = result && dispatch.set("company_id", id).set("creater", ParaUtils.getCurrentUser(getRequest()).get("id")).set("create_tine", ParaUtils.sdf.format(new Date())).set("date", ParaUtils.sdf2.format(new Date())).set("process", 0).save();
                        Company company = Company.companydao.findById(id);
                        if (company != null) {
                            result = result && company.set("process", 1).update();
                        } else return false;

                        if (!result) return false;
                        int charge_id = getParaToInt("charge_id");
                        DispatchUser dispatchUser = new DispatchUser();
                        result = result && dispatchUser.set("delivery_id", dispatch.get("id")).set("user_id", charge_id).set("type", 1).save();
                        if (!result) return false;
                        Integer[] joiner = getParaValuesToInt("join_id[]");
                        for (int joinId : joiner) {
                            DispatchUser dispatchJoiner = new DispatchUser();
                            result = result && dispatchJoiner.set("delivery_id", dispatch.get("id")).set("user_id", joinId).set("type", 0).save();
                            if (!result) break;
                        }
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
     * 获取个人派遣任务列表
     */
    public void UserDispatchList() {
        try {
            int rowCount = getParaToInt("rowCount");
            int currentPage = getParaToInt("currentPage");
            String condition_temp = getPara("condition");
            Map condition = ParaUtils.getSplitCondition(condition_temp);
            User user = ParaUtils.getCurrentUser(getRequest());
            Page<Company> companyPage = Company.companydao.paginate(currentPage, rowCount, "SELECT c.*", " FROM `db_company` c, `db_delivery` d,`db_delivery_user` u WHERE c.id=d.company_id AND d.id=u.delivery_id AND u.user_id=" + user.get("id"));
            List<Company> companyList = companyPage.getList();
            Map results = toJson(companyList);
            results.put("currentPage", currentPage);
            results.put("totalPage", companyPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<Company> companyList) {
        Map result = new HashMap();
        List temp = new ArrayList();
        for (Company company : companyList) {
            temp.add(company.toSimpleJSON());
        }
        result.put("results", temp);
        return result;
    }

}
