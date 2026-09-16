package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Book;
import org.example.vo.BookVO;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 分页查询小说（联分类、作者、用户，含章节数与最新章节标题）
     *
     * @param page          分页对象
     * @param categoryId    分类筛选（可空）
     * @param keyword       书名/作者名模糊搜索（可空）
     * @param status        状态精确筛选（可空）
     * @param orderBy       click-人气 / collect-收藏 / update-最近更新
     * @param bookId        按小说 id 精确查询（可空）
     * @param isRecommend   推荐位筛选（可空）
     * @param visibleOnly   仅展示上架作品（status != 0），读者端传 true
     */
    @Select("""
            <script>
            SELECT b.*,
                   c.name AS category_name,
                   u.nickname AS author_name,
                   a.user_id AS author_user_id,
                   (SELECT COUNT(*) FROM chapter ch WHERE ch.book_id = b.id) AS chapter_count,
                   (SELECT ch.title FROM chapter ch WHERE ch.book_id = b.id
                    ORDER BY ch.sort DESC, ch.id DESC LIMIT 1) AS latest_chapter_title
            FROM book b
            LEFT JOIN category c ON b.category_id = c.id
            LEFT JOIN author a ON b.author_id = a.id
            LEFT JOIN `user` u ON a.user_id = u.id
            WHERE 1=1
            <if test="bookId != null"> AND b.id = #{bookId}</if>
            <if test="status != null"> AND b.status = #{status}</if>
            <if test="visibleOnly != null and visibleOnly"> AND b.status IN (1, 2)</if>
            <if test="categoryId != null"> AND b.category_id = #{categoryId}</if>
            <if test="isRecommend != null"> AND b.is_recommend = #{isRecommend}</if>
            <if test="keyword != null and keyword != ''">
                AND (b.title LIKE CONCAT('%', #{keyword}, '%')
                     OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <choose>
                <when test="orderBy == 'click'"> ORDER BY b.click_count DESC, b.id DESC</when>
                <when test="orderBy == 'collect'"> ORDER BY b.collect_count DESC, b.id DESC</when>
                <when test="orderBy == 'update'"> ORDER BY b.update_time DESC, b.id DESC</when>
                <otherwise> ORDER BY b.update_time DESC, b.id DESC</otherwise>
            </choose>
            </script>
            """)
    IPage<BookVO> selectBookPage(IPage<BookVO> page,
                                 @Param("categoryId") Long categoryId,
                                 @Param("keyword") String keyword,
                                 @Param("status") Integer status,
                                 @Param("orderBy") String orderBy,
                                 @Param("bookId") Long bookId,
                                 @Param("isRecommend") Integer isRecommend,
                                 @Param("visibleOnly") Boolean visibleOnly);
}
