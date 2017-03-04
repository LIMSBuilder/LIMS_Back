package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.lims.utils.RenderUtils;

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
}
