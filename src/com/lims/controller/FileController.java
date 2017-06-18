package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.lims.model.Customer;
import com.lims.model.FileRecord;
import com.lims.model.Report;
import com.lims.utils.RenderUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caiwenhong on 2017/2/25.
 */
public class FileController extends Controller {
    /**
     * 文件上传功能
     */
    public void upload() {
        try {
            UploadFile uploadFile = getFile();
            Map result = RenderUtils.codeFactory(200);
            String path = " /upload\\" + uploadFile.getFileName();
            System.out.println(path);
            result.put("path", path);
            result.put("fileName", uploadFile.getOriginalFileName());
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * list
     **/
    public void sceneList() {
        try {

            List<FileRecord> fileRecordList = FileRecord.fileRecordDao.find("select * from `db_sample_record`");
            renderJson(toJson(fileRecordList));
        } catch (Exception e) {
            renderError(500);
        }
    }

    public Map toJson(List<FileRecord> entityList) {
        Map<String, Object> json = new HashMap<>();
        try {
            List result = new ArrayList();
            for (FileRecord fileRecord : entityList) {
                result.add(toJsonSingle(fileRecord));
            }
            json.put("results", result);
        } catch (Exception e) {
            renderError(500);
        }
        return json;
    }

    public Map toJsonSingle(FileRecord entry) {
        Map temp = new HashMap();
        temp.put("id", entry.get("id"));
        temp.put("name", entry.get("name"));
        temp.put("company_id", entry.get("company_id"));
        temp.put("file_path", entry.get("file_path"));
        return temp;
    }


    /**
     * 现场采样原始记录上传图片功能
     **/
    public void sceneSampleUpdoad() {
        try {
            UploadFile uploadFile = getFile();
            Map result = RenderUtils.codeFactory(200);
            String path = " /upload\\" + uploadFile.getFileName();
            System.out.println(path);
            result.put("file_path", path);
            result.put("name", uploadFile.getOriginalFileName());
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }

    }

    /**
     * 现场采样原始记录保存
     **/
    public void saveSceneSample() {
        try {
            int company_id = getParaToInt("company_id");
            String name = getPara("name");
            String file_path = getPara("file_path");
            FileRecord fileRecord = new FileRecord();
            boolean result = fileRecord.set("company_id", company_id).set("name", name).set("file_path", file_path).save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 删除原始记录功能
     **/
    public void deleteSceneSample() {
        try {
            int id = getParaToInt("id");
            boolean result = FileRecord.fileRecordDao.deleteById("id");
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }


    public Map toJsonSingle1(FileRecord fileRecord) {
        Map<String, Object> record = new HashMap<>();
        record.put("name", fileRecord.get("name"));
        record.put("file_path", fileRecord.get("file_path"));
        return record;
    }

    /***
     * 删除全部
     * */
    public void deleteAllSceneSample() {
        try {
            Boolean result = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Boolean result = true;
                    Integer[] selected = getParaValuesToInt("selected[]");
                    for (int id : selected) {
                        result = result && Customer.customerDao.deleteById(id);
                        if (!result) break;
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 通过公司找到现场采样记录列表
     **/
    public void findByCompanyId() {
        try {
            int company_id = getParaToInt("company_id");
            List<FileRecord> fileRecordList = FileRecord.fileRecordDao.find("select * from `db_sample_record` where company_id =" + company_id);
            renderJson(toJson(fileRecordList));

        } catch (Exception e)

        {
            renderError(500);
        }
    }

    /**
     * 上传报告功能
     **/
    public void saveReport() {
        try {
            int company_id = getParaToInt("company_id");
            String type = getPara("type");
            String report_path = getPara("report_path");
            Report report = new Report();
            Boolean result = report.set("company_id", company_id).set("type", type).set("report_path", report_path).set("process", 1).save();
            renderJson(result ? RenderUtils.CODE_SUCCESS : RenderUtils.CODE_ERROR);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 显示当前公司的所有报告
     **/
    public void reportList() {
        try {
            int company_id = getParaToInt("company_id");
            List<Report> reportList = Report.report.find("SELECT * FROM `db_report` WHERE company_id=" + company_id);
            List<Map> result = new ArrayList<>();
            for (Report report : reportList) {
                result.add(report.Json());
            }
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }
}
