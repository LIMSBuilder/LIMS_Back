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
        boolean result = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                String type = getPara("type");
                Integer[] inspect = getParaValuesToInt("inspect[]");
                Boolean result = true;
                switch (type) {
                    case "water":
                        for (int id : inspect) {
                            List<InspectWater> inspectWaterList = InspectWater.inspectWaterDao.find("SELECT * `db_inspect_water` WHERE inspect_id =" + id);
                            for (InspectWater water : inspectWaterList) {
                                result = result && water.set("result", getPara("result")).set("process", 1).update();
                            }
                        }
                        break;
                    case "soil":
                        for (int id : inspect) {
                            List<InspectSoil> inspectSoilList = InspectSoil.inspectSoilDao.find("SELECT * `db_inspect_soil` WHERE inspect_id =" + id);
                            for (InspectSoil soil : inspectSoilList) {
                                result = result && soil.set("result", getPara("result")).set("process", 1).update();
                            }
                        }
                        break;
                    case "solid":
                        for (int id : inspect) {
                           List<InspectSoild> inspectSoildList = InspectSoild.inspectSoildDao.find("SELECT * `db_inspect_solid` WHERE inspect_id =" + id);
                           for (InspectSoild soild:inspectSoildList){
                           result = result && soild.set("result", getPara("result")).set("process", 1).update();
                           }
                        }
                        break;
                    case "air":
                        for (int id : inspect) {
                            List<InspectAir> inspectAirList = InspectAir.inspectAir.find("SELECT * `db_inspect_air` WHERE inspect_id =" + id);
                            for (InspectAir air:inspectAirList){
                            result = result && air.set("result", getPara("result")).set("process", 1).update();}
                        }
                        break;
                    case "dysodia":
                        for (int id : inspect) {
                            List<InspectDysodia> inspectDysodiaList = InspectDysodia.inspectDysodiaDao.find("SELECT * `db_inspect_dysodia` WHERE inspect_id =" + id);
                            for (InspectDysodia dysodia:inspectDysodiaList) {
                                result = result && dysodia.set("result", getPara("result")).set("process", 1).update();
                            }
                        }
                        break;
                }

                return false;
            }
        });
        renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
    }


}
