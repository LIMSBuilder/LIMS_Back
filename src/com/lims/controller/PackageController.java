package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.lims.model.Contract;
import com.lims.model.ItemProject;
import com.lims.model.MonitorProject;
import com.lims.model.Package;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by qulongjun on 2017/4/28.
 */
public class PackageController extends Controller {
    /**
     * 创建分包信息
     */
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    Package p = new Package();
                    //Map paraMaps = getParaMap();
                    p
                            .set("contract_id", getPara("contract_id"))
                            .set("name", getPara("name"))
                            .set("payment", getPara("payment"))
                            .set("contact", getPara("contact"))
                            .set("remark", getPara("remark"));
                    p.set("create_time", ParaUtils.sdf.format(new Date())).set("creater", ParaUtils.getCurrentUser(getRequest()).get("id"));
                    result = result && p.save();
                    if (!result) return false;
                    Integer[] ids = getParaValuesToInt("ids[]");
                    for (int id : ids) {
                        ItemProject itemProject = ItemProject.itemprojectDao.findById(id);
                        result = result && itemProject.set("isPackage", 1).update();
                        if (!result) return false;
                    }
                    Contract contract = Contract.contractDao.findById(getPara("contract_id"));
                    result = result && contract.set("package_id", p.get("id")).update();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    /**
     * 修改分包信息
     */
    public void change() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    int id = getParaToInt("id");
                    Contract contract = Contract.contractDao.findById(id);
                    Package p = Package.packageDao.findById(contract.get("package_id"));
                    p
                            .set("name", getPara("name"))
                            .set("payment", getPara("payment"))
                            .set("contact", getPara("contact"))
                            .set("remark", getPara("remark"));
                    result = result && p.update();
                    if (!result) return false;

                    List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_company` c ,`db_item` i,`db_item_project` p WHERE c.contract_id = " + id + " AND i.company_id=c.id AND p.item_id=i.id AND p.isPackage=1 ");
                    for (ItemProject itemProject : itemProjectList) {
                        result = result && itemProject.set("isPackage", null).update();
                        if (!result) break;
                    }
                    Integer[] ids = getParaValuesToInt("ids[]");
                    for (int i : ids) {
                        ItemProject itemProject = ItemProject.itemprojectDao.findById(i);
                        result = result && itemProject.set("isPackage", 1).update();
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


    /**
     * 删除分包信息
     */
    public void delete() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    int id = getParaToInt("id");
                    Contract contract = Contract.contractDao.findById(id);
                    Package p = Package.packageDao.findById(contract.get("package_id"));
                    List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_company` c ,`db_item` i,`db_item_project` p WHERE c.contract_id = " + id + " AND i.company_id=c.id AND p.item_id=i.id AND p.isPackage=1 ");
                    for (ItemProject itemProject : itemProjectList) {
                        result = result && itemProject.set("isPackage", null).update();
                        if (!result) break;
                    }
                    result = result && Package.packageDao.deleteById(p.get("id"));
                    result = result && contract.set("package_id", null).update();
                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void getContractPackageList() {
        try {
            int id = getParaToInt("id");
            List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_company` c ,`db_item` i,`db_item_project` p WHERE c.contract_id = " + id + " AND i.company_id=c.id AND p.item_id=i.id AND p.isPackage=1 ");
            List<Map> result = new ArrayList<>();
            for (ItemProject itemProject : itemProjectList) {
                Map temp = new HashMap();
                temp.put("item_project_id", itemProject.get("id"));
                MonitorProject monitorProject = MonitorProject.monitorProjectdao.findById(itemProject.get("project_id"));
                temp.put("id", monitorProject.get("id"));
                temp.put("name", monitorProject.get("name"));
                result.add(temp);
            }
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }
}
