package com.reciprocityledger.backend.common.util;

public final class PageUtils {

    private PageUtils() {
    }

    public static int safePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    public static int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }

    public static int offset(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
}
