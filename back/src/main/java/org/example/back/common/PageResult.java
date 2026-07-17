package org.example.back.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records; // 当前页数据
    private Long total;      // 总条数
    private Long current;    // 当前页码
    private Long size;       // 每页条数
    private Long pages;      // 总页数

    // 静态构建分页结果（MyBatis-Plus Page对象转PageResult）
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}