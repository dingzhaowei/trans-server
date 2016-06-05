package com.ding.trans.server.model;

import java.util.List;

public class TransOrderColl {

    private int pageIndex;

    private int pageSize;

    private int totalCount;

    private int totalPage;

    private List<TransOrder> orders;

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<TransOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<TransOrder> orders) {
        this.orders = orders;
    }

}
