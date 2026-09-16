package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Author;
import org.example.vo.AuthorApplyVO;

@Mapper
public interface AuthorMapper extends BaseMapper<Author> {

    /**
     * 分页查询作者入驻申请（联 user 表）
     */
    @Select("""
            <script>
            SELECT a.*, u.username, u.nickname
            FROM author a
            LEFT JOIN `user` u ON a.user_id = u.id
            WHERE 1=1
            <if test="auditStatus != null"> AND a.audit_status = #{auditStatus}</if>
            ORDER BY a.create_time DESC
            </script>
            """)
    IPage<AuthorApplyVO> selectApplyPage(IPage<AuthorApplyVO> page,
                                         @Param("auditStatus") Integer auditStatus);
}
