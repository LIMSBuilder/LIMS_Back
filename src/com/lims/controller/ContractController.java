package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.*;

import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by caiwenhong on 2017/2/28.
 */
public class ContractController extends Controller {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    SimpleDateFormat formate_date = new SimpleDateFormat("yyyy-MM-dd");

    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Map paraMaps = getParaMap();
                    Contract contract = new Contract();
                    Boolean result = true;
                    System.out.println(paraMaps.get("item[]"));
                    for (Object key : paraMaps.keySet()) {
                        switch (key.toString()) {
                            case "in_room":
                                contract.set("in_room", ((String[]) paraMaps.get(key))[0].equals("true") ? 1 : 0);
                                break;
                            case "secret":
                                contract.set("secret", ((String[]) paraMaps.get(key))[0].equals("true") ? 1 : 0);
                                break;
                            case "id":
                                break;//不知道为什么会传一个id过来，待观察
                            default:
                                if (key.toString().indexOf("item") != -1) {
                                    continue;
                                }
                                contract.set(key.toString(), ((String[]) paraMaps.get(key))[0]);
                        }

                    }
                    User user = ParaUtils.getCurrentUser(getRequest());
                    contract.set("identify", createIdentify()).set("create_time", sdf.format(new Date())).set("creater", user.get("id")).set("process", 1);
                    result = result && contract.save();
                    String[] items = getParaValues("project_items[]");
                    for (String item : items) {
                        Map temp = Jackson.getJson().parse(item, Map.class);
                        Contractitem contractitem = new Contractitem();
                        List points = (ArrayList) temp.get("point");
                        String point = "";
                        if (points != null) {
                            for (int i = 0; i < points.size(); i++) {
                                point += points.get(i);
                                if (i != points.size() - 1) {
                                    point += ",";
                                }
                            }
                        }

                        result = result && contractitem.set("element", ((Map) temp.get("element")).get("id")).set("company", temp.get("company")).set("point", point).set("contract_id", contract.get("id")).set("other", temp.get("other")).set("is_package", temp.get("is_package")).save();
                        if (!result) break;
                        List<Map> projectList = (ArrayList) temp.get("project");
                        if (projectList != null) {
                            for (int m = 0; m < projectList.size(); m++) {
                                Map project = projectList.get(m);
                                ItemProject entry = new ItemProject();
                                entry.set("item_id", contractitem.get("id")).set("project_id", project.get("id"));
                                result = result && entry.save();
                                if (!result) break;
                            }
                        }
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

    public String createIdentify() {
        String identify = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        identify = sdf.format(new Date());
        Encode encode = Encode.encodeDao.findFirst("SELECT * FROM `db_encode`");
        if (encode == null) {
            //数据库中没有第一条记录，则创建它
            Encode entry = new Encode();
            entry.set("contract_identify", 0).save();
            identify += String.format("%04d", 1);
        } else {
            int identify_Encode = (encode.get("contract_identify") == null ? 0 : encode.getInt("contract_identify")) + 1;
            encode.set("contract_identify", identify_Encode).update();
            identify += String.format("%04d", identify_Encode);
        }
        return identify;
    }


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
                if (key.equals("process")) {
                    param += " AND " + key + " = " + value;
                    continue;
                }
                if (key.equals("keyWords")) {
                    param += (" AND ( identify ='" + value + "' OR name like \"%" + value + "%\" OR client_unit like \"%" + value + "%\")");
                    continue;
                }
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Contract> contractPage = Contract.contractDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_contract`" + param + " ORDER BY create_time DESC");
            List<Contract> contractList = contractPage.getList();
            Map results = toJson(contractList);
            results.put("currentPage", currentPage);
            results.put("totalPage", contractPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);
        } catch (Exception e) {
            renderError(500);

        }
    }

    public Map toJson(List<Contract> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Contract contract : entityList) {
                result.add(toJsonSingle(contract));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }


    public Map toJsonSingle(Contract entry) {
        Map temp = new HashMap();
        for (String key : entry._getAttrNames()) {
            switch (key) {
                case "trustee":
                    temp.put("trustee", User.userDao.findById(entry.get(key)).toSimpleJson());
                    break;
                case "type":
                    temp.put("type", Type.typeDao.findById(entry.get(key)));
                    break;
                default:
                    temp.put(key, entry.get(key));
            }

        }
        return temp;
    }


    public void getItems() {
        try {
            int id = getParaToInt("contract_id");
            Contract contract = Contract.contractDao.findById(id);
            if (contract != null) {
                renderJson(contract.getItems());
            } else renderJson(RenderUtils.CODE_SUCCESS);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Contract.contractDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void findById() {
        try {
            int id = getParaToInt("id");
            Contract contract = Contract.contractDao.findById(id);
            if (contract != null) {
                renderJson(toJsonSingle(contract));
            } else {
                renderJson(RenderUtils.CODE_EMPTY);
            }
        } catch (Exception e) {
        }
        renderError(500);
    }

    public void findByIdentify() {
        try {
            String identify = getPara("identify");

            Contract contract = Contract.contractDao.findFirst("select * from db_contracter where identify = '" + identify + "'");
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void defaultInfo() {
        try {
            Default defaultModel = Default.defaultDao.findFirst("SELECT * FROM `db_default`");
            Boolean result = true;
            if (defaultModel != null) {
                //更新
                defaultModel
                        .set("trustee_unit", getPara("trustee_unit"))
                        .set("trustee_address", getPara("trustee_address"))
                        .set("trustee_tel", getPara("trustee_tel"))
                        .set("trustee_code", getPara("trustee_code"))
                        .set("trustee_fax", getPara("trustee_fax"));
                result = result && defaultModel.update();
            } else {
                //创建新的Default
                Default temp = new Default();
                temp
                        .set("trustee_unit", getPara("trustee_unit"))
                        .set("trustee_address", getPara("trustee_address"))
                        .set("trustee_tel", getPara("trustee_tel"))
                        .set("trustee_code", getPara("trustee_code"))
                        .set("trustee_fax", getPara("trustee_fax"));
                result = result && temp.save();
            }
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void fetchDefault() {
        try {
            Default defaultModel = Default.defaultDao.findFirst("SELECT * FROM `db_default`");
            if (defaultModel != null) {
                renderJson(defaultModel);
            } else renderNull();
        } catch (Exception e) {
            renderError(500);
        }
    }
}
