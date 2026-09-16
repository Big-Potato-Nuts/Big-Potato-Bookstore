package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.entity.Author;
import org.example.entity.Book;
import org.example.entity.Chapter;
import org.example.entity.Orders;
import org.example.entity.User;
import org.example.mapper.AuthorMapper;
import org.example.mapper.BookMapper;
import org.example.mapper.ChapterMapper;
import org.example.mapper.OrdersMapper;
import org.example.mapper.UserMapper;
import org.example.vo.ChapterContentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 章节阅读服务：正文读取、付费权限校验、阅读计数、进度记录
 */
@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterMapper chapterMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final AuthorMapper authorMapper;
    private final OrdersMapper ordersMapper;
    private final BookshelfService bookshelfService;
    private final BanService banService;

    /**
     * 阅读章节正文：
     *  - 免费章节直接阅读；
     *  - 付费章节需登录且已购买（或为本书作者/管理员）；
     *  - 阅读后累加阅读量，登录用户自动记录阅读进度。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChapterContentVO read(Long chapterId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(404, "章节不存在");
        }
        Book book = bookMapper.selectById(chapter.getBookId());
        if (book == null
                || Integer.valueOf(CommonConstant.BOOK_STATUS_OFF).equals(book.getStatus())
                || Integer.valueOf(CommonConstant.BOOK_STATUS_PENDING).equals(book.getStatus())) {
            throw new BusinessException(404, "章节不存在或所属小说已下架");
        }

        // 付费章节权限校验
        if (!Integer.valueOf(CommonConstant.CHAPTER_FREE).equals(chapter.getIsFree())) {
            checkPaidChapterAccess(book, chapter);
        }

        // 阅读量累加
        chapter.setReadCount(chapter.getReadCount() + 1);
        chapterMapper.updateById(chapter);
        book.setClickCount(book.getClickCount() + 1);
        bookMapper.updateById(book);

        // 登录用户自动记录阅读进度
        if (StpUtil.isLogin()) {
            bookshelfService.recordProgress(StpUtil.getLoginIdAsLong(), book.getId(), chapter.getId(), 0);
        }

        return buildContentVO(book, chapter);
    }

    /** 校验付费章节访问权限 */
    private void checkPaidChapterAccess(Book book, Chapter chapter) {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "付费章节需要登录后购买阅读");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        // 管理员或本书作者可直接阅读
        if (user != null && user.getRole() == CommonConstant.ROLE_ADMIN) {
            return;
        }
        if (isBookAuthor(book, userId)) {
            return;
        }
        // 阅读购买封禁校验
        banService.checkBan(userId, CommonConstant.BAN_READ, "阅读付费章节");
        // 已购买单章或整本
        Long purchased = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getPayStatus, CommonConstant.ORDER_PAY_PAID)
                .and(w -> w.eq(Orders::getType, CommonConstant.ORDER_TYPE_CHAPTER)
                        .eq(Orders::getChapterId, chapter.getId())
                        .or()
                        .eq(Orders::getType, CommonConstant.ORDER_TYPE_BOOK)
                        .eq(Orders::getBookId, book.getId())));
        if (purchased == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "该章节为付费章节，请先购买");
        }
    }

    private boolean isBookAuthor(Book book, Long userId) {
        Author author = authorMapper.selectById(book.getAuthorId());
        return author != null && author.getUserId().equals(userId);
    }

    private ChapterContentVO buildContentVO(Book book, Chapter chapter) {
        ChapterContentVO vo = new ChapterContentVO();
        vo.setId(chapter.getId());
        vo.setBookId(book.getId());
        vo.setBookTitle(book.getTitle());
        vo.setTitle(chapter.getTitle());
        vo.setContent(chapter.getContent());
        vo.setIsFree(chapter.getIsFree());
        vo.setPrice(chapter.getPrice());
        vo.setSort(chapter.getSort());
        vo.setWords(chapter.getWords());
        vo.setReadCount(chapter.getReadCount());
        vo.setCreateTime(chapter.getCreateTime());

        // 上一章 / 下一章
        List<Chapter> ordered = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, book.getId())
                .orderByAsc(Chapter::getSort)
                .orderByAsc(Chapter::getId));
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).getId().equals(chapter.getId())) {
                if (i > 0) {
                    vo.setPrevChapterId(ordered.get(i - 1).getId());
                }
                if (i < ordered.size() - 1) {
                    vo.setNextChapterId(ordered.get(i + 1).getId());
                }
                break;
            }
        }
        return vo;
    }
}
