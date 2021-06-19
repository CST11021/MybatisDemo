package com.whz.mapperinterface;

import com.whz.transport.TransportStationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: wanghz
 * @date: 2021/6/16
 */
public interface TransportStationMapper {

    /**
     * 批量添加
     *
     * @param doList
     * @return
     */
    Integer batchInsert(@Param("doList") List<TransportStationDO> doList);

    /**
     * 更新联系次数
     *
     * @param loadingNo
     * @param endStationCode
     * @param connectionTime
     * @return
     */
    Integer updateConnectionTime(@Param("whCode") String whCode, @Param("loadingNo") String loadingNo, @Param("endStationCode") String endStationCode, @Param("connectionTime") Integer connectionTime);

    /**
     * 更新配送站状态
     *
     * @param loadingNo
     * @param endStationCode
     * @param state
     * @return
     */
    Integer updateState(@Param("whCode") String whCode, @Param("loadingNo") String loadingNo, @Param("endStationCode") String endStationCode, @Param("state") String state);

    /**
     * 更新站点的核验状态
     *
     * @param whCode
     * @param loadingNo
     * @param endStationCode
     * @param verificationState
     * @return
     */
    Integer updateVerificationState(@Param("whCode") String whCode, @Param("loadingNo") String loadingNo, @Param("endStationCode") String endStationCode, @Param("verificationState") String verificationState);

    /**
     * 更新停靠点状态
     *
     * @param updateDO
     * @return
     */
    Integer updateByLoadingNoAndStationCode(TransportStationDO updateDO);

    /**
     * 根据网格仓和运单号查询
     *
     * @param whCode
     * @param loadingNo
     * @return
     */
    List<TransportStationDO> getListByLoadingNo(@Param("whCode") String whCode, @Param("loadingNo") String loadingNo);

    /**
     * 查询站点
     *
     * @param whCode
     * @param loadingNo
     * @param endStationCode
     * @return
     */
    TransportStationDO getTransportStation(@Param("whCode") String whCode, @Param("loadingNo") String loadingNo, @Param("endStationCode") String endStationCode);
}
