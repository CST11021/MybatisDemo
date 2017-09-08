package com.whz.entity;

import java.io.Serializable;

public class PageResult<T> extends BaseResult<T> implements Serializable {

    public static final int MAX_PAGE_SIZE = 200;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_PAGE_INDEX = 1;

    // 分页起始页
    private int pageIndex = DEFAULT_PAGE_INDEX;
    // 每页记录条数
    private int pageSize = DEFAULT_PAGE_SIZE;
    // 总记录数
    private int totalCount = 0;
    // 是否升序
    private boolean asc;
    // 排序字段
    private String orderBy;

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    public boolean isAsc() {
        return asc;
    }
    public void setAsc(boolean asc) {
        this.asc = asc;
    }
    public String getOrderBy() {
        return orderBy;
    }
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
    public PageResult() {
        super();
    }
    public PageResult(int pageSize, int pageIndex, int totalCount, boolean asc, String orderBy) {
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
        this.totalCount = totalCount;
        this.asc = asc;
        this.orderBy = orderBy;
    }
    public PageResult(int pageSize, int pageIndex, int totalCount) {
        this(pageSize, pageIndex, totalCount, true, "");
    }
    public void setPage(int pageSize, int PageIndex, int totalCount) {
        setPage(pageSize, pageIndex, totalCount, false, "");
    }
    public void setPage(PageResult page) {
        setPage(page.getPageSize(), page.getPageIndex(), page.getTotalCount(), page.isAsc(), page.orderBy);
    }
    public void setPage(int pageSize, int pageIndex, int totalCount, boolean asc, String orderBy) {
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
        this.totalCount = totalCount;
        this.asc = asc;
        this.orderBy = orderBy;
    }
    public int getTotalPage() {
        return (totalCount + pageSize - 1) / pageSize;
    }
    public int getStartRow() {
        return Math.max(pageIndex - 1, 0) * pageSize;
    }
    public int getEndRow() {
        return pageIndex * pageSize;
    }
    public int getPageIndex() {
        return Math.max(0, pageIndex);
    }
    public int getPageSize() {
        if (pageSize >= 1 && pageSize <= MAX_PAGE_SIZE) {
            return pageSize;
        }
        return DEFAULT_PAGE_INDEX;
    }
    public int getTotalCount() {
        return Math.max(0, totalCount);
    }
    @Override
    public String toString() {
        return "PageResult{" +
            "pageIndex=" + pageIndex +
            ", pageSize=" + pageSize +
            ", totalCount=" + totalCount +
            ", asc=" + asc +
            ", orderBy='" + orderBy + '\'' +
            '}';
    }
}