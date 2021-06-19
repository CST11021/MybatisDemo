package com.whz.mapperinterface;

import com.whz.loading.LoadingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: wanghz
 * @date: 2021/6/19
 */
public interface LoadingMapper {

    /**
     * 根据条件count
     *
     * @param param
     * @return
     */
    Integer count(Map<String, Object> param);

    /**
     * 根据条件查询
     *
     * @param param
     * @return
     */
    List<LoadingDO> select(Map<String, Object> param);

    /**
     * 插入
     *
     * @param loadingDO
     * @return
     */
    Integer insert(LoadingDO loadingDO);

    /**
     * 根据商家code、运单号和版本更新
     *
     * @param loadingDO
     * @return
     */
    Integer updateByLoadingNoAndVersion(LoadingDO loadingDO);

}
