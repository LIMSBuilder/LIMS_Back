package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.FirstReview;
import com.lims.model.Task;
import com.lims.model.User;
import com.lims.utils.LoggerKit;
import com.lims.utils.ParaUtils;
import com.lims.utils.ProcessKit;
import com.lims.utils.RenderUtils;
import org.apache.poi.ss.formula.functions.T;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by chenyangyang on 2017/5/22.
 * 评审记录
 */
public class ReviewController extends Controller {

    /**
     * 一审评审记录
     ***/
    public void firstReview() {

        try {
             Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    Task task = Task.taskDao.findById(id);
                    if (task != null) {
                        User user = ParaUtils.getCurrentUser(getRequest());
                        FirstReview firstReview = new FirstReview();
                        Boolean result = true;
                        firstReview.set("origin", getParaToInt("origin"))
                                .set("monitorData", getParaToInt("monitorData"))
                                .set("inspect", getParaToInt("inspect"))
                                .set("originAlter", getParaToInt("originAlter"))
                                .set("originReview", getParaToInt("originReview"))
                                .set("standard", getParaToInt("standard"))
                                .set("result", getParaToInt("result"))
                                .set("other",getPara("other"))
                                .set("reviewer", user.get("id"))
                                .set("review_time", ParaUtils.sdf.format(new Date()))
                                .set("task_id", id);

                        result = result && firstReview.save();
                        if (!result) return false;
                        if (getParaToInt("result") == 1) {
                            LoggerKit.addContractLog(task.getInt("id"), "一审审核通过", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                        } else {
                            LoggerKit.addContractLog(task.getInt("id"), "一审审核拒绝", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                        }
                        return result;

                    } else {
                        return false;
                    }

                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 二审评审记录
     **/
    public void secondReview() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    int id = getParaToInt("id");
                    Task task = Task.taskDao.findById(id);
                    if (task != null) {
                        User user = ParaUtils.getCurrentUser(getRequest());
                        FirstReview firstReview = new FirstReview();
                        Boolean result = true;
                        firstReview.set("qualitySample", getParaToInt("qualitySample"))
                                .set("monitor", getParaToInt("monitor"))
                                .set("method", getParaToInt("method"))
                                .set("receiveBack", getParaToInt("receiveBack"))
                                .set("balance", getParaToInt("balance"))
                                .set("standard", getParaToInt("standard"))
                                .set("black", getParaToInt("black"))
                                .set("other",getPara("other"))
                                .set("reviewer", user.get("id"))
                                .set("review_time", ParaUtils.sdf.format(new Date()))
                                .set("task_id", id);

                        result = result && firstReview.save();
                        if (!result) return false;
                        if (getParaToInt("result") == 1) {
                            LoggerKit.addTaskLog(task.getInt("id"), "二审审核通过", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                        } else {
                            LoggerKit.addTaskLog(task.getInt("id"), "二审审核拒绝", ParaUtils.getCurrentUser(getRequest()).getInt("id"));
                        }
                        return result;

                    } else {
                        return false;
                    }

                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);


        } catch (Exception e) {
            renderError(500);
        }
    }


}
