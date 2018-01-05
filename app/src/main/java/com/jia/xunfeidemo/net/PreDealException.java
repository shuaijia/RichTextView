package com.jia.xunfeidemo.net;

/**
 * Describtion: 自定义预处理异常
 * Created by jia on 2017/6/7.
 * 人之所以能，是相信能
 */
public class PreDealException extends RuntimeException {

    private String errorMsg;

    public PreDealException(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String getMessage() {
        return this.errorMsg;
    }
}
