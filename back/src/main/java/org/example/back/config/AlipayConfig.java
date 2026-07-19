package org.example.back.config;

import java.io.FileWriter;
import java.io.IOException;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *修改日期：2017-04-05
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 *
 * 沙箱环境不稳定
 */

public class AlipayConfig {
	
//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	// 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
	public static String app_id = "9021000122689434";
	
	// 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbJKu+e77AxvkK64rmyBmR0B3JfjN7Lp8jAdjVb1HFDRfrki0VJqfu9DaS96ki0OCR1UAcGVC67PT7MGH54ND6HyqTqZF33pBasSOLNj6LFFhHKfNVJ+7FhVh06mxc87cIV7le5dmNh93E0BZZn3cSLwdIFmgsdBN/8ChcZNUrC9fudEUE6M+eeos8+Pw3KC0hMrCcCNx0MqpmWU4zAupX+bIYkhR+DcN9CAJKWhgEk3+yykcPncKEshgPOLZlIaJjECvDlcC2C4rzkepXIrdBiKvaSa1gor5CxquwQwLBroVZ1/GlFcvRPBVFc5tFSNXwVcMSjsP6f10x9iKJH7C7AgMBAAECggEBAIze+2N3EXoSMciMoA912mHgS2vOQAi2CKHM54rGDcWTYgjxLiZqd0ouTe+dGeMXLxK8Q/4/rmJNDxWWlSBD1v2Gly6FZiUl3oW7Gr8pfhjjXFUh7mWAYPrRXcSrp8GvOp9EqHlW0/gDSoePOvweJAxKz53RWr4imCIvezaKCFWQdRVD8cWfNfVYe97oaNezLa2GxW4tOZuQQi0/QIMF5ha7ty0L/ANcefFkD2LvTHVkjqCGnSwSSEZMt8pMYlJIErWZE/2IfmK7bjDHz8vuunpZROUzk3XUvDvxAmeqqg+tqnRfrTAKB6xgSfvNuOdxOrkBdXUdVByzmFtodXHuAMECgYEA92CiB3Z4WriWB+yfQ41VyF0EICMXgmQBj8Qcc/2Bz4ioapp1ELrhWL+Fb/QUrNX186EwJ50r2q14ftJN+fOWT18gWqiDChVwTClqfCz7+GZ5uu9oAC2iXJEOFnTmiZKBkNTeT/yU7mPgJcnWwxzNb8kba8zzmqL2It1N6IQgwTkCgYEAoI0GeF2mqbTOOUEWAoTF/4NFsjIR27QE2vnI4bOpTBt9EJi4XKSySa0V0K9g779ugEJzFpugH8xCZmSqh///P68RqF0dS+h5uHcDwZJimdq3HaoOz0ghvRTviNJ5quMv8KxfKvXSPeIzNjTWoAERKokw4hw+bG0iEA40e8b/pZMCgYAFioiWmjS/83qI2ugGX1gYkNzSy3ZvWUYfrkjk4DzUJT+Ag80zfrwlfMTEaTc9V92Wg3nMqVyeHVyhmpxciPRI+tyCq1ukEi7dxd+4wFk0izcW1N4DgwIsCKPEJQI0+q8kS2BfckzzuhD/R7Ij1LVVV6/hjkm2oQfaaBKirrL9IQKBgFTFAVSORhA2yegLCH798bkOa79BlpJFQ4cG+dzJyqrHnMoliLpJr2UdQXZ1Ex6jGcoYy8F1HB+N7sU3g57ZOME8Q3t85poMvi/6jv4mt276v1KnVrh1InM+WZcJYnorCEXwDTOWpZJNG7MURPoodhLaavgH56HCIYkOpwFvuc2HAoGAOMzgNl9zM35iVPSRXHSKhQ8Kc0IcOGjypnjEHavfK3V/MY/6hOqIKf8X3Cv7Il1etVgTpdDMHm6P2ENOHf901JIbkH1Qpfxrjma9fBAAKI8tV1Z/1zGv+O9xA0TkGOAhyjmQ3bC4dpPJJLnvo8uBmkj0NtCl9qQpN7l7Z04U+AA=";
	
	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi/0YKKgIEI1yFkmW8SyZWR9NvrpGzlkTq1aLm7wH0Jbi/U/27WWCzpXSMDm+gXwga+m8qlS4ER/ivc+wRGbg45aLaxZaq4ZIJYCZ++XsDiz82YRLcwHcD45gMO//iRuDbwlflA8DuKjClAgjT9tnTH2QMCdXkUIvB0mormMif+lD4YizEnaM3Ht/qtrYKxXVeFSjHqgj9laP3V3FS8Aa5Hl1MXmZWVZFu8ZA7y2sL33em4jw9PbjDeoK4GMCMNsvyTQ0cfTE4xQ3+h/6Ca8cGJdtD+dBIROmuSJK7Ou89CzYdcuwRpvt4X8url6L/8aJhb/yX8nu47+w0PW59nMutQIDAQAB";

	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String notify_url = "http://localhost:8080/api/alipay/notifyUrl";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String return_url = "http://localhost:8080/api/alipay/returnUrl";

	// 签名方式
	public static String sign_type = "RSA2";
	
	// 字符编码格式
	public static String charset = "utf-8";
	
	// 支付宝网关 - 更改
	public static String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
	
	// 支付宝网关
	public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /** 
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

