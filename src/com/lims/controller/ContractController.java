package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Contract;

import com.lims.model.Identify;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by caiwenhong on 2017/2/28.
 */
public class ContractController extends Controller {
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    SimpleDateFormat format_date=new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat new_format_date=new SimpleDateFormat("yyyy-MM-dd");
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    String identify = createIdentify();
                    String client_unit = getPara("client_unit");
                    String client_code = getPara("client_code");
                    String client_tel = getPara("client_tel");
                    String client = getPara("client");
                    String client_fax = getPara("client_fax");
                    String client_address = getPara("client_address");
                    String trustee_unit = getPara("trustee_unit");
                    String trustee_code = getPara("trustee_code");
                    String trustee_tel = getPara("trustee_tel");
                    int trustee = getParaToInt("trustee");
                    String trustee_fax = getPara("trustee_fax");
                    String trustee_address = getPara("trustee_address");
                    String project_name = getPara("name");
                    String aim = getPara("aim");
                    int type_id = getParaToInt("type");
                    int projectWay = getParaToInt("way");
                    String wayDesp = getPara("wayDesp");
                    String package_unit = getPara("package_unit");
                    //int in_room = getParaToInt("in_room");
                    int in_room = getParaToBoolean("in_room") ? 1 : 0;
                    int secret = getParaToBoolean("secret")?1:0;
                    String pay_way = getPara("paymentWay");
                    String finish_time = getPara("finish_time");
                    float payment = getParaToLong("way");
                    String other = getPara("other");
                   // int process = getParaToInt("process");
                    //int review_id = getParaToInt("review_id");
                    //String review_time = getPara("review_time");
                    String create_time = getPara("create_time");

                    Boolean result = true;
                    if (Contract.contractDao.find("SELECT * FROM `db_contract` WHERE  identify='" + identify + "'").size() != 0) {
                        renderJson(RenderUtils.CODE_REPEAT);
                    } else {
                        Contract contract = new Contract();
                        result = result && contract
                                .set("identify", identify)
                                .set("client_unit", client_unit)
                                .set("client_code", client_code)
                                .set("client_tel", client_tel)
                                .set("client", client)
                                .set("client_fax", client_fax)
                                .set("client_address", client_address)
                                .set("trustee_unit", trustee_unit)
                                .set("trustee_code", trustee_code)
                                .set("trustee_tel", trustee_tel)
                                .set("trustee", trustee)
                                .set("trustee_fax", trustee_fax)
                                .set("trustee_address", trustee_address)
                                .set("project_name", project_name)
                                .set("aim", aim)
                                .set("type_id", type_id)
                                .set("projectWay", projectWay)
                                .set("wayDesp", wayDesp)
                                .set("package_unit", package_unit)
                                .set("in_room", in_room)
                                .set("secret", secret)
                                .set("pay_way", pay_way)
                                .set("finish_time", finish_time)
                                .set("payment", payment)
                                .set("other", other)
                                .set("process", 1)
                                .set("create_time", sdf.format(new Date())).save();
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
        Prop p= PropKit.use("identify.properties");
        String identify=p.get("contract_identify");
        Date now=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String identify_value=sdf.format(now);
        String pre=identify_value.split("-")[0];
        String zero=identify_value.split("-")[1];
        Identify identify = Identify.identifyDao.findFirst("SELECT * FROM `db_encode`");
        int id_num= identify.getId();
        DecimalFormat  df=new DecimalFormat(zero);
        String id=df.format(id_num);
        String identify1=pre +" - "+id;;
        while (Identify.identifyDao.find("select * from `db_encode` where= identify'"+identify+"'").size()!=0){
            id_num++;
            id=df.format(id_num);
            identify1=pre+"-"+id;
        }
        Boolean result=Identify.identifyDao.setId(id_num);
        Map idMap=new HashMap();
        idMap.put("identify1",identify1);
        renderJson(result?idMap:RenderUtils.CODE_ERROR);
        //从数据库中读取当前idengtify记录
        //+1  return

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
                param += (" AND " + key + " like \"%" + value + "%\"");
            }
            Page<Contract> contractPage = Contract.contractDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_contract`" + param);
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


    public Map toJsonSingle(Contract contract) {
        Map<String, Object> contract1 = new HashMap<>();
        contract1.put("id", contract.getInt("id"));
        contract1.put("identify", contract.get("identify"));
        contract1.put("client_unit", contract.get("client_unit"));
        contract1.put("client_code", contract.get("client_code"));
        contract1.put("client_tel", contract.get("client_tel"));
        contract1.put("client", contract.get("client"));
        contract1.put("client_fax", contract.get("client_fax"));
        contract1.put("client_address", contract.get("client_address"));
        contract1.put("trustee_unit", contract.get("trustee_unit"));
        contract1.put("trustee_code", contract.get("trustee_unit"));
        contract1.put("trustee_tel", contract.get("trustee_tel"));
        contract1.put("trustee", contract.getInt("trustee"));
        contract1.put("trustee_fax", contract.get("trustee_fax"));
        contract1.put("trustee_address", contract.get("trustee_address"));
        contract1.put("project_name", contract.get("name"));
        contract1.put("aim", contract.get("aim"));
        contract1.put("type_id", contract.getInt("type_id"));
        contract1.put("projectWay", contract.getInt("paojectWay"));
        contract1.put("wayDesp", contract.get("wayDesp"));
        contract1.put("package_unit", contract.get("package_unit"));
        contract1.put("in_room", contract.getInt("in_room"));
        contract1.put("secert", contract.getInt("secert"));
        contract1.put("pay_way", contract.get("pay_way"));
        contract1.put("finish_time", contract.get("finish_time"));
        contract1.put("payment", contract.getFloat("payment"));
        contract1.put("other", contract.get("other"));
       // contract1.put("process", contract.get("process"));
       // contract1.put("review_id", contract.get("review_id"));
       // contract1.put("review_time", contract.get("review_time"));
        contract1.put("create_time", contract.get("create_time"));
        return contract1;
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
}
