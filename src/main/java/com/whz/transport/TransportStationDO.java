package com.whz.transport;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: wanghz
 * @date: 2021/6/16
 */
public class TransportStationDO implements Serializable {

    private static final long serialVersionUID = -8535122942765335432L;

    /** 主键 */
    private long id;
    /** 创建时间 */
    private Date gmtCreate;
    /** 修改时间 */
    private Date gmtModified;
    /** 网格仓 */
    private String whCode;
    /** 装车单号 */
    private String loadingNo;
    /** 顺序 */
    private Integer seq;
    /** 起始站点 */
    private String startStationCode;
    /** 到达站点 */
    private String endStationCode;
    /** 起始站点名 */
    private String startStationName;
    /** 到达站点名 */
    private String endStationName;
    /** 到达经度 */
    private String longitude;
    /** 到达纬度 */
    private String latitude;
    /** 出发经度 */
    private String startoffLongitude;
    /** 出发纬度 */
    private String startoffLatitude;
    /** 到达时间 */
    private Date arrivalTime;
    /** 出发时间 */
    private Date startoffTime;
    /** 计划到达时间 */
    private Date planArrivalTime;
    /** 计划出发时间 */
    private Date planStartoffTime;
    /** 司机APP到达时间 */
    private Date appArrivalTime;
    /** 司机APP出发时间 */
    private Date appStartoffTime;
    /** 手工到达时间 */
    private Date manualArrivalTime;
    /** 手工出发时间 */
    private Date manualStartoffTime;
    /** 停留时长(分钟) */
    private Integer stickDuration;
    /** 到达是否抵达电子围栏 */
    private String arrivalFence;
    /** 出发是否抵达电子围栏 */
    private String startoffArrivalFence;
    /** 标准运行时长 */
    private Integer standardServiceTime;
    /** 业务状态 */
    private String state;
    /** 数据状态 */
    private String status;
    /** 创建者 */
    private String creator;
    /** 修改者 */
    private String modifier;
    /** 备注 */
    private String remark;
    /** 核验状态 */
    private String verificationState;
    /** 交接类型 */
    private String handOverType;
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
    /** 司机联系次数 */
    private Integer connectionTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getLoadingNo() {
        return loadingNo;
    }

    public void setLoadingNo(String loadingNo) {
        this.loadingNo = loadingNo;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getStartStationCode() {
        return startStationCode;
    }

    public void setStartStationCode(String startStationCode) {
        this.startStationCode = startStationCode;
    }

    public String getEndStationCode() {
        return endStationCode;
    }

    public void setEndStationCode(String endStationCode) {
        this.endStationCode = endStationCode;
    }

    public String getStartStationName() {
        return startStationName;
    }

    public void setStartStationName(String startStationName) {
        this.startStationName = startStationName;
    }

    public String getEndStationName() {
        return endStationName;
    }

    public void setEndStationName(String endStationName) {
        this.endStationName = endStationName;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
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

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Date getStartoffTime() {
        return startoffTime;
    }

    public void setStartoffTime(Date startoffTime) {
        this.startoffTime = startoffTime;
    }

    public Date getPlanArrivalTime() {
        return planArrivalTime;
    }

    public void setPlanArrivalTime(Date planArrivalTime) {
        this.planArrivalTime = planArrivalTime;
    }

    public Date getPlanStartoffTime() {
        return planStartoffTime;
    }

    public void setPlanStartoffTime(Date planStartoffTime) {
        this.planStartoffTime = planStartoffTime;
    }

    public Date getAppArrivalTime() {
        return appArrivalTime;
    }

    public void setAppArrivalTime(Date appArrivalTime) {
        this.appArrivalTime = appArrivalTime;
    }

    public Date getAppStartoffTime() {
        return appStartoffTime;
    }

    public void setAppStartoffTime(Date appStartoffTime) {
        this.appStartoffTime = appStartoffTime;
    }

    public Date getManualArrivalTime() {
        return manualArrivalTime;
    }

    public void setManualArrivalTime(Date manualArrivalTime) {
        this.manualArrivalTime = manualArrivalTime;
    }

    public Date getManualStartoffTime() {
        return manualStartoffTime;
    }

    public void setManualStartoffTime(Date manualStartoffTime) {
        this.manualStartoffTime = manualStartoffTime;
    }

    public Integer getStickDuration() {
        return stickDuration;
    }

    public void setStickDuration(Integer stickDuration) {
        this.stickDuration = stickDuration;
    }

    public String getArrivalFence() {
        return arrivalFence;
    }

    public void setArrivalFence(String arrivalFence) {
        this.arrivalFence = arrivalFence;
    }

    public String getStartoffArrivalFence() {
        return startoffArrivalFence;
    }

    public void setStartoffArrivalFence(String startoffArrivalFence) {
        this.startoffArrivalFence = startoffArrivalFence;
    }

    public Integer getStandardServiceTime() {
        return standardServiceTime;
    }

    public void setStandardServiceTime(Integer standardServiceTime) {
        this.standardServiceTime = standardServiceTime;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getVerificationState() {
        return verificationState;
    }

    public void setVerificationState(String verificationState) {
        this.verificationState = verificationState;
    }

    public String getHandOverType() {
        return handOverType;
    }

    public void setHandOverType(String handOverType) {
        this.handOverType = handOverType;
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

    public Integer getConnectionTime() {
        return connectionTime;
    }

    public void setConnectionTime(Integer connectionTime) {
        this.connectionTime = connectionTime;
    }
}
