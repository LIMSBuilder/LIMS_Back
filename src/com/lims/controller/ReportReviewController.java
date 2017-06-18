package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by chenyangyang on 2017/6/16.
 */
public class ReportReviewController extends Controller {
    /***
     * 报告一审
     * */
    public void firstReview() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Report report = Report.report.findById(getPara("report_id"));
                    if (report != null) {
                        RecordFirstReview recordFirstReview = new RecordFirstReview();
                        int condition1 = getParaToInt("condition1");
                        int condition2 = getParaToInt("condition2");
                        int condition3 = getParaToInt("condition3");
                        int condition4 = getParaToInt("condition4");
                        int condition5 = getParaToInt("condition5");
                        int condition6 = getParaToInt("condition6");
                        int condition7 = getParaToInt("condition7");
                        Boolean result = recordFirstReview.set("condition1", condition1).set("condition2", condition2)
                                .set("condition3", condition3).set("condition4", condition4)
                                .set("condition5", condition5).set("condition6", condition6)
                                .set("condition7", condition7)
                                .set("other", getPara("other"))
                                .set("report_id", getPara("report_id"))
                                .set("reviewer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("review_time", ParaUtils.sdf.format(new Date()))
                                .save();
                        if (!result) return false;
                        if ((condition1 == 1) && (condition2 == 1) && (condition3 == 1) && (condition4 == 1) && (condition5 == 1) && (condition6 == 1) && (condition7 == 1)) {
                            result = result && report.set("process", 3).set("firstReview", recordFirstReview.get("id")).update();
                        } else {
                            result = result && report.set("process", 1).set("firstReview", recordFirstReview.get("id")).update();
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
     * 报告二审
     **/
    public void secondReview() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Report report = Report.report.findById(getPara("report_id"));
                    if (report != null) {
                        ReportSecondReview reportSecondReview = new ReportSecondReview();
                        int condition1 = getParaToInt("condition1");
                        int condition2 = getParaToInt("condition2");
                        int condition3 = getParaToInt("condition3");
                        int condition4 = getParaToInt("condition4");
                        int condition5 = getParaToInt("condition5");
                        int condition6 = getParaToInt("condition6");
                        Boolean result = reportSecondReview.set("condition1", condition1).set("condition2", condition2)
                                .set("condition3", condition3).set("condition4", condition4)
                                .set("condition5", condition5).set("condition6", condition6)
                                .set("other", getPara("other"))
                                .set("report_id", getPara("report_id"))
                                .set("reviewer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("review_time", ParaUtils.sdf.format(new Date()))
                                .save();
                        if (!result) return false;
                        if ((condition1 == 1) && (condition2 == 1) && (condition3 == 1) && (condition4 == 1) && (condition5 == 1) && (condition6 == 1)) {
                            result = result && report.set("process", 4).set("secondReview", reportSecondReview.get("id")).update();
                        } else {
                            result = result && report.set("process", 1).set("secondReview", reportSecondReview.get("id")).update();
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
     * 报告三审
     **/
    public void thirdReview() {
        try {
            boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Report report = Report.report.findById(getPara("report_id"));
                    if (report != null) {
                        ReportThirdReview reportThirdReview = new ReportThirdReview();
                        int condition1 = getParaToInt("condition1");
                        int condition2 = getParaToInt("condition2");
                        int condition3 = getParaToInt("condition3");
                        Boolean result = reportThirdReview.set("condition1", condition1).set("condition2", condition2)
                                .set("condition3", condition3).set("other", getPara("other"))
                                .set("report_id", getPara("report_id"))
                                .set("reviewer", ParaUtils.getCurrentUser(getRequest()).get("id"))
                                .set("review_time", ParaUtils.sdf.format(new Date()))
                                .save();
                        if (!result) return false;
                        if (!result) return false;
                        if ((condition1 == 1) && (condition2 == 1) && (condition3 == 1)) {
                            User user = ParaUtils.getCurrentUser(getRequest());
                            result = result && report.set("process", 5).set("thirdReview", reportThirdReview.get("id")).set("singer",user.get("id")).set("sign_time",ParaUtils.sdf.format(new Date())).update();
                        } else {
                            User user = ParaUtils.getCurrentUser(getRequest());
                            result = result && report.set("process", 1).set("thirdReview", reportThirdReview.get("id")).set("singer",user.get("id")).set("sign_time",ParaUtils.sdf.format(new Date())).update();
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
