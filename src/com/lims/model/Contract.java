package com.lims.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/28.
 */
public class Contract extends Model<Contract> {
    public static Contract contractDao = new Contract();

    public List getItems() {
        List<Map> mapList = new ArrayList<>();
        List<Company> companyList = Company.companydao.find("SELECT * FROM `db_company` WHERE contract_id=" + this.get("id"));
        for (Company company : companyList) {
            if (company.get("company") != null) {
                //不为空，导入的
                Map temp = new HashMap();
                for (String key : company._getAttrNames()) {
                    temp.put(key, company.get(key));
                }
                List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + company.get("id"));
                List<Map> itemsList = new ArrayList<>();
                for (Contractitem contractitem : contractitemList) {
                    itemsList.add(contractitem.toSimpleJson());
                }
                temp.put("items", itemsList);
                mapList.add(temp);
            } else {
                //为空，手动创建的
                if (mapList.size() == 0) {
                    Map temp = new HashMap();
                    for (String key : company._getAttrNames()) {
                        temp.put(key, company.get(key));
                    }
                    List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + company.get("id"));
                    List<Map> itemsList = new ArrayList<>();
                    for (Contractitem contractitem : contractitemList) {
                        itemsList.add(contractitem.toSimpleJson());
                    }
                    temp.put("items", itemsList);
                    mapList.add(temp);
                } else {
                    List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + company.get("id"));
                    List<Map> itemsList = new ArrayList<>();
                    for (Contractitem contractitem : contractitemList) {
                        itemsList.add(contractitem.toSimpleJson());
                    }
                    ((List) mapList.get(0).get("items")).addAll(itemsList);
                }
            }
        }
        return mapList;
    }
    public Map Json() {
        Map temp = new HashMap();
        temp.put("id", this.get("id"));
        temp.put("identify", this.get("identify"));
        temp.put("client_unit", this.get("client_unit"));
        temp.put("client_code", this.get("client_code"));
        temp.put("client_tel", this.get("client_tel"));
        temp.put("client", this.get("client"));
        temp.put("client_fax", this.get("client_fax"));
        temp.put("client_address", this.get("client_address"));
        temp.put("trustee_unit", this.get("trustee_unit"));
        temp.put("trustee_code", this.get("trustee_code"));
        temp.put("trustee_tel", this.get("trustee_tel"));
        temp.put("trustee", User.userDao.findById(this.get("trustee")).get("name"));
        temp.put("trustee_fax", this.get("trustee_fax"));
        temp.put("trustee_address", this.get("trustee_address"));
        temp.put("name", this.get("name"));
        temp.put("aim", this.get("aim"));
        temp.put("type", Type.typeDao.findById(this.get("type")).get("name"));
        temp.put("way", this.get("way"));
        temp.put("wayDesp", this.get("wayDesp"));
        temp.put("isPackage", this.get("isPackage"));
        temp.put("in_room", this.get("in_room"));
        temp.put("secret", this.get("secret"));
        temp.put("paymentWay", this.get("paymentWay"));
        temp.put("finish_time", this.get("finish_time"));
        temp.put("payment", this.get("payment"));
        temp.put("other", this.get("other"));
        temp.put("process", this.get("process"));
        temp.put("review_id", this.get("review_id") == null ? null : User.userDao.findById(this.get("review_id")).get("name"));
        temp.put("review_time", this.get("review_time"));
        temp.put("create_time", this.get("create_time"));
        temp.put("creater", this.get("creater") == null ? null : User.userDao.findById(this.get("creater")).get("name"));
        temp.put("reviewer", this.get("reviewer") == null ? null : User.userDao.findById(this.get("reviewer")).get("name"));
        temp.put("update_time", this.get("update_time"));
        temp.put("package_id", this.get("package_id"));
        temp.put("importWrite", this.get("importWrite"));
        List<Map> mapList = new ArrayList<>();
        List<Company> companyList = Company.companydao.find("SELECT * FROM `db_company` WHERE contract_id=" + this.get("id"));
        for (Company company : companyList) {
            if (company.get("company") != null) {
                //不为空，导入的
                Map tem = new HashMap();
                for (String key : company._getAttrNames()) {
                    tem.put(key, company.get(key));
                }
                List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + company.get("id"));
                List<Map> itemsList = new ArrayList<>();
                for (Contractitem contractitem : contractitemList) {
                    itemsList.add(contractitem.toSimpleJson());
                }
                tem.put("items", itemsList);
                mapList.add(temp);
            } else {
                //为空，手动创建的
                if (mapList.size() == 0) {
                    Map tem = new HashMap();
                    for (String key : company._getAttrNames()) {
                        tem.put(key, company.get(key));
                    }
                    List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + company.get("id"));
                    List<Map> itemsList = new ArrayList<>();
                    for (Contractitem contractitem : contractitemList) {
                        itemsList.add(contractitem.toSimpleJson());
                    }
                    tem.put("items", itemsList);
                    mapList.add(temp);
                } else {
                    List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + company.get("id"));
                    List<Map> itemsList = new ArrayList<>();
                    for (Contractitem contractitem : contractitemList) {
                        itemsList.add(contractitem.toSimpleJson());
                    }
                    ((List) mapList.get(0).get("items")).addAll(itemsList);
                }
            }
        }
        temp.put("map",mapList);

        return temp;
    }


}
