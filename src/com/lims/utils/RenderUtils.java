package com.lims.utils;

import com.jfinal.kit.JsonKit;

import java.util.HashMap;
import java.util.Map;

/**
 * 200:服务器执行成功
 * 500:服务器通用异常
 * 501:数据不能为空
 * 502:数据库操作异常
 * 503:数据值不能重复
 * 504:数据库无该条记录
 * 505:业务只允许有一个重复值
 */
public class RenderUtils {

    public static final String CODE_SUCCESS = "{\"code\":200}";//成功
    public static final String CODE_NOTEMPTY = "{\"code\":501}";//不能为空
    public static final String CODE_ERROR = "{\"code\":502}";//数据库异常
    public static final String CODE_REPEAT = "{\"code\":503}";//重复
    public static final String CODE_EMPTY = "{\"code\":504}";//数据库无该条记录
    public static final String CODE_UNIQUE = "{\"code\":505}";//业务只允许有一个重复值

    /**
     * 状态码返回
     *
     * @param code
     * @return
     */
    public static Map codeFactory(int code) {
        Map jsonMap = new HashMap();
        jsonMap.put("code", code);
        return jsonMap;
    }

}
