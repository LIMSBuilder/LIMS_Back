package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.utils.RenderUtils;

/**
 * Created by qulongjun on 2017/3/10.
 */
public class TaskController extends Controller {

    public void create() {
        try {
            renderJson(RenderUtils.CODE_SUCCESS);
        } catch (Exception e) {
            renderError(500);
        }
    }
}
