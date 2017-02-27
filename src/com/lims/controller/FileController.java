package com.lims.controller;

import com.jfinal.core.Controller;
import com.jfinal.kit.FileKit;
import com.jfinal.upload.UploadFile;
import com.lims.utils.RenderUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            renderJson(result);
        } catch (Exception e) {
            renderError(500);
        }
    }

    /**
     * 测试功能，勿删
     */
    public void test() {
        renderJson(RenderUtils.CODE_UNIQUE);
    }
}
