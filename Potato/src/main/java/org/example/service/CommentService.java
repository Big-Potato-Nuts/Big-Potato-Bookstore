package org.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.PageResult;
import org.example.common.util.SensitiveWordUtil;
import org.example.dto.CommentDTO;
import org.example.entity.Book;
import org.example.entity.Comment;
import org.example.mapper.BookMapper;
import org.example.mapper.CommentMapper;
import org.example.vo.CommentVO;
import org.springframework.stereotype.Service;

/**
 * 评论服务：发表、查看
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final BookMapper bookMapper;
    private final BanService banService;

    /** 小说评论列表（仅正常评论，按时间倒序） */
    public PageResult<CommentVO> pageByBook(Long bookId, int page, int size) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(404, "小说不存在");
        }
        IPage<CommentVO> result = commentMapper.selectCommentPage(new Page<>(page, size),
                bookId, null, null, CommonConstant.COMMENT_NORMAL);
        result.getRecords().forEach(this::fillText);
        return PageResult.of(result);
    }

    /** 发表评论（基础敏感词过滤 + 长度校验） */
    public CommentVO add(Long userId, Long bookId, CommentDTO dto) {
        // 评论封禁校验
        banService.checkBan(userId, CommonConstant.BAN_COMMENT, "发表评论");
        Book book = bookMapper.selectById(bookId);
        if (book == null || Integer.valueOf(CommonConstant.BOOK_STATUS_OFF).equals(book.getStatus())) {
            throw new BusinessException(404, "小说不存在或已下架");
        }
        if (SensitiveWordUtil.containsSensitiveWord(dto.getContent())) {
            throw new BusinessException(400, "评论内容包含敏感词，请修改后重试");
        }
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setBookId(bookId);
        comment.setContent(dto.getContent());
        comment.setStatus(CommonConstant.COMMENT_NORMAL);
        commentMapper.insert(comment);

        IPage<CommentVO> result = commentMapper.selectCommentPage(new Page<>(1, 1),
                bookId, null, null, CommonConstant.COMMENT_NORMAL);
        return result.getRecords().stream()
                .filter(v -> v.getId().equals(comment.getId()))
                .findFirst()
                .orElse(null);
    }

    /** 我的评论记录 */
    public PageResult<CommentVO> myComments(Long userId, int page, int size) {
        IPage<CommentVO> result = commentMapper.selectCommentPage(new Page<>(page, size),
                null, userId, null, null);
        result.getRecords().forEach(this::fillText);
        return PageResult.of(result);
    }

    private void fillText(CommentVO vo) {
        vo.setStatusText(Integer.valueOf(CommonConstant.COMMENT_NORMAL).equals(vo.getStatus()) ? "正常" : "已屏蔽");
    }
}
