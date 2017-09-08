package com.whz.entity;

import java.io.Serializable;

public class BaseResult<T> implements Serializable {

    // 错误信息
    private String errorMsg;
    // 错误代码
    private String errorCode;
    // 是否成功
    private boolean success = true;
    // 返回结果
    private T result;

    // 默认返回成功
    public BaseResult() {
        this.success = true;
    }
    public BaseResult(boolean success) {
        this.success = success;
    }
    public BaseResult(String errorCode) {
        this.success = false;
        this.errorCode = errorCode;
    }
    public BaseResult(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.success = false;
    }


    public String getErrorMsg() {
        return errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public T getResult() {
        return result;
    }
    public void setResult(T result) {
        this.result = result;
    }
    public BaseResult<T> success(T result){
        this.result = result;
        this.success = true;
        return this;
    }
    public BaseResult<T> fail(String errorCode, String errorMsg) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        return this;
    }
    public BaseResult<T> fail(String errorMsg) {
        this.success = false;
        //this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        return this;
    }
    public static<T> BaseResult<T> makeSuccess(T data){
        BaseResult<T> result = new BaseResult<>();
        result.success(data);
        return result;
    }
    public static BaseResult<Void> makeSuccess() {
        BaseResult<Void> result = new BaseResult<>();
        result.setSuccess(true);
        return result;
    }
    public static<T> BaseResult<T> makeFail(String errorCode){
        BaseResult<T> result = new BaseResult<T>();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        return result;
    }
    public static <T> BaseResult<T> makeFail(String errorCode, String  errorMsg){
        BaseResult<T> result = new BaseResult<>();
        result.fail(errorCode, errorMsg);
        return result;
    }
    @Override
    public String toString() {
        return "BaseResult{" +
            "errorMsg='" + errorMsg + '\'' +
            ", errorCode='" + errorCode + '\'' +
            ", success=" + success +
            ", result=" + result +
            '}';
    }

}