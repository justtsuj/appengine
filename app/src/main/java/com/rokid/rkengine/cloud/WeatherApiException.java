package com.rokid.rkengine.cloud;

/**
 * 和风天气API异常类
 * 用于表示与和风天气API交互过程中发生的错误
 */
public class WeatherApiException extends RuntimeException {
    private final String errorCode;
    private final String errorMsg;

    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误代码
     * @param errorMsg 错误描述
     */
    public WeatherApiException(String message, String errorCode, String errorMsg) {
        super(message);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 构造函数
     * @param message 异常消息
     * @param cause 根本原因
     * @param errorCode 错误代码
     * @param errorMsg 错误描述
     */
    public WeatherApiException(String message, Throwable cause, String errorCode, String errorMsg) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 获取错误代码
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误描述
     * @return 错误描述
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return "WeatherApiException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}