package org.seamless.util;

import java.io.Serializable;
/* loaded from: classes.dex */
public class Pager implements Serializable {
    private Long numOfRecords;
    private Integer page;
    private Long pageSize;

    public Pager() {
        this.numOfRecords = 0L;
        this.page = 1;
        this.pageSize = 15L;
    }

    public Pager(Long numOfRecords) {
        this.numOfRecords = 0L;
        this.page = 1;
        this.pageSize = 15L;
        this.numOfRecords = numOfRecords;
    }

    public Pager(Long numOfRecords, Integer page) {
        this.numOfRecords = 0L;
        this.page = 1;
        this.pageSize = 15L;
        this.numOfRecords = numOfRecords;
        this.page = page;
    }

    public Pager(Long numOfRecords, Integer page, Long pageSize) {
        this.numOfRecords = 0L;
        this.page = 1;
        this.pageSize = 15L;
        this.numOfRecords = numOfRecords;
        this.page = page;
        this.pageSize = pageSize;
    }

    public Long getNumOfRecords() {
        return this.numOfRecords;
    }

    public void setNumOfRecords(Long numOfRecords) {
        this.numOfRecords = numOfRecords;
    }

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        if (page != null) {
            this.page = page;
        }
    }

    public Long getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Long pageSize) {
        if (pageSize != null) {
            this.pageSize = pageSize;
        }
    }

    public int getNextPage() {
        return this.page.intValue() + 1;
    }

    public int getPreviousPage() {
        return this.page.intValue() - 1;
    }

    public int getFirstPage() {
        return 1;
    }

    public long getIndexRangeBegin() {
        long retval = (getPage().intValue() - 1) * getPageSize().longValue();
        return Math.max(Math.min(getNumOfRecords().longValue() - 1, retval >= 0 ? retval : 0L), 0L);
    }

    public long getIndexRangeEnd() {
        long firstIndex = getIndexRangeBegin();
        long pageIndex = getPageSize().longValue() - 1;
        long lastIndex = getNumOfRecords().longValue() - 1;
        return Math.min(firstIndex + pageIndex, lastIndex);
    }

    public long getLastPage() {
        long lastPage = this.numOfRecords.longValue() / this.pageSize.longValue();
        if (this.numOfRecords.longValue() % this.pageSize.longValue() == 0) {
            lastPage--;
        }
        return 1 + lastPage;
    }

    public boolean isPreviousPageAvailable() {
        return getIndexRangeBegin() + 1 > getPageSize().longValue();
    }

    public boolean isNextPageAvailable() {
        return this.numOfRecords.longValue() - 1 > getIndexRangeEnd();
    }

    public boolean isSeveralPages() {
        return getNumOfRecords().longValue() != 0 && getNumOfRecords().longValue() > getPageSize().longValue();
    }

    public String toString() {
        return "Pager - Records: " + getNumOfRecords() + " Page size: " + getPageSize();
    }
}
