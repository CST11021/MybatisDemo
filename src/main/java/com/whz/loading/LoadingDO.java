package com.whz.loading;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wanghz
 * @date: 2021/6/19
 */
public class LoadingDO {

    /** 主键 */
    private Long id;
    /** 商家编码 */
    private String merchantCode;
    /** 装车单号 */
    private String loadingNo;
    /** 发货方编码 */
    private String senderStationCode;
    /** 运输商编码 */
    private String transCode;
    /** 车辆编码 */
    private String carCode;
    /** 司机编码 */
    private String driverCode;
    /** 线路编码 */
    private String lineCode;
    /** 调度员编码 */
    private String dispatcherCode;
    /** 计划到仓时间 */
    private Date planArrivalTime;
    /** 要求到店时间 */
    private Date deadlineTime;
    /** 发车时间 */
    private Date startoffTime;
    /** 最终到店时间 */
    private Date arrivalTime;
    /** 业务类型 */
    private String temperatureType;
    /** 业务状态 */
    private String state;
    /** 数据状态 */
    private String status;
    /** 关闭时间 */
    private Date closeTime;
    /** 创建时间 */
    private Date gmtCreate;
    /** 修改时间 */
    private Date gmtModified;
    /** 备注 */
    private String remark;
    /** 实际到仓时间 */
    private Date factArrivalTime;
    /** 数据生成模式 */
    private String generateMode;
    /** 班次编码 */
    private String shiftCode;
    /** 创建者 */
    private String creator;
    /** 修改者 */
    private String modifier;
    /** 计划发车时间 */
    private Date planStartoffTime;
    /** 运单完成时间 */
    private Date loadingEndTime;
    /** 托盘个数 */
    private Integer palletNumber;
    /** 笼车个数 */
    private Integer rollNumber;
    /** 装载率 */
    private BigDecimal payloadRatio;
    /** 到达仓经度 */
    private String arrivalWsLongitude;
    /** 到达仓纬度 */
    private String arrivalWsLatitude;
    /** 发车经度 */
    private String startoffLongitude;
    /** 发车纬度 */
    private String startoffLatitude;
    /** 班次周期时间 */
    private Date shiftPeriodTime;
    /** 发布状态 */
    private String publishState;
    /** 考勤状态 */
    private String attendanceState;
    /** 计费状态 */
    private String billingState;
    /** 计划车型编码 */
    private String carModelCode;
    /** 运输线路名称 */
    private String lineName;
    /** 司机电话 */
    private String driverPhone;
    /** 运输计划类型 */
    private String transPlanType;
    /** 运输名称 */
    private String loadingName;
    /** 目的站点 */
    private String endStationCode;
    /** 站点数 */
    private Integer stationNum;
    /** 进度百分比 */
    private BigDecimal progressPercent;
    /** 路由校验结果信息 */
    private String itineraryVerifyInfo;
    /** 审核状态，0：免审核 1：待审核 2：审核通过 3：审核不通过 */
    private String auditState;
    /** 报价方式，0：固定合同 1：临时报价 */
    private String quoteType;
    /** 临时报价金额 */
    private BigDecimal temporaryQuoteAmt;
    /** 业务性质编码 */
    private String bizNatureCode;
    /** 固定合同编号 */
    private String contractNo;
    /** 固定合同版本号 */
    private String contractVersion;
    /** 收货状态 */
    private String receiveState;
    /** 收货完成时间 */
    private Date receiveTime;
    /** 运输商名称 */
    private String transName;
    /** 运输商简称 */
    private String transShortName;
    /** 实际车型编码 */
    private String factCarModelCode;
    /** 实际车型名称 */
    private String factCarModelName;
    /** 车牌号 */
    private String plateNumber;
    /** 司机姓名 */
    private String driverName;
    /** 标准封签数 */
    private Integer standardSealNum;
    /** 运单状态(新), 状态合一 */
    private int loadingState;
    /** 运单状态备注 */
    private String stateRemark;
    /** 变更版本号 */
    private int version;
    /** 计划车型名 */
    private String planCarModelName;
    /** 计划车型温层 */
    private String planTemperatureType;
    /** 计划车型长度 */
    private BigDecimal planLength;
    /** 计划车型宽度 */
    private BigDecimal planWidth;
    /** 计划车型高度 */
    private BigDecimal planHeight;
    /** 实际车型温层 */
    private String factTemperatureType;
    /** 实际车型长度 */
    private BigDecimal factLength;
    /** 实际车型宽度 */
    private BigDecimal factWidth;
    /** 实际车型高度 */
    private BigDecimal factHeight;
    /** 外部单号 */
    private String externalOrderCode;
    /** 外部系统 */
    private String externalSys;
    /** 运输方式 */
    private String transportType;
    /** 动态字段 */
    private String dynamicFields;
    /** 创单来源 */
    private String createFrom;
    /** 提货状态 */
    private String deliveryState;
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
    /** 关联单据数量 */
    private Integer orderNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getLoadingNo() {
        return loadingNo;
    }

