package com.lims.utils;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qulongjun on 2016/10/26.
 */
public class ParaUtils extends Controller {
    public final static Map flows = new HashMap() {{
        put("stop_task", -2);//中止任务书
        put("finish_task", -1);//结束任务
        put("create_task", 0);//任务创建成功,进入样品登记环节
        put("create_sample", 1);//样品登记成功,进入交接联单环节
        put("connect_sample", 2);//交接联单生成完成,进入质量控制环节
        put("create_quality", 3);//质量控制完成,进入样品接收环节
        put("receive_delivery", 4);//样品接收完成,进入实验人员分配
        put("task_dstribute", 5);//任务分配完成,进入实验分析环节
        put("master_review", 6);//实验分析-审核-复核完成,进入主任一审环节
        put("quality_review", 7);//主任一审完成,进入质量控制二审
        put("create_report", 8);//质量控制二审完成,进入报告编制环节
    }};


    /**
     * 获取系统配置的rowCount信息
     *
     * @return
     */
    public static int getRowCount() {
        Prop setting = PropKit.use("setting.properties");
        int rowCount = setting.getInt("rowCount");
        return rowCount;
    }

    /**
     * 拆分请求中的搜索条件
     *
     * @param condition
     * @return
     */
    public static Map getSplitCondition(String condition) {
        String[] conditionArr = condition.split("&&");
        Map paraMap = new HashMap<>();
        for (int i = 0; i < conditionArr.length; i++) {
            String temp = conditionArr[i];
            String[] kv = temp.split("=");
            if (kv.length == 2) paraMap.put(kv[0], convertRequestParam(kv[1]));
        }
        return paraMap;
    }

    /**
     * 过滤中文字符,防止乱码
     *
     * @param param
     * @return
     */
    public static String convertRequestParam(String param) {
        if (param != null) {
            try {
                return URLDecoder.decode(param, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.out.println("request convert to UTF-8 error ");
            }
        }
        return "";
    }

    /**
     * 利用MD5进行加密
     *
     * @param str 待加密的字符串
     * @return 加密后的字符串
     * @throws NoSuchAlgorithmException     没有这种产生消息摘要的算法
     * @throws UnsupportedEncodingException
     */
    public static String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }


    /**
     * 判断用户密码是否正确
     *
     * @param newpasswd 用户输入的密码
     * @param oldpasswd 数据库中存储的密码－－用户密码的摘要
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static boolean checkpassword(String newpasswd, String oldpasswd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (EncoderByMd5(newpasswd).equals(oldpasswd))
            return true;
        else
            return false;
    }


    /**
     * 判断key是否在数组arrs中
     *
     * @param arrs
     * @param key
     * @return
     */
    public static Boolean isInArray(String[] arrs, String key) {
        for (int i = 0; arrs != null && i < arrs.length; i++) {
            if (arrs[i].equals(key)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 处理的最大数字达千万亿位 精确到分
     *
     * @return
     */
    public static String DigitUppercase(String num) throws Exception {
        String fraction[] = {"角", "分"};
        String digit[] = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        /**
         *      仟        佰        拾         ' '
         ' '    $4        $3        $2         $1
         万     $8        $7        $6         $5
         亿     $12       $11       $10        $9
         */
        String unit1[] = {"", "拾", "佰", "仟"};//把钱数分成段,每四个一段,实际上得到的是一个二维数组
        String unit2[] = {"元", "万", "亿", "万亿"}; //把钱数分成段,每四个一段,实际上得到的是一个二维数组
        BigDecimal bigDecimal = new BigDecimal(num);
        bigDecimal = bigDecimal.multiply(new BigDecimal(100));
//        Double bigDecimal = new Double(name*100);     存在精度问题 eg：145296.8
        String strVal = String.valueOf(bigDecimal.toBigInteger());
        String head = strVal.substring(0, strVal.length() - 2);         //整数部分
        String end = strVal.substring(strVal.length() - 2);              //小数部分
        String endMoney = "";
        String headMoney = "";
        if ("00".equals(end)) {
            endMoney = "整";
        } else {
            if (!end.substring(0, 1).equals("0")) {
                endMoney += digit[Integer.valueOf(end.substring(0, 1))] + "角";
            } else if (end.substring(0, 1).equals("0") && !end.substring(1, 2).equals("0")) {
                endMoney += "零";
            }
            if (!end.substring(1, 2).equals("0")) {
                endMoney += digit[Integer.valueOf(end.substring(1, 2))] + "分";
            }
        }
        char[] chars = head.toCharArray();
        Map<String, Boolean> map = new HashMap<String, Boolean>();//段位置是否已出现zero
        boolean zeroKeepFlag = false;//0连续出现标志
        int vidxtemp = 0;
        for (int i = 0; i < chars.length; i++) {
            int idx = (chars.length - 1 - i) % 4;//段内位置  unit1
            int vidx = (chars.length - 1 - i) / 4;//段位置 unit2
            String s = digit[Integer.valueOf(String.valueOf(chars[i]))];
            if (!"零".equals(s)) {
                headMoney += s + unit1[idx] + unit2[vidx];
                zeroKeepFlag = false;
            } else if (i == chars.length - 1 || map.get("zero" + vidx) != null) {
                headMoney += "";
            } else {
                headMoney += s;
                zeroKeepFlag = true;
                map.put("zero" + vidx, true);//该段位已经出现0；
            }
            if (vidxtemp != vidx || i == chars.length - 1) {
                headMoney = headMoney.replaceAll(unit2[vidx], "");
                headMoney += unit2[vidx];
            }
            if (zeroKeepFlag && (chars.length - 1 - i) % 4 == 0) {
                headMoney = headMoney.replaceAll("零", "");
            }
        }
        return headMoney + endMoney;
    }


    /**
     * 获取当前登录用户
     *
     * @return
     */
//    public static User getCurrentUser(HttpServletRequest request) {
//        //User user = User.userDao.findById(1);
//        User user = (User) request.getSession().getAttribute("user");
//        return user;
//    }

}
