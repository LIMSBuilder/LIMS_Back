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
        this.put("create", 1);//创建合同完成
        this.put("review", 2);//合同审核通过
        this.put("finish",3);//合同执行完成
        this.put("change", -1);//审核未拒绝，待修改
        this.put("stop", -2);//合同中止
    }};

    //这是存放任务流程的控制参数
    public static Map TaskMap = new HashMap() {{
        this.put("create", 1);//创建合同完成
    }};

    public static int getContractProcess(String processName) {
        int process = (Integer) (ContractMap.get(processName) != null ? ContractMap.get(processName) : 0);
        return process;
    }

    public static int getTaskProcess(String processName) {
        int process = (Integer) (TaskMap.get(processName) != null ? TaskMap.get(processName) : 0);
        return process;
    }
}
