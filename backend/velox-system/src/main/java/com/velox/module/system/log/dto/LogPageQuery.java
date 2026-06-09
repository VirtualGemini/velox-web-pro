package com.velox.module.system.log.dto;

public class LogPageQuery {

    private Long current;
    private Long size;
    private Long pageNo;
    private Long pageSize;

    public long page() {
        Long value = current != null ? current : pageNo;
        return value != null && value > 0 ? value : 1;
    }

    public long size() {
        Long value = size != null ? size : pageSize;
        return value != null && value > 0 ? Math.min(value, 200) : 10;
    }

    public Long getCurrent() { return current; }
    public void setCurrent(Long current) { this.current = current; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Long getPageNo() { return pageNo; }
    public void setPageNo(Long pageNo) { this.pageNo = pageNo; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
}
