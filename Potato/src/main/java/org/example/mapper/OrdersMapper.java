package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Orders;
import org.example.vo.OrderVO;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 分页查询订单（联书、章节、用户）
     */
    @Select("""
            <script>
            SELECT o.*,
                   b.title AS book_title,
                   ch.title AS chapter_title,
                   u.username,
                   u.nickname
            FROM orders o
            LEFT JOIN book b ON o.book_id = b.id
            LEFT JOIN chapter ch ON o.chapter_id = ch.id
            LEFT JOIN `user` u ON o.user_id = u.id
            WHERE 1=1
            <if test="orderNo != null and orderNo != ''"> AND o.order_no = #{orderNo}</if>
            <if test="userId != null"> AND o.user_id = #{userId}</if>
            <if test="username != null and username != ''"> AND u.username LIKE CONCAT('%', #{username}, '%')</if>
            <if test="payStatus != null"> AND o.pay_status = #{payStatus}</if>
            <if test="type != null"> AND o.type = #{type}</if>
            ORDER BY o.id DESC
            </script>
            """)
    IPage<OrderVO> selectOrderPage(IPage<OrderVO> page,
                                   @Param("orderNo") String orderNo,
                                   @Param("userId") Long userId,
                                   @Param("username") String username,
                                   @Param("payStatus") Integer payStatus,
                                   @Param("type") Integer type);
}
