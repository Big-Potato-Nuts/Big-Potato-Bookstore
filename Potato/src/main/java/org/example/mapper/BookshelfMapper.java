package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Bookshelf;
import org.example.vo.BookshelfVO;

import java.util.List;

@Mapper
public interface BookshelfMapper extends BaseMapper<Bookshelf> {

    /**
     * 查询用户书架（联小说、作者、章节）
     */
    @Select("""
            SELECT sh.*,
                   b.title AS book_title,
                   b.cover,
                   b.status AS book_status,
                   u.nickname AS author_name,
                   ch.title AS read_chapter_title
            FROM bookshelf sh
            LEFT JOIN book b ON sh.book_id = b.id
            LEFT JOIN author a ON b.author_id = a.id
            LEFT JOIN `user` u ON a.user_id = u.id
            LEFT JOIN chapter ch ON sh.read_chapter_id = ch.id
            WHERE sh.user_id = #{userId}
            ORDER BY sh.update_time DESC
            """)
    List<BookshelfVO> selectShelfList(@Param("userId") Long userId);
}
