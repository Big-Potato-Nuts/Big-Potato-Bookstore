package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Chapter;

@Mapper
public interface ChapterMapper extends BaseMapper<Chapter> {

    /** 查询某书当前最大排序号，无章节返回 0 */
    @Select("SELECT COALESCE(MAX(sort), 0) FROM chapter WHERE book_id = #{bookId}")
    Integer selectMaxSort(@Param("bookId") Long bookId);

    /** 查询某书付费章节总价（单位：分） */
    @Select("SELECT COALESCE(SUM(price), 0) FROM chapter WHERE book_id = #{bookId} AND is_free = 0")
    Integer selectPaidAmountByBook(@Param("bookId") Long bookId);
}
