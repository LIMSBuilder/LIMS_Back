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

    //这是存放服务合同流程的控制参数
    public static Map ServiceMap = new HashMap() {{
        this.put("create", 1);//待审核
        this.put("review", 2);//待执行
        this.put("finish", 3);//已执行
        this.put("change", -1);//待修改
        this.put("stop", -2);//已中止
    }};

    //这是存放任务流程的控制参数
    public static Map TaskMap = new HashMap() {{
        this.put("stop", -2);//已中止任务
        this.put("create", 1);//创建任务完成，已下达 办公室派发给具体负责人待派遣
        this.put("dispatch", 2);//派遣任务，待样品登记
        this.put("apply", 3);//样品登记
        this.put("laboratory",4);//实验室样品交接
        this.put("quality", 5);//质控 质控表
        this.put("lab",6);//质控完成，重新流转到实验室


    }};

    public static Map ItemMap = new HashMap() {{
        this.put("beforeApply", 0);//样品号待申请
        this.put("afterApply", 1);//样品号已申请
    }};
    //存放样品进度
    public static Map SampleMap = new HashMap() {{
        this.put("apply", 0);//自送样未登记
        this.put("create", 1);//自送样已经登记
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


    public static int getServiceProcess(String processName) {
        int process = (Integer) (ServiceMap.get(processName) != null ? ServiceMap.get(processName) : 0);
        return process;
    }
}
