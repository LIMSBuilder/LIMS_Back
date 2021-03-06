package com.lims.utils;

import com.lims.model.Contract;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2017/3/7.
 */
public class ProcessKit {
    //这是存放合同流程的控制参数
    public static Map ContractMap = new HashMap() {{
        this.put("create", 1);//待审核
        this.put("review", 2);//待执行
        this.put("finish", 3);//已执行
        this.put("change", -1);//待修改
        this.put("stop", -2);//已中止
    }};

    //这是存放任务流程的控制参数
    public static Map TaskMap = new HashMap() {{
        this.put("stop", -2);//已中止
        this.put("create", 1);//创建合同完成,未派遣
        this.put("dispatch", 2);//任务派遣完成
        //this.put("sample",3);
    }};

    public static Map ItemMap = new HashMap() {{
        this.put("beforeApply", 0);//样品号待申请
        this.put("afterApply", 1);//样品号已申请
    }};
    //存放样品进度
    public static Map SampleMap = new HashMap() {{
        this.put("apply", 0);//申请样品号
        this.put("create", 1);//登记样品信息
    }};


    public static int getContractProcess(String processName) {
        int process = (Integer) (ContractMap.get(processName) != null ? ContractMap.get(processName) : 0);
        return process;
    }

    public static int getTaskProcess(String processName) {
        int process = (Integer) (TaskMap.get(processName) != null ? TaskMap.get(processName) : 0);
        return process;
    }

    public static int getItemProcess(String processName) {
        int process = (Integer) (ItemMap.get(processName) != null ? ItemMap.get(processName) : 0);
        return process;
    }

    public static int getSampleProcess(String processName) {
        int process = (Integer) (SampleMap.get(processName) != null ? SampleMap.get(processName) : 0);
        return process;
    }
}
