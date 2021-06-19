package com.whz.handover;


import java.util.List;

/**
 * @author: wanghz
 * @date: 2021/6/11
 */
public class HandoverPlanQueryParam extends PageDtoParam {

    /** 网格仓编码 */
    private String whCode;
    /** 站点编码 */
    private String stationCode;
    /** 站点编码 */
    private List<String> stationCodes;
    /** 发货单号 */
    private String deliveryOrderCode;
    /** 运单号 */
    private String loadingNo;
    /** 交接实物外部条码 */
    private String outCode;
    /** 交接实物编码 */
    private String code;
    /** 交接实物名（模糊查） */
    private String nameLike;

    public String getWhCode() {
        return whCode;
    }

    public void setWhCode(String whCode) {
        this.whCode = whCode;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public List<String> getStationCodes() {
        return stationCodes;
    }

    public void setStationCodes(List<String> stationCodes) {
        this.stationCodes = stationCodes;
    }

    public String getDeliveryOrderCode() {
        return deliveryOrderCode;
    }

    public void setDeliveryOrderCode(String deliveryOrderCode) {
        this.deliveryOrderCode = deliveryOrderCode;
    }

    public String getLoadingNo() {
        return loadingNo;
    }

    public void setLoadingNo(String loadingNo) {
        this.loadingNo = loadingNo;
    }

    public String getOutCode() {
        return outCode;
    }

    public void setOutCode(String outCode) {
        this.outCode = outCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameLike() {
        return nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }
}
