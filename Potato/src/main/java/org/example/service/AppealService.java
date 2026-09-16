package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.PageResult;
import org.example.dto.AppealCreateDTO;
import org.example.entity.User;
import org.example.entity.UserAppeal;
import org.example.mapper.UserAppealMapper;
import org.example.mapper.UserMapper;
import org.example.vo.UserAppealVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户申诉服务：发起申诉、我的申诉列表
 */
@Service
@RequiredArgsConstructor
public class AppealService {

    private final UserAppealMapper appealMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /** 发起申诉 */
    @Transactional(rollbackFor = Exception.class)
    public UserAppealVO create(AppealCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 检查是否有生效中的处罚
        boolean hasBan = (user.getBanFlags() != null && user.getBanFlags() > 0)
                || (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_DISABLED);
        if (!hasBan) {
            throw new BusinessException(400, "当前账号无生效处罚，无需申诉");
        }
        // 检查是否有待处理的申诉
        Long pendingCount = appealMapper.selectCount(new LambdaQueryWrapper<UserAppeal>()
                .eq(UserAppeal::getUserId, userId)
                .eq(UserAppeal::getStatus, CommonConstant.APPEAL_PENDING));
        if (pendingCount > 0) {
            throw new BusinessException(400, "你已有一条待处理的申诉，请等待管理员审核");
        }
        UserAppeal appeal = new UserAppeal();
        appeal.setUserId(userId);
        appeal.setAppealType(dto.getAppealType());
        appeal.setReason(dto.getReason());
        appeal.setStatus(CommonConstant.APPEAL_PENDING);
        appealMapper.insert(appeal);
        return toVO(appeal);
    }

    /** 我的申诉列表 */
    public PageResult<UserAppealVO> myAppeals(int page, int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        IPage<UserAppeal> result = appealMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UserAppeal>()
                        .eq(UserAppeal::getUserId, userId)
                        .orderByDesc(UserAppeal::getCreateTime));
        List<UserAppealVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result, records);
    }

    /** 检查当前用户是否有待处理申诉（前端用） */
    public boolean hasPending() {
        if (!StpUtil.isLogin()) return false;
        Long userId = StpUtil.getLoginIdAsLong();
        Long count = appealMapper.selectCount(new LambdaQueryWrapper<UserAppeal>()
                .eq(UserAppeal::getUserId, userId)
                .eq(UserAppeal::getStatus, CommonConstant.APPEAL_PENDING));
        return count > 0;
    }

    private UserAppealVO toVO(UserAppeal appeal) {
        UserAppealVO vo = new UserAppealVO();
        vo.setId(appeal.getId());
        vo.setUserId(appeal.getUserId());
        vo.setAppealType(appeal.getAppealType());
        vo.setAppealTypeText(appealTypeText(appeal.getAppealType()));
        vo.setReason(appeal.getReason());
        vo.setStatus(appeal.getStatus());
        vo.setStatusText(appealStatusText(appeal.getStatus()));
        vo.setAdminReply(appeal.getAdminReply());
        vo.setAdminId(appeal.getAdminId());
        vo.setHandleTime(appeal.getHandleTime());
        vo.setCreateTime(appeal.getCreateTime());
        User user = userMapper.selectById(appeal.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        if (appeal.getAdminId() != null) {
            User admin = userMapper.selectById(appeal.getAdminId());
            if (admin != null) {
                vo.setAdminName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
            }
        }
        return vo;
    }

    private String appealTypeText(Integer type) {
        if (type == null) return "其他";
        return switch (type) {
            case 1 -> "评论封禁";
            case 2 -> "账号封禁";
            default -> "其他";
        };
    }

    private String appealStatusText(Integer status) {
        if (status == null) return "待处理";
        return switch (status) {
            case 1 -> "已通过";
            case 2 -> "已驳回";
            case 3 -> "已减轻处罚";
            default -> "待处理";
        };
    }
}
