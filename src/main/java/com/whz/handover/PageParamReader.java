package com.whz.handover;

/**
 * @author liyueqian.lyq
 * @date 2021/3/17
 */
public interface PageParamReader {
    /**
     * 分页起始下标
     */
    Integer getPageBegin();

    /**
     * 当前页号
     */
    Integer getPageIndex();

    /**
     * 分页大小
     */
    Integer getPageSize();
}
