package com.whz.mapperinterface;

import com.whz.handover.CountPlanDischargeNumberDO;
import com.whz.handover.HandoverPlanDO;
import com.whz.handover.HandoverPlanQueryParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author: wanghz
 * @date: 2021/6/11
 */
public interface HandoverPlanMapper {
    /**
     * 根据条件查询，不分页
     *
     * @param param
     * @return
     */
    List<HandoverPlanDO> list(Map<String, Object> param);

    /**
     * 批量添加
     *
     * @param doList
     * @return
     */
    Integer batchInsert(@Param("doList") List<HandoverPlanDO> doList);

    /**
     * 更新实物的签收数量
     *
     * @param whCode                网格仓
     * @param bizOrderCode          发货单
     * @param code                  商品code
     * @param actualDischargeNumber 实际卸货数量
     * @return
     */
    Integer updateSignQuantityByCode(@Param("whCode") String whCode, @Param("bizOrderCode") String bizOrderCode, @Param("code") String code, @Param("actualDischargeNumber") Long actualDischargeNumber);

    /**
     * 删除
     *
     * @param whCode
     * @param stationCode
     * @param bizOrderCode
     * @return
     */
    Integer deleteByBizOrderCode(@Param("whCode") String whCode, @Param("stationCode") String stationCode, @Param("bizOrderCode") String bizOrderCode);

    /**
     * 统计每个站点的应交付数量
     *
     * @param whCode
     * @param loadingNo
     * @param stationCodes
     * @return
     */
    List<CountPlanDischargeNumberDO> countPlanDischargeNumberGroupStation(@Param("whCode") String whCode, @Param("loadingNo") String loadingNo, @Param("stationCodes") List<String> stationCodes);

}
