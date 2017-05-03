package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.model.ItemProject;
import com.lims.model.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qulongjun on 2017/5/3.
 */
public class QualityController extends Controller {
    public void list() {
        try {
            int company_id = getParaToInt("company_id");
            List<ItemProject> itemProjectList = ItemProject.itemprojectDao.find("SELECT p.* FROM `db_company` c,`db_item` i,`db_item_project` p \n" +
                    "WHERE c.id=" + company_id + " AND i.company_id=c.id AND p.item_id=i.id");
            List result = new ArrayList();
            for (ItemProject itemProject : itemProjectList) {
                Map temp = new HashMap();
                temp = itemProject.toJsonSingle();
                List<Sample> sampleList = Sample.sampleDao.find("SELECT s.* FROM `db_sample` s,`db_sample_project` p WHERE s.company_id=" + company_id + " AND p.sample_id=s.id AND p.item_project_id=" + itemProject.get("id"));
                List<Map> re = new ArrayList<>();
                int count = 0;
                for (Sample sample : sampleList) {
                    re.add(sample.toSimpleJson());
                    if (sample.get("balance") != null) {
                        count++;
                    }
                }
                temp.put("sample", re);
                temp.put("sceneCount", count);
                result.add(temp);
            }
            renderJson(result);

        } catch (Exception e) {
            renderError(500);
        }
    }
}