    public void setLoadingNo(String loadingNo) {
        this.loadingNo = loadingNo;
    }

    public String getSenderStationCode() {
        return senderStationCode;
    }

    public void setSenderStationCode(String senderStationCode) {
        this.senderStationCode = senderStationCode;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getCarCode() {
        return carCode;
    }

    public void setCarCode(String carCode) {
        this.carCode = carCode;
    }

    public String getDriverCode() {
        return driverCode;
    }

    public void setDriverCode(String driverCode) {
        this.driverCode = driverCode;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getDispatcherCode() {
        return dispatcherCode;
    }

    public void setDispatcherCode(String dispatcherCode) {
        this.dispatcherCode = dispatcherCode;
    }

    public Date getPlanArrivalTime() {
        return planArrivalTime;
    }

    public void setPlanArrivalTime(Date planArrivalTime) {
        this.planArrivalTime = planArrivalTime;
    }

    public Date getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(Date deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public Date getStartoffTime() {
        return startoffTime;
    }

    public void setStartoffTime(Date startoffTime) {
        this.startoffTime = startoffTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getTemperatureType() {
        return temperatureType;
    }

    public void setTemperatureType(String temperatureType) {
        this.temperatureType = temperatureType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getFactArrivalTime() {
        return factArrivalTime;
    }

    public void setFactArrivalTime(Date factArrivalTime) {
        this.factArrivalTime = factArrivalTime;
    }

    public String getGenerateMode() {
        return generateMode;
    }

    public void setGenerateMode(String generateMode) {
        this.generateMode = generateMode;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Date getPlanStartoffTime() {
        return planStartoffTime;
    }

    public void setPlanStartoffTime(Date planStartoffTime) {
        this.planStartoffTime = planStartoffTime;
    }

    public Date getLoadingEndTime() {
        return loadingEndTime;
    }

    public void setLoadingEndTime(Date loadingEndTime) {
        this.loadingEndTime = loadingEndTime;
    }

    public Integer getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(Integer palletNumber) {
        this.palletNumber = palletNumber;
    }

    public Integer getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(Integer rollNumber) {
        this.rollNumber = rollNumber;
    }

    public BigDecimal getPayloadRatio() {
        return payloadRatio;
    }

    public void setPayloadRatio(BigDecimal payloadRatio) {
        this.payloadRatio = payloadRatio;
    }

    public String getArrivalWsLongitude() {
        return arrivalWsLongitude;
    }

    public void setArrivalWsLongitude(String arrivalWsLongitude) {
        this.arrivalWsLongitude = arrivalWsLongitude;
    }

    public String getArrivalWsLatitude() {
        return arrivalWsLatitude;
    }

    public void setArrivalWsLatitude(String arrivalWsLatitude) {
        this.arrivalWsLatitude = arrivalWsLatitude;
    }

    public String getStartoffLongitude() {
        return startoffLongitude;
    }

    public void setStartoffLongitude(String startoffLongitude) {
        this.startoffLongitude = startoffLongitude;
    }

    public String getStartoffLatitude() {
        return startoffLatitude;
    }

    public void setStartoffLatitude(String startoffLatitude) {
        this.startoffLatitude = startoffLatitude;
    }

    public Date getShiftPeriodTime() {
        return shiftPeriodTime;
    }

    public void setShiftPeriodTime(Date shiftPeriodTime) {
        this.shiftPeriodTime = shiftPeriodTime;
    }

    public String getPublishState() {
        return publishState;
    }

    public void setPublishState(String publishState) {
        this.publishState = publishState;
    }

    public String getAttendanceState() {
        return attendanceState;
    }

    public void setAttendanceState(String attendanceState) {
        this.attendanceState = attendanceState;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getCarModelCode() {
        return carModelCode;
    }

    public void setCarModelCode(String carModelCode) {
        this.carModelCode = carModelCode;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getTransPlanType() {
        return transPlanType;
    }

    public void setTransPlanType(String transPlanType) {
        this.transPlanType = transPlanType;
    }

    public String getLoadingName() {
        return loadingName;
    }

    public void setLoadingName(String loadingName) {
        this.loadingName = loadingName;
    }

    public String getEndStationCode() {
        return endStationCode;
    }

    public void setEndStationCode(String endStationCode) {
        this.endStationCode = endStationCode;
    }

    public Integer getStationNum() {
        return stationNum;
    }

    public void setStationNum(Integer stationNum) {
        this.stationNum = stationNum;
    }

    public BigDecimal getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(BigDecimal progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getItineraryVerifyInfo() {
        return itineraryVerifyInfo;
    }

    public void setItineraryVerifyInfo(String itineraryVerifyInfo) {
        this.itineraryVerifyInfo = itineraryVerifyInfo;
    }

    public String getAuditState() {
        return auditState;
    }

    public void setAuditState(String auditState) {
        this.auditState = auditState;
    }

    public String getQuoteType() {
        return quoteType;
    }

    public void setQuoteType(String quoteType) {
        this.quoteType = quoteType;
    }

    public BigDecimal getTemporaryQuoteAmt() {
        return temporaryQuoteAmt;
    }

    public void setTemporaryQuoteAmt(BigDecimal temporaryQuoteAmt) {
        this.temporaryQuoteAmt = temporaryQuoteAmt;
    }

    public String getBizNatureCode() {
        return bizNatureCode;
    }

    public void setBizNatureCode(String bizNatureCode) {
        this.bizNatureCode = bizNatureCode;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(String contractVersion) {
        this.contractVersion = contractVersion;
    }

    public String getReceiveState() {
        return receiveState;
    }

    public void setReceiveState(String receiveState) {
        this.receiveState = receiveState;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public String getTransShortName() {
        return transShortName;
    }

    public void setTransShortName(String transShortName) {
        this.transShortName = transShortName;
    }

    public String getFactCarModelCode() {
        return factCarModelCode;
    }

    public void setFactCarModelCode(String factCarModelCode) {
        this.factCarModelCode = factCarModelCode;
    }

    public String getFactCarModelName() {
        return factCarModelName;
    }

    public void setFactCarModelName(String factCarModelName) {
        this.factCarModelName = factCarModelName;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Integer getStandardSealNum() {
        return standardSealNum;
    }

    public void setStandardSealNum(Integer standardSealNum) {
        this.standardSealNum = standardSealNum;
    }

    public int getLoadingState() {
        return loadingState;
    }

    public void setLoadingState(int loadingState) {
        this.loadingState = loadingState;
    }

    public String getStateRemark() {
        return stateRemark;
    }

    public void setStateRemark(String stateRemark) {
        this.stateRemark = stateRemark;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPlanCarModelName() {
        return planCarModelName;
    }

    public void setPlanCarModelName(String planCarModelName) {
        this.planCarModelName = planCarModelName;
    }

    public String getPlanTemperatureType() {
        return planTemperatureType;
    }

    public void setPlanTemperatureType(String planTemperatureType) {
        this.planTemperatureType = planTemperatureType;
    }

    public BigDecimal getPlanLength() {
        return planLength;
    }

    public void setPlanLength(BigDecimal planLength) {
        this.planLength = planLength;
    }

    public BigDecimal getPlanWidth() {
        return planWidth;
    }

    public void setPlanWidth(BigDecimal planWidth) {
        this.planWidth = planWidth;
    }

    public BigDecimal getPlanHeight() {
        return planHeight;
    }

    public void setPlanHeight(BigDecimal planHeight) {
        this.planHeight = planHeight;
    }

    public String getFactTemperatureType() {
        return factTemperatureType;
    }

    public void setFactTemperatureType(String factTemperatureType) {
        this.factTemperatureType = factTemperatureType;
    }

    public BigDecimal getFactLength() {
        return factLength;
    }

    public void setFactLength(BigDecimal factLength) {
        this.factLength = factLength;
    }

    public BigDecimal getFactWidth() {
        return factWidth;
    }

    public void setFactWidth(BigDecimal factWidth) {
        this.factWidth = factWidth;
    }

    public BigDecimal getFactHeight() {
        return factHeight;
    }

    public void setFactHeight(BigDecimal factHeight) {
        this.factHeight = factHeight;
    }

    public String getExternalOrderCode() {
        return externalOrderCode;
    }

    public void setExternalOrderCode(String externalOrderCode) {
        this.externalOrderCode = externalOrderCode;
    }

    public String getExternalSys() {
        return externalSys;
    }

    public void setExternalSys(String externalSys) {
        this.externalSys = externalSys;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(String dynamicFields) {
        this.dynamicFields = dynamicFields;
    }

    public String getCreateFrom() {
        return createFrom;
    }

    public void setCreateFrom(String createFrom) {
        this.createFrom = createFrom;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(String deliveryState) {
        this.deliveryState = deliveryState;
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

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
