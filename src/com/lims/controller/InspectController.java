package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.*;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by chenyangyang on 2017/6/3.
 */
public class InspectController extends Controller {
    /**
     * 保存送检单数据
     **/
    public void save() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    String type = getPara("type");
                    Boolean result = true;
                    switch (type) {
                        case "water":
                            InspectWater inspectWater = InspectWater.inspectWaterDao.findById(getPara("id"));
                            if (inspectWater != null) {
                                result = result && inspectWater.set("result", getPara("result")).set("process", 1).update();
                            } else return false;
                            break;
                        case "soil":
                            InspectSoil inspectSoil = InspectSoil.inspectSoilDao.findById(getPara("id"));
                            if (inspectSoil != null) {
                                result = result && inspectSoil.set("result", getPara("result")).set("point", getPara("point")).set("remark", getPara("remark")).set("process", 1).update();
                            } else return false;
                            break;
                        case "solid":
                            InspectSoild inspectSoild = InspectSoild.inspectSoildDao.findById(getPara("id"));
                            if (inspectSoild != null) {
                                result = result && inspectSoild.set("result", getPara("result")).set("volume", getPara("volume")).set("flow", getPara("flow")).set("concentration", getPara("concentration")).set("discharge", getPara("discharge")).set("process", 1).update();
                            } else return false;
                            break;
                        case "air":
                            InspectAir inspectAir = InspectAir.inspectAir.findById(getPara("id"));
                            if (inspectAir != null) {
                                result = result && inspectAir.set("result", getPara("result")).set("volume", getPara("volume")).set("concentration", getPara("concentration")).set("process", 1).update();
                            } else return false;
                            break;
                        case "dysodia":
                            InspectDysodia inspectDysodia = InspectDysodia.inspectDysodiaDao.findById(getPara("id"));
                            if (inspectDysodia != null) {
                                result = result && inspectDysodia.set("result", getPara("result")).set("concentration", getPara("concentration")).set("process", 1).update();
                            } else return false;
                            break;
                    }
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void saveAttachment() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    int task_id = getParaToInt("task_id");
                    int project_id = getParaToInt("project_id");
                    String path = getPara("path");

                    String fileName = path.trim().substring(path.trim().lastIndexOf("\\") + 1);
                    InspectAttachment attachment = new InspectAttachment();
                    result = result && attachment.set("task_id", task_id).set("project_id", project_id).set("path", path).set("name", fileName).save();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void deleteAttachment() {
        try {
            int id = getParaToInt("id");
            Boolean result = InspectAttachment.inspectAttachmentDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


}
