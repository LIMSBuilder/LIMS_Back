package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by chenyangyang on 2017/4/18.
 */
public class DeliveryController extends Controller {
    public void delivery() {
        try {
            String date = ParaUtils.sdf2.format(new Date());

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void finishItem() {
        try {
            int company_id = getParaToInt("id");
            Company company = Company.companydao.findById(company_id);
            if (company != null) {
                int sampleSize = Sample.sampleDao.find("SELECT s.* FROM `db_company` c ,`db_sample` s WHERE c.id=" + company.get("id") + " AND c.id=s.company_id AND s.process is NULL").size();
                if (sampleSize != 0) {
                    //还有未提交的样品信息
                    renderJson(RenderUtils.CODE_UNIQUE);
                    return;
                }
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_company` c,`db_item` i,`db_item_project` p \n" +
                        "WHERE c.id=" + company.get("id") + " AND c.id=i.company_id AND i.id=p.item_id");
                Boolean result = true;
                for (ItemProject itemProject : itemProjectList) {
                    int count = Db.find("SELECT p.* FROM `db_company` c,`db_sample` s ,`db_sample_project` p\n" +
                            "WHERE c.id=" + company.get("id") + " AND c.id =s.company_id AND s.id=p.sample_id AND p.item_project_id=" + itemProject.get("id")).size();
                    result = result && (count == 0 ? false : true);
                    if (!result) break;
                }
                if (!result) {
                    renderJson(RenderUtils.CODE_EMPTY);
                    return;
                }
                result = result && company.set("process", 2).update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 提交当日所有作业
     */
    public void finishList() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    User user = ParaUtils.getCurrentUser(getRequest());
                    List<Delivery> deliveryList = Delivery.deliverydao.find("SELECT d.* FROM `db_delivery_user` u,`db_delivery` d \n" +
                            "WHERE u.user_id=" + user.get("id") + " AND u.delivery_id=d.id AND d.process=0");
                    Boolean result = true;
                    for (Delivery delivery : deliveryList) {
                        Company company = Company.companydao.findById(delivery.get("company_id"));
                        if (company.get("process") == 1) {
                            //1表示已经派遣了，但是没有完成样品登记，在提交的时候，需要把它还回科室主任处
                            result = result && company.set("process", 0).update();
                        }
                        result = result && delivery.set("process", 1).update();
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


}
