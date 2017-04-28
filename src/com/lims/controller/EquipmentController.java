package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Equipment;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;

/**
 * Created by chenyangyang on 2017/4/24.
 */
public class EquipmentController extends Controller {

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
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Equipment> equipmentPage = Equipment.equipmentDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_equipment`" + param);
            List<Equipment> equipmentList = equipmentPage.getList();
            Map results = toJson(equipmentList);
            results.put("currentPage", currentPage);
            results.put("totalPage", equipmentPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    /**
     * 将多Equipment序列化为对象集合
     *
     * @param entityList
     * @return
     */
    public Map toJson(List<Equipment> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Equipment equipment : entityList) {
                result.add(toJsonSingle(equipment));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    /**
     * 将单个Equipment序列化为Map对象
     *
     * @param equipment
     * @return
     */
    public Map toJsonSingle(Equipment equipment) {
        Map<String, Object> equip = new HashMap<>();
        equip.put("id", equipment.getInt("id"));
        equip.put("GIdentify", equipment.get("GIdentify"));
        equip.put("name", equipment.get("name"));
        equip.put("type", equipment.get("type"));
        equip.put("Fidentify", equipment.get("Fidentify"));
        equip.put("factory", equipment.get("factory"));
        equip.put("price", equipment.get("price"));
        equip.put("method", equipment.get("method"));
        equip.put("place", equipment.get("place"));
        equip.put("people", equipment.get("people"));
        equip.put("finalTime", equipment.get("finalTime"));
        equip.put("time", equipment.get("time"));
        equip.put("certificate", equipment.get("certificate"));
        return equip;
    }


    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Equipment equipment = new Equipment();
                    Boolean result = true;
                    equipment.set("GIdentify", getPara("GIdentify"))
                            .set("name", getPara("name")).set("type", getPara("type"))
                            .set("Fidentify", getPara("Fidentify")).set("factory", getPara("factory"))
                            .set("price", getPara("price")).set("method", getPara("method"))
                            .set("place", getPara("place")).set("people", getPara("people"))
                            .set("finalTime", ParaUtils.sdf2.format(new Date())).set("time", ParaUtils.sdf2.format(new Date()))
                            .set("certificate", getPara("certificate"));
                    result = result && equipment.save();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void change() {
        try {
            int id = getParaToInt("id");
            Equipment equipment = Equipment.equipmentDao.findById(id);
            boolean result = true;
            if (equipment != null) {
                equipment.set("id", id).set("GIdentify", getPara("GIdentify"))
                        .set("name", getPara("name")).set("type", getPara("type"))
                        .set("Fidentify", getPara("Fidentify")).set("factory", getPara("factory"))
                        .set("price", getPara("price")).set("method", getPara("method"))
                        .set("place", getPara("place")).set("people", getPara("people"))
                        .set("finalTime", getPara("finalTime")).set("time", getPara("time"))
                        .set("certificate", getPara("certificate"));
                result = result && equipment.update();
                renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Equipment.equipmentDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void deleteAll() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Integer[] selected = getParaValuesToInt("selected[]");
                    Boolean result = true;
                    for (int i = 0; i < selected.length; i++) {
                        int id = selected[i];
                        result = result && Equipment.equipmentDao.deleteById(id);
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
             Equipment equipment =Equipment.equipmentDao.findById(id);
             if(equipment!=null){
                 renderJson(toJsonSingle(equipment));
             }
             else {
                 renderJson(RenderUtils.CODE_EMPTY);
             }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public  void total(){
      try {
          List<Equipment> equipmentList =Equipment.equipmentDao.find("select * from `db_equipment`");
          renderJson(toJson(equipmentList));
      }catch (Exception e){
          renderError(500);
      }
    }

}
