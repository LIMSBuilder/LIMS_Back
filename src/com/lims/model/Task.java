package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qulongjun on 2017/3/10.
 */
public class Task extends Model<Task> {
    public static Task taskDao = new Task();

    public List getItems() {
        List<Map> mapList = new ArrayList<>();
        List<Company> companyList = Company.companydao.find("SELECT * FROM `db_company` WHERE contract_id=" + this.get("id"));
        for (Company company : companyList) {
            Map temp = new HashMap();
            for (String key : company._getAttrNames()) {
                temp.put(key, company.get(key));
            }
            List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_contract_item` WHERE company_id=" + company.get("id"));
            List<Map> itemsList = new ArrayList<>();
            for (Contractitem contractitem : contractitemList) {
                itemsList.add(contractitem.toSimpleJson());
            }
            temp.put("items", itemsList);
            mapList.add(temp);
        }
        return mapList;
    }
}

