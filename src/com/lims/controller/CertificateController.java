package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Certificate;
import com.lims.model.Equipment;
import com.lims.model.MonitorProject;
import com.lims.model.User;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;
import org.apache.poi.ss.formula.functions.T;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyangyang on 2017/5/20.
 */
public class CertificateController extends Controller {
    public void create() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;

                    int people = getParaToInt("person");
                    Integer[] projectlist = getParaValuesToInt("name[]");
                    if (projectlist == null) {

                    } else {
                        for (int id : projectlist) {
                            if (Certificate.certificateDao.find("select * from `db_lab_certificate` where lab = '" + people + "'AND project_id = " + id).size() != 0) {
                                renderJson(RenderUtils.CODE_REPEAT);
                            } else {
                                Certificate certificate = new Certificate();
                                certificate.set("lab", people)
                                        .set("project_id", id);
                                result = result && certificate.save();
                                if (!result) return false;
                            }
                        }
                    }

                    return result;
                }
            });
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);

        } catch (Exception e) {
            renderError(500);
        }


    }

    //删除
    public void delete() {
        try {
            int id = getParaToInt("id");
            Boolean result = Certificate.certificateDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 批量删除
     */
    public void deleteAll() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Integer[] selected = getParaValuesToInt("selected[]");
                    Boolean result = true;
                    for (int i = 0; i < selected.length; i++) {
                        int id = selected[i];
                        result = result && Certificate.certificateDao.deleteById(id);
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

    //修改
    public void change() {
        try {
            int id = getParaToInt("id");
            Certificate certificate = Certificate.certificateDao.findById(id);
            Boolean result = certificate.set("lab", getParaToInt("person")).set("project_id", getParaToInt("name")).update();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception E) {
            renderError(500);
        }
    }

    //返回实验室名单
    public void userlist() {
        try {
            List<User> userList = User.userDao.find("SELECT u.*  FROM `db_user` u,`db_role` r,`db_department` d where d.name ='实验室' AND r.department_id = d.id AND u.roleId = r.id ");
            if (userList != null) {
                renderJson(toJson(userList));
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<User> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (User user : entityList) {
                result.add(toJsonSingle(user));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    /**
     * 将单个User序列化为Map对象
     *
     * @param user
     * @return
     */
    public Map toJsonSingle(User user) {
        Map<String, Object> equip = new HashMap<>();
        equip.put("id", user.get("id"));
        equip.put("name", user.get("name"));
        return equip;
    }


    //返回监测项目名字
    public void projectList() {
        try {
            List<MonitorProject> monitorProjectList = MonitorProject.monitorProjectdao.find("SELECT  * from `db_monitor_project`");
            if (monitorProjectList != null) {
                renderJson(toJson1(monitorProjectList));
            }
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson1(List<MonitorProject> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (MonitorProject monitorProject : entityList) {
                result.add(toJsonSingle1(monitorProject));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    /**
     * 将单个MonitorProject序列化为Map对象
     *
     * @param monitorProject
     * @return
     */
    public Map toJsonSingle1(MonitorProject monitorProject) {
        Map<String, Object> equip = new HashMap<>();
        equip.put("id", monitorProject.get("id"));
        equip.put("name", monitorProject.get("name"));
        return equip;
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
            Page<Certificate> certificatePage = Certificate.certificateDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_lab_certificate`" + param);
            List<Certificate> certificateList = certificatePage.getList();
            Map results = toJson2(certificateList);
            results.put("currentPage", currentPage);
            results.put("totalPage", certificatePage.getTotalPage());
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
    public Map toJson2(List<Certificate> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (Certificate certificate : entityList) {
                result.add(toJsonSingle2(certificate));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    /**
     * 将单个Certificate序列化为Map对象
     *
     * @param certificate
     * @return
     */
    public Map toJsonSingle2(Certificate certificate) {
        Map<String, Object> equip = new HashMap<>();
        equip.put("id", certificate.getInt("id"));
        equip.put("name", User.userDao.findById(certificate.get("lab")).get("name"));
        equip.put("monitor", MonitorProject.monitorProjectdao.findById(certificate.get("project_id")).get("name"));
        return equip;
    }


    public  void findById(){
        try {
            int id =getParaToInt("id");
            Certificate  certificate =Certificate.certificateDao.findById(id);
            if(certificate !=null){
                renderJson(toJsonSingle2(certificate));
            }
            else {
                renderJson(RenderUtils.CODE_EMPTY);
            }

        }catch (Exception e){
            renderError(500);
        }
    }
}
