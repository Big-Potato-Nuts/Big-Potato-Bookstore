package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Comment;
import org.example.vo.CommentVO;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询评论（联用户、书）
     */
    @Select("""
            <script>
            SELECT cm.*, u.nickname, u.avatar, b.title AS book_title
            FROM comment cm
            LEFT JOIN `user` u ON cm.user_id = u.id
            LEFT JOIN book b ON cm.book_id = b.id
            WHERE 1=1
            <if test="bookId != null"> AND cm.book_id = #{bookId}</if>
            <if test="userId != null"> AND cm.user_id = #{userId}</if>
            <if test="status != null"> AND cm.status = #{status}</if>
            <if test="keyword != null and keyword != ''">
                AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                     OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY cm.id DESC
            </script>
            """)
    IPage<CommentVO> selectCommentPage(IPage<CommentVO> page,
                                       @Param("bookId") Long bookId,
                                       @Param("userId") Long userId,
                                       @Param("keyword") String keyword,
                                       @Param("status") Integer status);
}
