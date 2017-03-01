package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.lims.model.Department;
import com.lims.model.ItemProject;
import com.lims.utils.ParaUtils;
import com.lims.utils.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/28.
 */
public class ItemProjectController extends Controller {
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
            Page<ItemProject> itemProjectPage = ItemProject.itemprojectDao.paginate(currentPage, rowCount, "SELECT *", "FROM `db_item_project`" + param);
            List<ItemProject> itemProjectList = itemProjectPage.getList();
            Map results = toJson(itemProjectList);
            results.put("currentPage", currentPage);
            results.put("totalPage", itemProjectPage.getTotalPage());
            results.put("rowCount", rowCount);
            results.put("condition", condition_temp);
            renderJson(results);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<ItemProject> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (ItemProject itemProject : entityList) {
                result.add(toJsonSingle(itemProject));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(ItemProject itemProject) {
        Map<String, Object> item = new HashMap<>();
        item.put("item_id", itemProject.getInt("item_id"));
        item.put("project_id", itemProject.getInt("project_id"));
        return item;
    }

    public void create() {
        try {
            int item_id = getParaToInt("item_id");
            int project_id = getParaToInt("project_id");
            if (ItemProject.itemprojectDao.find("select * from `db_item_project` where item_id='" + item_id + "'and  project_id='" + project_id + "'").size() != 0) {
                renderJson(RenderUtils.CODE_REPEAT);
            } else {
                ItemProject itemProject = new ItemProject();
                Boolean result = itemProject.set("item_id", item_id).set("project_id", project_id).save();
            }

        } catch (Exception e) {
            renderError(500);
        }
    }

    public void delete() {
        try {
            int id = getParaToInt();
            Boolean result = ItemProject.itemprojectDao.deleteById(id);
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    public void change() {
        try {
            int id = getParaToInt("id");
            int item_id = getParaToInt("item_id");
            int project_id = getParaToInt("project_id");
            ItemProject itemProject = ItemProject.itemprojectDao.findById(id);
            Boolean result = itemProject.set("id", id).set("item_id", item_id).set("project_id", project_id).save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public void findByItem() {
        try {
            int item_id = getParaToInt("item_id");
            if (item_id != 0) {
                List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project` where item_id =" + item_id);
                renderJson(toJson(itemProjectList));
            } else
                renderJson(RenderUtils.CODE_REPEAT);

        } catch (Exception e) {
            renderError(500);
        }
    }

    public  void findByProject(){
        try {
            int project_id=getParaToInt("project_id");
            if(project_id!=0){
                List<ItemProject> itemProjectList=ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project` where project_id =" + project_id);
            }
            else {
                renderJson(RenderUtils.CODE_REPEAT);
            }
        }catch (Exception e)
        {renderError(500);}

    }
    public void total() {
        try {
            renderJson(toJson(ItemProject.itemprojectDao.find("SELECT * FROM `db_item_project`")));
        } catch (Exception e) {
            renderError(500);
        }
    }
}
