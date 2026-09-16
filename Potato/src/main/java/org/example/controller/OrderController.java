package org.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.result.PageResult;
import org.example.common.result.Result;
import org.example.dto.OrderCreateDTO;
import org.example.dto.OrderPayDTO;
import org.example.service.OrderService;
import org.example.vo.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单交易接口（需登录）：模拟下单、模拟支付、订单查询
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 创建订单（待支付） */
    @PostMapping("/create")
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok("下单成功", orderService.create(dto));
    }

    /** 确认支付（模拟支付：扣减余额并放行阅读权限） */
    @PostMapping("/pay")
    public Result<OrderVO> pay(@Valid @RequestBody OrderPayDTO dto) {
        return Result.ok("支付成功", orderService.pay(dto));
    }

    /** 我的订单 */
    @GetMapping("/my")
    public Result<PageResult<OrderVO>> my(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.ok(orderService.myOrders(StpUtil.getLoginIdAsLong(), page, size));
    }

    /** 订单详情 */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.detail(StpUtil.getLoginIdAsLong(), orderNo));
    }
}
