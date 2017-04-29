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
            if (company.get("name") != null) {
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

}
