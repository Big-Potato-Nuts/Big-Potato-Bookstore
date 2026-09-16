package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.result.Result;
import org.example.dto.RechargeDTO;
import org.example.dto.UpdatePasswordDTO;
import org.example.dto.UpdateProfileDTO;
import org.example.service.UserService;
import org.example.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 个人中心接口（需登录）
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 修改个人资料 */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return Result.ok(userService.updateProfile(dto));
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.ok("修改成功", null);
    }

    /** 模拟充值 */
    @PostMapping("/recharge")
    public Result<Map<String, Object>> recharge(@Valid @RequestBody RechargeDTO dto) {
        return Result.ok("充值成功", userService.recharge(dto));
    }

    /** 查询余额 */
    @GetMapping("/balance")
    public Result<Map<String, Object>> balance() {
        return Result.ok(userService.balance());
    }
}
