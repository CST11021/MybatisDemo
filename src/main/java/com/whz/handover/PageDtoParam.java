package com.whz.handover;

import java.io.Serializable;
import java.util.Map;

/**
 * 分页参数
 *
 * @author wb-cjb381966
 */
public class PageDtoParam implements Serializable, PageParamReader {
    private static final long serialVersionUID = 1L;


    private Boolean isPagination;

    /**
     * 开始行号
     */
    public static final String PAGE_BEGIN = "pageBegin";

    /**
     * 页码大小
     */
    public static final String PAGE_SIZE = "pageSize";



    /**
     * 当前页号
     */
    private Integer pageIndex;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 分页起始下标
     */
    private Integer pageBegin;

    /**
     * 把分页参数添加到条件对象
     *
     * @param criteriaMap
     */
    public void addLimitToMap(Map<String, Object> criteriaMap) {
        criteriaMap.put(PageDtoParam.PAGE_BEGIN, this.getPageBegin());
        criteriaMap.put(PageDtoParam.PAGE_SIZE, this.getPageSize());
    }

    @Override
    public Integer getPageBegin() {
        Integer pageBegin = 0;
        if (pageIndex != null && pageIndex > 0 && pageSize != null && pageSize > 0) {
            pageBegin = (pageIndex - 1) * pageSize;
        }
        return pageBegin;
    }

    @Override
    public Integer getPageIndex() {
        if (pageIndex == null || pageIndex <= 0) {
            pageIndex = 1;
        }
        return pageIndex;
    }

    @Override
    public Integer getPageSize() {
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }
        return pageSize;
    }

    public Boolean getPagination() {
        return isPagination;
    }

    public void setPagination(Boolean pagination) {
        isPagination = pagination;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

}
