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
        }
        return mapList;
    }

}
