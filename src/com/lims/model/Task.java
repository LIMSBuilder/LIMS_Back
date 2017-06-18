package com.lims.model;

import com.jfinal.plugin.activerecord.Model;
import org.junit.Test;

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

    public Map toJsonSingle() {
        Map result = new HashMap();
        result.put("id", this.get("id"));
        result.put("contract_id", this.get("contract_id") == null ? null : this.get("contract_id"));
        result.put("service_id", this.get("service_id") == null ? null : this.get("service_id"));
        result.put("client_unit", this.get("client_unit"));
        result.put("identify", this.get("identify"));
        result.put("type", this.get("id") == null ? " " : Type.typeDao.findById(this.get("type")).get("name"));
        result.put("client_unit", this.get("client_unit"));
        result.put("client_code", this.get("client_code"));
        result.put("client_tel", this.get("client_tel"));
        result.put("client", this.get("client"));
        result.put("client_fax", this.get("client_fax"));
        result.put("client_address", this.get("client_address"));
        result.put("name", this.get("name"));
        result.put("aim", this.get("aim"));
        result.put("type", Type.typeDao.findById(this.get("type")).get("name"));
        result.put("way", this.get("way"));
        result.put("wayDesp", this.get("wayDesp"));
        result.put("other", this.get("other"));
        result.put("process", this.get("process"));
        result.put("create_time", this.get("create_time"));
        result.put("creater", this.get("creater") == null ? null : User.userDao.findById(this.get("creater")).get("name"));
        result.put("charge",this.get("charge") == null ? null : User.userDao.findById(this.get("charge")).get("name"));
        result.put("importWrite", this.get("importWrite"));
        result.put("sample_creater", this.get("sample_creater") == null ? null : User.userDao.findById(this.get("sample_creater")).get("name"));
        result.put("sample_time",this.get("sample_time"));
        result.put("package",this.get("package"));
        result.put("receive_type",this.get("receive_type"));
        result.put("additive",this.get("additive"));
        result.put("receive_time",this.get("receive_time"));
        result.put("sample_receiver",this.get("sample_receiver") == null ? null : User.userDao.findById(this.get("sample_receiver")).get("name"));
        return result;
    }
}

