package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.common.util.MaskUtil;
import org.example.dto.ApplyAuthorDTO;
import org.example.dto.BookSaveDTO;
import org.example.dto.BookStatusDTO;
import org.example.dto.BookWithChaptersDTO;
import org.example.dto.ChapterImportDTO;
import org.example.dto.ChapterSaveDTO;
import org.example.entity.Author;
import org.example.entity.Book;
import org.example.entity.Category;
import org.example.entity.Chapter;
import org.example.entity.Orders;
import org.example.entity.User;
import org.example.mapper.AuthorMapper;
import org.example.mapper.BookMapper;
import org.example.mapper.CategoryMapper;
import org.example.mapper.ChapterMapper;
import org.example.mapper.OrdersMapper;
import org.example.mapper.UserMapper;
import org.example.vo.AuthorApplyVO;
import org.example.vo.AuthorBookStatsVO;
import org.example.vo.AuthorStatsVO;
import org.example.vo.BookVO;
import org.example.vo.ChapterVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者端服务：入驻申请、作品管理、章节管理、数据看板
 */
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;
    private final ChapterMapper chapterMapper;
    private final CategoryMapper categoryMapper;
    private final OrdersMapper ordersMapper;
    private final UserMapper userMapper;
    private final BanService banService;

    // ==================== 入驻申请 ====================

    /** 提交/重新提交入驻申请 */
    @Transactional(rollbackFor = Exception.class)
    public AuthorApplyVO apply(ApplyAuthorDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Author author = authorMapper.selectOne(new LambdaQueryWrapper<Author>()
                .eq(Author::getUserId, userId));
        if (author != null && !Integer.valueOf(CommonConstant.AUDIT_REJECTED).equals(author.getAuditStatus())) {
            if (Integer.valueOf(CommonConstant.AUDIT_PASSED).equals(author.getAuditStatus())) {
                throw new BusinessException(400, "你已是作者，无需重复申请");
            }
            throw new BusinessException(400, "申请审核中，请耐心等待");
        }
        if (author == null) {
            author = new Author();
            author.setUserId(userId);
        }
        author.setRealName(dto.getRealName());
        author.setIdCard(dto.getIdCard());
        author.setIntro(dto.getIntro());
        author.setAuditStatus(CommonConstant.AUDIT_PENDING);
        author.setAuditReason(null);
        author.setAuditTime(null);
        if (author.getId() == null) {
            authorMapper.insert(author);
        } else {
            authorMapper.updateById(author);
        }
        return toApplyVO(author);
    }

    /** 查询当前用户入驻申请状态 */
    public AuthorApplyVO applyStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        Author author = authorMapper.selectOne(new LambdaQueryWrapper<Author>()
                .eq(Author::getUserId, userId));
        return author == null ? null : toApplyVO(author);
    }

    /** 获取当前用户的作者资质，未通过审核则抛出异常 */
    private Author requireAuthor() {
        Long userId = StpUtil.getLoginIdAsLong();
        Author author = authorMapper.selectOne(new LambdaQueryWrapper<Author>()
                .eq(Author::getUserId, userId));
        if (author == null) {
            throw new BusinessException(403, "请先提交作者入驻申请");
        }
        if (!Integer.valueOf(CommonConstant.AUDIT_PASSED).equals(author.getAuditStatus())) {
            throw new BusinessException(403, "作者入驻审核未通过，暂不能进行创作");
        }
        return author;
    }

    // ==================== 作品管理 ====================

    /** 我的作品列表 */
    public List<BookVO> myBooks() {
        Author author = requireAuthor();
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getAuthorId, author.getId())
                .orderByDesc(Book::getUpdateTime));
        List<BookVO> result = new ArrayList<>();
        for (Book book : books) {
            result.add(toBookVO(book));
        }
        return result;
    }

    /** 新建小说 */
    @Transactional(rollbackFor = Exception.class)
    public BookVO createBook(BookSaveDTO dto) {
        Author author = requireAuthor();
        // 写书封禁检查
        banService.checkAuthorWriteBan(author.getUserId());
        requireCategory(dto.getCategoryId());
        Book book = new Book();
        book.setAuthorId(author.getId());
        book.setCategoryId(dto.getCategoryId());
        book.setTitle(dto.getTitle());
        book.setCover(dto.getCover());
        book.setIntro(dto.getIntro());
        // 作者警告期间，新书状态设为待审核
        if (banService.isAuthorWarning(author.getUserId())) {
            book.setStatus(CommonConstant.BOOK_STATUS_PENDING);
        } else {
            book.setStatus(CommonConstant.BOOK_STATUS_SERIALIZING);
        }
        book.setIsRecommend(0);
        book.setClickCount(0);
        book.setCollectCount(0);
        bookMapper.insert(book);
        return toBookVO(book);
    }

    /** 编辑小说信息 */
    @Transactional(rollbackFor = Exception.class)
    public BookVO updateBook(Long bookId, BookSaveDTO dto) {
        Book book = requireOwnedBook(bookId);
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        requireCategory(dto.getCategoryId());
        book.setCategoryId(dto.getCategoryId());
        book.setTitle(dto.getTitle());
        book.setCover(dto.getCover());
        book.setIntro(dto.getIntro());
        bookMapper.updateById(book);
        return toBookVO(book);
    }

    /** 上架 / 完结 / 下架 */
    @Transactional(rollbackFor = Exception.class)
    public void updateBookStatus(Long bookId, BookStatusDTO dto) {
        Book book = requireOwnedBook(bookId);
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        // 待审核状态的书籍不允许作者自行改状态
        if (Integer.valueOf(CommonConstant.BOOK_STATUS_PENDING).equals(book.getStatus())) {
            throw new BusinessException(400, "书籍正在审核中，无法修改状态");
        }
        book.setStatus(dto.getStatus());
        bookMapper.updateById(book);
    }

    /** 删除小说（连同章节） */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long bookId) {
        Book book = requireOwnedBook(bookId);
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        chapterMapper.delete(new LambdaQueryWrapper<Chapter>().eq(Chapter::getBookId, bookId));
        bookMapper.deleteById(bookId);
    }

    /** 重新提交审核（驳回后修改并重新提交，status 0→3） */
    @Transactional(rollbackFor = Exception.class)
    public BookVO resubmitBook(Long bookId) {
        Book book = requireOwnedBook(bookId);
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        if (!Integer.valueOf(CommonConstant.BOOK_STATUS_OFF).equals(book.getStatus())) {
            throw new BusinessException(400, "仅被驳回的书籍可以重新提交审核");
        }
        book.setStatus(CommonConstant.BOOK_STATUS_PENDING);
        bookMapper.updateById(book);
        return toBookVO(book);
    }

    /** 新建作品（含章节），一个事务中创建书籍和所有章节 */
    @Transactional(rollbackFor = Exception.class)
    public BookVO createBookWithChapters(BookWithChaptersDTO dto) {
        Author author = requireAuthor();
        banService.checkAuthorWriteBan(author.getUserId());
        requireCategory(dto.getCategoryId());

        // 1. 创建书籍
        Book book = new Book();
        book.setAuthorId(author.getId());
        book.setCategoryId(dto.getCategoryId());
        book.setTitle(dto.getTitle());
        book.setCover(dto.getCover());
        book.setIntro(dto.getIntro());
        if (banService.isAuthorWarning(author.getUserId())) {
            book.setStatus(CommonConstant.BOOK_STATUS_PENDING);
        } else {
            book.setStatus(CommonConstant.BOOK_STATUS_SERIALIZING);
        }
        book.setIsRecommend(0);
        book.setClickCount(0);
        book.setCollectCount(0);
        bookMapper.insert(book);

        // 2. 批量创建章节
        if (dto.getChapters() != null && !dto.getChapters().isEmpty()) {
            int sort = 1;
            for (BookWithChaptersDTO.ChapterItem item : dto.getChapters()) {
                Chapter chapter = new Chapter();
                chapter.setBookId(book.getId());
                chapter.setTitle(item.getTitle());
                chapter.setContent(item.getContent());
                chapter.setSort(sort++);
                chapter.setReadCount(0);
                chapter.setPayCount(0);
                // 免费/付费
                Integer isFree = item.getIsFree() == null ? 1 : item.getIsFree();
                if (Integer.valueOf(CommonConstant.CHAPTER_FREE).equals(isFree)) {
                    chapter.setIsFree(CommonConstant.CHAPTER_FREE);
                    chapter.setPrice(0);
                } else {
                    if (item.getPrice() == null || item.getPrice() <= 0) {
                        throw new BusinessException(400, "付费章节「" + item.getTitle() + "」必须设置价格且大于 0");
                    }
                    chapter.setIsFree(CommonConstant.CHAPTER_PAID);
                    chapter.setPrice(item.getPrice());
                }
                chapterMapper.insert(chapter);
            }
        }
        return toBookVO(book);
    }

    /** 从纯文本导入章节，按章节标题自动拆分 */
    @Transactional(rollbackFor = Exception.class)
    public int importChapters(ChapterImportDTO dto) {
        Book book = requireOwnedBook(dto.getBookId());
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        if (Integer.valueOf(CommonConstant.BOOK_STATUS_PENDING).equals(book.getStatus())) {
            throw new BusinessException(400, "书籍正在审核中，无法添加章节");
        }

        // 解析文本，按章节标题拆分
        List<String[]> chapters = parseChapterText(dto.getText());
        if (chapters.isEmpty()) {
            throw new BusinessException(400, "未能识别到章节，请确保文本中包含「第X章」格式的章节标题");
        }

        int startSort = chapterMapper.selectMaxSort(dto.getBookId()) + 1;
        int count = 0;
        for (String[] ch : chapters) {
            Chapter chapter = new Chapter();
            chapter.setBookId(dto.getBookId());
            chapter.setTitle(ch[0]);
            chapter.setContent(ch[1]);
            chapter.setSort(startSort + count);
            chapter.setReadCount(0);
            chapter.setPayCount(0);
            Integer isFree = dto.getIsFree() == null ? 1 : dto.getIsFree();
            if (Integer.valueOf(CommonConstant.CHAPTER_FREE).equals(isFree)) {
                chapter.setIsFree(CommonConstant.CHAPTER_FREE);
                chapter.setPrice(0);
            } else {
                if (dto.getPrice() == null || dto.getPrice() <= 0) {
                    throw new BusinessException(400, "付费章节必须设置价格且大于 0");
                }
                chapter.setIsFree(CommonConstant.CHAPTER_PAID);
                chapter.setPrice(dto.getPrice());
            }
            chapterMapper.insert(chapter);
            count++;
        }
        return count;
    }

    /**
     * 解析纯文本，按章节标题拆分
     * 支持格式：第X章、第X节、第一章、第一节、Chapter X 等
     * 返回 List<String[2]>，每个元素为 [标题, 正文]
     */
    private List<String[]> parseChapterText(String text) {
        List<String[]> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        // 统一换行符
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        // 章节标题正则：第X章/第X节/第一章/第一节/Chapter X/卷X
        String regex = "(?m)^(第[一二三四五六七八九十百千万零〇\\d]+[章节卷回集部篇]|Chapter\\s+\\d+|第\\d+章).*$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(normalized);

        List<int[]> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(new int[]{matcher.start(), matcher.end()});
        }

        if (matches.isEmpty()) {
            return result;
        }

        for (int i = 0; i < matches.size(); i++) {
            int titleStart = matches.get(i)[0];
            int titleEnd = matches.get(i)[1];
            int contentEnd = (i + 1 < matches.size()) ? matches.get(i + 1)[0] : normalized.length();

            String title = normalized.substring(titleStart, titleEnd).trim();
            String content = normalized.substring(titleEnd, contentEnd).trim();

            // 跳过空内容的章节
            if (!content.isBlank()) {
                result.add(new String[]{title, content});
            }
        }
        return result;
    }

    // ==================== 章节管理 ====================

    /** 作品章节列表 */
    public List<ChapterVO> chapters(Long bookId) {
        Book book = requireOwnedBook(bookId);
        List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, bookId)
                .orderByAsc(Chapter::getSort)
                .orderByAsc(Chapter::getId));
        return chapters.stream().map(this::toChapterVO).toList();
    }

    /** 新建章节 */
    @Transactional(rollbackFor = Exception.class)
    public ChapterVO createChapter(ChapterSaveDTO dto) {
        Book book = requireOwnedBook(dto.getBookId());
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        // 待审核书籍不允许添加章节
        if (Integer.valueOf(CommonConstant.BOOK_STATUS_PENDING).equals(book.getStatus())) {
            throw new BusinessException(400, "书籍正在审核中，无法添加章节");
        }
        Chapter chapter = new Chapter();
        chapter.setBookId(dto.getBookId());
        applyChapterFields(chapter, dto);
        int sort = dto.getSort() == null
                ? chapterMapper.selectMaxSort(dto.getBookId()) + 1
                : dto.getSort();
        chapter.setSort(sort);
        chapter.setReadCount(0);
        chapter.setPayCount(0);
        chapterMapper.insert(chapter);
        return toChapterVO(chapter);
    }

    /** 编辑章节 */
    @Transactional(rollbackFor = Exception.class)
    public ChapterVO updateChapter(Long chapterId, ChapterSaveDTO dto) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(404, "章节不存在");
        }
        requireOwnedBook(chapter.getBookId());
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        applyChapterFields(chapter, dto);
        chapterMapper.updateById(chapter);
        return toChapterVO(chapter);
    }

    /** 删除章节 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(404, "章节不存在");
        }
        requireOwnedBook(chapter.getBookId());
        banService.checkAuthorWriteBan(StpUtil.getLoginIdAsLong());
        chapterMapper.deleteById(chapterId);
    }

    private void applyChapterFields(Chapter chapter, ChapterSaveDTO dto) {
        chapter.setTitle(dto.getTitle());
        chapter.setContent(dto.getContent());
        if (Integer.valueOf(CommonConstant.CHAPTER_FREE).equals(dto.getIsFree())) {
            chapter.setIsFree(CommonConstant.CHAPTER_FREE);
            chapter.setPrice(0);
        } else {
            if (dto.getPrice() == null || dto.getPrice() <= 0) {
                throw new BusinessException(400, "付费章节必须设置价格且大于 0");
            }
            chapter.setIsFree(CommonConstant.CHAPTER_PAID);
            chapter.setPrice(dto.getPrice());
        }
        if (dto.getSort() != null) {
            chapter.setSort(dto.getSort());
        }
        chapter.setWords(dto.getContent().length());
    }

    // ==================== 数据看板 ====================

    /** 作者数据统计（按作品维度，支持按支付时间范围筛选收益） */
    public AuthorStatsVO stats(LocalDate start, LocalDate end) {
        Author author = requireAuthor();
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getAuthorId, author.getId())
                .orderByDesc(Book::getUpdateTime));

        AuthorStatsVO stats = new AuthorStatsVO();
        stats.setBookCount(books.size());
        List<AuthorBookStatsVO> bookStats = new ArrayList<>();
        long totalClick = 0;
        long totalCollect = 0;
        long totalPayCount = 0;
        long totalRevenue = 0;

        for (Book book : books) {
            AuthorBookStatsVO item = new AuthorBookStatsVO();
            item.setBookId(book.getId());
            item.setTitle(book.getTitle());
            item.setStatus(book.getStatus());
            item.setStatusText(bookStatusText(book.getStatus()));
            item.setClickCount(book.getClickCount());
            item.setCollectCount(book.getCollectCount());
            item.setUpdateTime(book.getUpdateTime());

            long chapterCount = chapterMapper.selectCount(new LambdaQueryWrapper<Chapter>()
                    .eq(Chapter::getBookId, book.getId()));
            item.setChapterCount(chapterCount);

            // 付费阅读次数 = 章节 pay_count 合计
            List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                    .eq(Chapter::getBookId, book.getId()));
            long payCount = chapters.stream().mapToLong(c ->
                    c.getPayCount() == null ? 0L : c.getPayCount()).sum();
            item.setPayCount(payCount);

            // 收益 = 该书已支付订单金额合计（可按支付时间范围过滤）
            LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getBookId, book.getId())
                    .eq(Orders::getPayStatus, CommonConstant.ORDER_PAY_PAID);
            if (start != null) {
                orderWrapper.ge(Orders::getPayTime, start.atStartOfDay());
            }
            if (end != null) {
                orderWrapper.le(Orders::getPayTime, end.atTime(LocalTime.MAX));
            }
            List<Orders> paidOrders = ordersMapper.selectList(orderWrapper);
            long revenue = paidOrders.stream().mapToLong(o -> o.getAmount() == null ? 0L : o.getAmount()).sum();
            item.setRevenue(revenue);

            bookStats.add(item);
            totalClick += item.getClickCount();
            totalCollect += item.getCollectCount();
            totalPayCount += payCount;
            totalRevenue += revenue;
        }
        stats.setBooks(bookStats);
        stats.setTotalClick(totalClick);
        stats.setTotalCollect(totalCollect);
        stats.setTotalPayCount(totalPayCount);
        stats.setTotalRevenue(totalRevenue);
        return stats;
    }

    // ==================== 内部工具 ====================

    private Book requireOwnedBook(Long bookId) {
        Author author = requireAuthor();
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(404, "小说不存在");
        }
        if (!book.getAuthorId().equals(author.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该作品");
        }
        return book;
    }

    private void requireCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(400, "分类不存在");
        }
    }

    private AuthorApplyVO toApplyVO(Author author) {
        AuthorApplyVO vo = new AuthorApplyVO();
        vo.setId(author.getId());
        vo.setUserId(author.getUserId());
        User user = userMapper.selectById(author.getUserId());
        vo.setUsername(user != null ? user.getUsername() : null);
        vo.setRealName(author.getRealName());
        vo.setIdCard(MaskUtil.maskIdCard(author.getIdCard()));
        vo.setIntro(author.getIntro());
        vo.setAuditStatus(author.getAuditStatus());
        vo.setAuditStatusText(auditStatusText(author.getAuditStatus()));
        vo.setAuditReason(author.getAuditReason());
        vo.setAuditTime(author.getAuditTime());
        vo.setCreateTime(author.getCreateTime());
        return vo;
    }

    private BookVO toBookVO(Book book) {
        BookVO vo = new BookVO();
        vo.setId(book.getId());
        vo.setAuthorId(book.getAuthorId());
        vo.setCategoryId(book.getCategoryId());
        vo.setTitle(book.getTitle());
        vo.setCover(book.getCover());
        vo.setIntro(book.getIntro());
        vo.setStatus(book.getStatus());
        vo.setStatusText(bookStatusText(book.getStatus()));
        vo.setIsRecommend(book.getIsRecommend());
        vo.setClickCount(book.getClickCount());
        vo.setCollectCount(book.getCollectCount());
        vo.setCreateTime(book.getCreateTime());
        vo.setUpdateTime(book.getUpdateTime());
        Category category = categoryMapper.selectById(book.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : null);
        Author author = authorMapper.selectById(book.getAuthorId());
        if (author != null) {
            User user = userMapper.selectById(author.getUserId());
            vo.setAuthorName(user != null ? user.getNickname() : null);
            vo.setAuthorUserId(author.getUserId());
        }
        Long chapterCount = chapterMapper.selectCount(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, book.getId()));
        vo.setChapterCount(chapterCount);
        return vo;
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

    private String auditStatusText(Integer status) {
        if (status == null) {
            return "待审核";
        }
        return switch (status) {
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "待审核";
        };
    }

    private String bookStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "连载中";
            case 2 -> "已完结";
            case 3 -> "待审核";
            default -> "已下架";
        };
    }
}
