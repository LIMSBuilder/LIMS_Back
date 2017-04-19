package com.lims.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/4/18.
 */
public class Company extends Model<Company> {
    public static Company companydao = new Company();


    public Map toSimpleJSON() {
        Map result = new HashMap();
        for (String key : this._getAttrNames()) {
            if (key.equals("company")) {
                if (this.get("flag") == 0) {
                    Task task = Task.taskDao.findById(this.get("task_id"));
                    result.put("company", task.get("client_unit"));
                    continue;
                }
            }
            result.put(key, this.get(key));
        }
        List<Contractitem> contractitemList = Contractitem.contractitemdao.find("SELECT * FROM `db_item` WHERE company_id=" + this.get("id"));
        List<Map> items = new ArrayList<>();
        for (Contractitem contractitem : contractitemList) {
            items.add(contractitem.toSimpleJson());
        }
        result.put("items", items);
        List<DeliveryUser> deliveryUserList = DeliveryUser.deliveryUser.find("SELECT du.* FROM `db_delivery` d,`db_delivery_user` du WHERE d.company_id=" + this.get("id") + " AND du.delivery_id=d.id ");
        List<Map> joiner = new ArrayList<>();
        for (DeliveryUser deliveryUser : deliveryUserList) {
            Map temp = User.userDao.findById(deliveryUser.get("user_id")).toSimpleJson();
            temp.put("type", deliveryUser.get("type"));
            joiner.add(temp);
        }
        result.put("joiner", joiner);
        return result;
    }
}
