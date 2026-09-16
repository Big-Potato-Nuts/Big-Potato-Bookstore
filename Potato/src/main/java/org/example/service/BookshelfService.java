package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.dto.BookshelfAddDTO;
import org.example.dto.CollectDTO;
import org.example.dto.ProgressDTO;
import org.example.entity.Book;
import org.example.entity.Bookshelf;
import org.example.mapper.BookMapper;
import org.example.mapper.BookshelfMapper;
import org.example.vo.BookshelfVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 书架服务：收藏、阅读记录、阅读进度
 */
@Service
@RequiredArgsConstructor
public class BookshelfService {

    private final BookshelfMapper bookshelfMapper;
    private final BookMapper bookMapper;

    /** 我的书架（收藏 + 阅读记录） */
    public List<BookshelfVO> list(Long userId) {
        return bookshelfMapper.selectShelfList(userId);
    }

    /** 加入书架（默认同时收藏） */
    @Transactional(rollbackFor = Exception.class)
    public BookshelfVO add(Long userId, BookshelfAddDTO dto) {
        Book book = requireOnShelfBook(dto.getBookId());
        int collect = dto.getIsCollect() == null ? CommonConstant.SHELF_COLLECTED : dto.getIsCollect();

        Bookshelf existing = find(userId, dto.getBookId());
        if (existing != null) {
            boolean wasCollected = Integer.valueOf(CommonConstant.SHELF_COLLECTED).equals(existing.getIsCollect());
            if (collect == CommonConstant.SHELF_COLLECTED && !wasCollected) {
                increaseCollectCount(book.getId());
            }
            existing.setIsCollect(collect);
            bookshelfMapper.updateById(existing);
        } else {
            Bookshelf shelf = new Bookshelf();
            shelf.setUserId(userId);
            shelf.setBookId(dto.getBookId());
            shelf.setIsCollect(collect);
            shelf.setReadPosition(0);
            bookshelfMapper.insert(shelf);
            if (collect == CommonConstant.SHELF_COLLECTED) {
                increaseCollectCount(book.getId());
            }
        }
        return list(userId).stream()
                .filter(v -> v.getBookId().equals(dto.getBookId()))
                .findFirst()
                .orElse(null);
    }

    /** 记录阅读进度（阅读时自动调用；也支持前端手动上报） */
    @Transactional(rollbackFor = Exception.class)
    public void recordProgress(Long userId, ProgressDTO dto) {
        recordProgress(userId, dto.getBookId(), dto.getChapterId(),
                dto.getPosition() == null ? 0 : dto.getPosition());
    }

    /** 记录阅读进度（阅读时自动调用；也支持前端手动上报） */
    @Transactional(rollbackFor = Exception.class)
    public void recordProgress(Long userId, Long bookId, Long chapterId, int position) {
        Bookshelf existing = find(userId, bookId);
        if (existing != null) {
            existing.setReadChapterId(chapterId);
            existing.setReadPosition(position);
            existing.setUpdateTime(java.time.LocalDateTime.now());
            bookshelfMapper.updateById(existing);
        } else {
            Bookshelf shelf = new Bookshelf();
            shelf.setUserId(userId);
            shelf.setBookId(bookId);
            shelf.setReadChapterId(chapterId);
            shelf.setReadPosition(position);
            shelf.setIsCollect(CommonConstant.SHELF_NOT_COLLECT);
            bookshelfMapper.insert(shelf);
        }
    }

    /** 收藏 / 取消收藏 */
    @Transactional(rollbackFor = Exception.class)
    public void setCollect(Long userId, CollectDTO dto) {
        Book book = requireOnShelfBook(dto.getBookId());
        Bookshelf existing = find(userId, dto.getBookId());
        if (existing == null) {
            recordProgress(userId, dto.getBookId(), null, 0);
            existing = find(userId, dto.getBookId());
        }
        boolean wasCollected = Integer.valueOf(CommonConstant.SHELF_COLLECTED).equals(existing.getIsCollect());
        boolean nowCollected = Integer.valueOf(CommonConstant.SHELF_COLLECTED).equals(dto.getIsCollect());
        if (!wasCollected && nowCollected) {
            increaseCollectCount(book.getId());
        } else if (wasCollected && !nowCollected) {
            decreaseCollectCount(book.getId());
        }
        existing.setIsCollect(dto.getIsCollect());
        bookshelfMapper.updateById(existing);
    }

    /** 从书架移除 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long bookId) {
        Bookshelf existing = find(userId, bookId);
        if (existing == null) {
            return;
        }
        if (Integer.valueOf(CommonConstant.SHELF_COLLECTED).equals(existing.getIsCollect())) {
            decreaseCollectCount(bookId);
        }
        bookshelfMapper.deleteById(existing.getId());
    }

    private Bookshelf find(Long userId, Long bookId) {
        return bookshelfMapper.selectOne(new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId)
                .eq(Bookshelf::getBookId, bookId));
    }

    private Book requireOnShelfBook(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || Integer.valueOf(CommonConstant.BOOK_STATUS_OFF).equals(book.getStatus())) {
            throw new BusinessException(404, "小说不存在或已下架");
        }
        return book;
    }

    private void increaseCollectCount(Long bookId) {
        bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                .eq(Book::getId, bookId)
                .setSql("collect_count = collect_count + 1"));
    }

    private void decreaseCollectCount(Long bookId) {
        bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                .eq(Book::getId, bookId)
                .setSql("collect_count = GREATEST(collect_count - 1, 0)"));
    }
}
