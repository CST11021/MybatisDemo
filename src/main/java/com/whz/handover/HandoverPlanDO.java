package com.whz.handover;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author: wanghz
 * @date: 2021/6/11
 */
public class HandoverPlanDO implements Serializable {

    private static final long serialVersionUID = -4531302941619815108L;

    /** 主键 */
    private Long id;
    /** 创建时间 */
    private Date gmtCreate;
    /** 修改时间 */
    private Date gmtModified;
    /** 网格仓编码 */
    private String whCode;
    /** 站点编码 */
    private String stationCode;
    /** 业务单号（比如：发货单号） */
    private String bizOrderCode;
    /** 运单号 */
    private String loadingNo;
    /** 交接实物id */
    private String entityId;
    /** 交接实物外部条码 */
    private String outCode;
    /** 交接实物编码 */
    private String code;
    /** 交接实物类型 */
    private String type;
    /** 交接实物名 */
    private String name;
    /** 交接实物规格 */
    private String spec;
    /** 交接实物的图片url */
    private String imageUrl;
    /** 计划揽收数量 */
    private Long planPickNumber;
    /** 实际揽收数量 */
    private Long actualPickNumber;
    /** 期望卸货数量(取自对应发货单计划揽收数量) */
    private Long expectDischargeNumber;
    /** 计划卸货数量 */
    private Long planDischargeNumber;
    /** 实际卸货数量 */
    private Long actualDischargeNumber;
    /** 状态 */
    private String status;
    /** 操作时间 */
    private Date operationTime;
    /** 拓展属性 */
    private String attribute;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

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

    public String getBizOrderCode() {
        return bizOrderCode;
    }

    public void setBizOrderCode(String bizOrderCode) {
        this.bizOrderCode = bizOrderCode;
    }

    public String getLoadingNo() {
        return loadingNo;
    }

    public void setLoadingNo(String loadingNo) {
        this.loadingNo = loadingNo;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getPlanPickNumber() {
        return planPickNumber;
    }

    public void setPlanPickNumber(Long planPickNumber) {
        this.planPickNumber = planPickNumber;
    }

    public Long getActualPickNumber() {
        return actualPickNumber;
    }

    public void setActualPickNumber(Long actualPickNumber) {
        this.actualPickNumber = actualPickNumber;
    }

    public Long getExpectDischargeNumber() {
        return expectDischargeNumber;
    }

    public void setExpectDischargeNumber(Long expectDischargeNumber) {
        this.expectDischargeNumber = expectDischargeNumber;
    }

    public Long getPlanDischargeNumber() {
        return planDischargeNumber;
    }

    public void setPlanDischargeNumber(Long planDischargeNumber) {
        this.planDischargeNumber = planDischargeNumber;
    }

    public Long getActualDischargeNumber() {
        return actualDischargeNumber;
    }

    public void setActualDischargeNumber(Long actualDischargeNumber) {
        this.actualDischargeNumber = actualDischargeNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
