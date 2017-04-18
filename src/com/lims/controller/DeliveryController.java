package com.lims.controller;

import com.jfinal.core.Controller;
import com.lims.utils.ParaUtils;

import java.util.Date;

/**
 * Created by chenyangyang on 2017/4/18.
 */
public class DeliveryController extends Controller {
    public  void  delivery(){
        try {
            String date = ParaUtils.sdf2.format(new Date());

        }catch (Exception e){
            renderError(500);
        }
    }
}
