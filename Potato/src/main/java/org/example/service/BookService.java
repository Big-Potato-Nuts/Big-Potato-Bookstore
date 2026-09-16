package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.PageResult;
import org.example.entity.Book;
import org.example.entity.Bookshelf;
import org.example.entity.Chapter;
import org.example.entity.Orders;
import org.example.mapper.BookMapper;
import org.example.mapper.BookshelfMapper;
import org.example.mapper.ChapterMapper;
import org.example.mapper.OrdersMapper;
import org.example.vo.BookVO;
import org.example.vo.ChapterVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小说浏览服务：首页推荐、分类浏览、排行榜、搜索、详情、目录
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final ChapterMapper chapterMapper;
    private final OrdersMapper ordersMapper;
    private final BookshelfMapper bookshelfMapper;

    /** 分页浏览/搜索（读者端仅展示上架作品） */
    public PageResult<BookVO> pageBooks(int page, int size, Long categoryId, String keyword) {
        IPage<BookVO> result = bookMapper.selectBookPage(new Page<>(page, size),
                categoryId, trimToNull(keyword), null, "update", null, null, true);
        result.getRecords().forEach(this::fillStatusText);
        return PageResult.of(result);
    }

    /** 首页推荐位 */
    public List<BookVO> recommend(int limit) {
        limit = Math.min(Math.max(limit, 1), 20);
        IPage<BookVO> result = bookMapper.selectBookPage(new Page<>(1, limit),
                null, null, null, "click", null, 1, true);
        result.getRecords().forEach(this::fillStatusText);
        return result.getRecords();
    }

    /** 排行榜：click-人气 / collect-收藏 / update-最近更新 */
    public List<BookVO> rank(String type, int limit) {
        String orderBy = switch (type == null ? "click" : type) {
            case "collect" -> "collect";
            case "update" -> "update";
            default -> "click";
        };
        limit = Math.min(Math.max(limit, 1), 50);
        IPage<BookVO> result = bookMapper.selectBookPage(new Page<>(1, limit),
                null, null, null, orderBy, null, null, true);
        result.getRecords().forEach(this::fillStatusText);
        return result.getRecords();
    }

    /** 小说详情（含收藏/购买状态） */
    public BookVO detail(Long bookId) {
        IPage<BookVO> result = bookMapper.selectBookPage(new Page<>(1, 1),
                null, null, null, "update", bookId, null, true);
        List<BookVO> records = result.getRecords();
        if (records.isEmpty()) {
            throw new BusinessException(404, "小说不存在或已下架");
        }
        BookVO vo = records.get(0);
        fillStatusText(vo);
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            // 是否已收藏
            Long collected = bookshelfMapper.selectCount(new LambdaQueryWrapper<Bookshelf>()
                    .eq(Bookshelf::getUserId, userId)
                    .eq(Bookshelf::getBookId, bookId)
                    .eq(Bookshelf::getIsCollect, CommonConstant.SHELF_COLLECTED));
            vo.setCollected(collected > 0);
            // 是否已购买整本
            Long purchased = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getUserId, userId)
                    .eq(Orders::getBookId, bookId)
                    .eq(Orders::getType, CommonConstant.ORDER_TYPE_BOOK)
                    .eq(Orders::getPayStatus, CommonConstant.ORDER_PAY_PAID));
            vo.setPurchased(purchased > 0);
        }
        return vo;
    }

    /** 小说章节目录（不含正文） */
    public List<ChapterVO> chapters(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null
                || Integer.valueOf(CommonConstant.BOOK_STATUS_OFF).equals(book.getStatus())
                || Integer.valueOf(CommonConstant.BOOK_STATUS_PENDING).equals(book.getStatus())) {
            throw new BusinessException(404, "小说不存在或已下架");
        }
        List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, bookId)
                .orderByAsc(Chapter::getSort)
                .orderByAsc(Chapter::getId));
        return chapters.stream().map(this::toChapterVO).toList();
    }

    private ChapterVO toChapterVO(Chapter chapter) {
        ChapterVO vo = new ChapterVO();
        vo.setId(chapter.getId());
        vo.setBookId(chapter.getBookId());
        vo.setTitle(chapter.getTitle());
        vo.setIsFree(chapter.getIsFree());
        vo.setIsFreeText(Integer.valueOf(CommonConstant.CHAPTER_FREE).equals(chapter.getIsFree()) ? "免费" : "付费");
        vo.setPrice(chapter.getPrice());
        vo.setSort(chapter.getSort());
        vo.setWords(chapter.getWords());
        vo.setReadCount(chapter.getReadCount());
        vo.setPayCount(chapter.getPayCount());
        vo.setCreateTime(chapter.getCreateTime());
        return vo;
    }

    private void fillStatusText(BookVO vo) {
        if (vo.getStatus() == null) {
            vo.setStatusText("未知");
        } else {
            vo.setStatusText(switch (vo.getStatus()) {
                case 1 -> "连载中";
                case 2 -> "已完结";
                case 3 -> "待审核";
                default -> "已下架";
            });
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
