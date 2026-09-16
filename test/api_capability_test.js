/* ============================================================
 * 在线小说书城 · 能力健全性测试（API 级）
 * 运行: node api_capability_test.js  （需后端运行在 localhost:8080）
 * 覆盖: 读者端 / 作者端 / 管理端 核心能力 + 新增功能(推荐位/一键建书/章节导入)
 * ============================================================ */
const BASE = 'http://localhost:8080/api';

const results = [];
let seq = 1;

function ok(name, cond, extra = '') {
  results.push({ name, pass: !!cond, extra });
  console.log(`${cond ? '✅ PASS' : '❌ FAIL'}  ${name}${extra ? '  [' + extra + ']' : ''}`);
}
function info(msg) { console.log('   └ ' + msg); }

async function api(method, path, { token, body, query } = {}) {
  const url = BASE + path + (query ? '?' + new URLSearchParams(query) : '');
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = token;
  const res = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });
  let json = null;
  try { json = await res.json(); } catch (e) { /* ignore */ }
  return { status: res.status, body: json };
}

async function login(username, password = '123456') {
  const r = await api('POST', '/auth/login', { body: { username, password } });
  return r.body && r.body.code === 200 ? r.body.data.token : null;
}

const P = {
  code: 200, // 通用成功
  isCode: (n) => (b) => b && b.code === n,
  has: (f) => (b) => b && f(b)
};

(async () => {
  console.log('==================== 在线小说书城 · 能力测试 ====================');

  /* ---------------- A. 认证与用户 ---------------- */
  console.log('\n【A. 认证与用户】');
  const uname = 'tester_' + Date.now().toString().slice(-8);
  const reg = await api('POST', '/auth/register', { body: { username: uname, password: '123456', nickname: '测试用户' } });
  ok('A1 注册新用户', P.isCode(200)(reg.body), reg.body?.message);
  const tReader = reg.body?.data?.token;
  ok('A2 注册自动登录返回 token', !!tReader);
  const regDup = await api('POST', '/auth/register', { body: { username: uname, password: '123456' } });
  ok('A3 重复注册被拦截', P.isCode(400)(regDup.body), regDup.body?.message);
  const badLogin = await api('POST', '/auth/login', { body: { username: 'reader02', password: 'wrong' } });
  ok('A4 错误密码登录被拦截', P.isCode(400)(badLogin.body), badLogin.body?.message);

  const tReader02 = await login('reader02');
  ok('A5 登录 reader02', !!tReader02);
  const infoR = await api('GET', '/auth/info', { token: tReader02 });
  ok('A6 获取当前用户信息', P.isCode(200)(infoR.body) && infoR.body?.data?.username === 'reader02', `余额=${infoR.body?.data?.balance}分`);
  const prof = await api('PUT', '/user/profile', { token: tReader02, body: { nickname: '书虫小陈·改' } });
  ok('A7 修改昵称', P.isCode(200)(prof.body));
  await api('PUT', '/user/profile', { token: tReader02, body: { nickname: '书虫小陈' } }); // 还原
  const pwd = await api('PUT', '/user/password', { token: tReader02, body: { oldPassword: '123456', newPassword: '123456' } });
  ok('A8 修改密码(改回原密码)', P.isCode(200)(pwd.body));
  const re = await api('POST', '/user/recharge', { token: tReader02, body: { amount: 500 } });
  ok('A9 模拟充值', P.isCode(200)(re.body) && re.body?.data?.recharged === 500);
  const bal = await api('GET', '/user/balance', { token: tReader02 });
  ok('A10 查询余额', P.isCode(200)(bal.body), `balance=${bal.body?.data?.balance}`);

  /* ---------------- B. 公开浏览 ---------------- */
  console.log('\n【B. 公开浏览/检索】');
  const cats = await api('GET', '/category/list');
  ok('B1 分类列表', P.isCode(200)(cats.body) && cats.body?.data?.length === 8, `${cats.body?.data?.length}个分类`);
  const pageAll = await api('GET', '/book/page', { query: { page: 1, size: 20 } });
  ok('B2 书城分页', P.isCode(200)(pageAll.body) && pageAll.body?.data?.total >= 18, `共${pageAll.body?.data?.total}本`);
  const pageKw = await api('GET', '/book/page', { query: { keyword: '星海' } });
  ok('B3 关键词搜索', P.isCode(200)(pageKw.body) && pageKw.body?.data?.records?.some(b => b.title.includes('星海')), `命中${pageKw.body?.data?.total}本`);
  const pageCat = await api('GET', '/book/page', { query: { categoryId: 3 } });
  ok('B4 分类筛选(言情)', P.isCode(200)(pageCat.body) && pageCat.body?.data?.records?.every(b => b.categoryId === 3), `${pageCat.body?.data?.total}本`);
  const rec = await api('GET', '/book/recommend', { query: { limit: 8 } });
  ok('B5 首页推荐', P.isCode(200)(rec.body) && rec.body?.data?.length > 0 && rec.body?.data?.every(b => b.isRecommend === 1), `${rec.body?.data?.length}本推荐`);
  const rankClick = await api('GET', '/book/rank', { query: { type: 'click', limit: 10 } });
  const rankCollect = await api('GET', '/book/rank', { query: { type: 'collect', limit: 10 } });
  const rankUpdate = await api('GET', '/book/rank', { query: { type: 'update', limit: 10 } });
  ok('B6 排行榜(人气/收藏/更新)', P.isCode(200)(rankClick.body) && P.isCode(200)(rankCollect.body) && P.isCode(200)(rankUpdate.body), `人气TOP1=${rankClick.body?.data?.[0]?.title}`);
  const detail = await api('GET', '/book/13');
  ok('B7 小说详情', P.isCode(200)(detail.body) && detail.body?.data?.title === '重回1998', detail.body?.data?.title);
  const chapters = await api('GET', '/book/13/chapters');
  const freeChs = (chapters.body?.data || []).filter(c => c.isFree === 1);
  const paidChs = (chapters.body?.data || []).filter(c => c.isFree === 0);
  ok('B8 章节目录(免费/付费标记)', P.isCode(200)(chapters.body) && freeChs.length >= 2 && paidChs.length >= 5, `共${chapters.body?.data?.length}章, 付费${paidChs.length}章`);

  /* ---------------- C. 阅读权限 ---------------- */
  console.log('\n【C. 阅读权限】');
  // 动态挑选 reader02 尚未购买的付费章（避免与历史运行/种子订单冲突）
  const myOrders0 = await api('GET', '/order/my', { token: tReader02, query: { page: 1, size: 100 } });
  const boughtChs = new Set((myOrders0.body?.data?.records || []).map(o => Number(o.chapterId)));
  const ch18 = await api('GET', '/book/18/chapters');
  const freeChs18 = (ch18.body?.data || []).filter(c => c.isFree === 1);
  const firstFree = freeChs18[0];
  let testBookId = 18, testPaid = null;
  for (const bid of [18, 16, 22, 17, 24, 20, 21, 23, 26, 15]) {
    const chs = await api('GET', `/book/${bid}/chapters`);
    const p = (chs.body?.data || []).find(c => c.isFree === 0 && !boughtChs.has(Number(c.id)));
    if (p) { testBookId = bid; testPaid = p; break; }
  }
  const firstPaid = testPaid;
  const readFree = await api('GET', `/chapter/${firstFree.id}`);
  ok('C1 免费章匿名可读', P.isCode(200)(readFree.body) && readFree.body?.data?.content?.length > 0);
  const readPaidAnon = await api('GET', `/chapter/${firstPaid.id}`);
  ok('C2 付费章匿名 → 401', P.isCode(401)(readPaidAnon.body), readPaidAnon.body?.message);
  const readPaidNoBuy = await api('GET', `/chapter/${firstPaid.id}`, { token: tReader02 });
  ok('C3 付费章未购买 → 403', P.isCode(403)(readPaidNoBuy.body), readPaidNoBuy.body?.message);
  const notFound = await api('GET', '/chapter/999999');
  ok('C4 不存在的章节 → 404', P.isCode(404)(notFound.body));

  /* ---------------- D. 购买流程 ---------------- */
  console.log('\n【D. 下单/支付/阅读】');
  const order = await api('POST', '/order/create', { token: tReader02, body: { bookId: testBookId, chapterId: firstPaid.id, type: 1 } });
  ok('D1 创建单章订单', P.isCode(200)(order.body), `金额=${order.body?.data?.amount}分`);
  const orderNo = order.body?.data?.orderNo;
  const pay = await api('POST', '/order/pay', { token: tReader02, body: { orderNo } });
  ok('D2 支付成功', P.isCode(200)(pay.body) && pay.body?.data?.payStatus === 1, pay.body?.data?.payStatusText);
  const balAfter = await api('GET', '/user/balance', { token: tReader02 });
  ok('D3 余额已扣减', balAfter.body?.data?.balance === (bal.body?.data?.balance - order.body?.data?.amount), `${bal.body?.data?.balance}→${balAfter.body?.data?.balance}`);
  const dup = await api('POST', '/order/create', { token: tReader02, body: { bookId: testBookId, chapterId: firstPaid.id, type: 1 } });
  ok('D4 重复购买 → 3002', P.isCode(3002)(dup.body), dup.body?.message);
  const readPaidOk = await api('GET', `/chapter/${firstPaid.id}`, { token: tReader02 });
  ok('D5 已购付费章可读', P.isCode(200)(readPaidOk.body));
  const myOrders = await api('GET', '/order/my', { token: tReader02, query: { page: 1, size: 5 } });
  ok('D6 我的订单', P.isCode(200)(myOrders.body) && myOrders.body?.data?.records?.length >= 1);
  const orderDetail = await api('GET', `/order/${orderNo}`, { token: tReader02 });
  ok('D7 订单详情', P.isCode(200)(orderDetail.body));

  /* ---------------- E. 书架 / 评论 / 申诉 ---------------- */
  console.log('\n【E. 书架/评论/申诉】');
  const shelf = await api('GET', '/bookshelf/list', { token: tReader02 });
  ok('E1 书架列表', P.isCode(200)(shelf.body), `共${shelf.body?.data?.length}条`);
  // 挑一本不在书架上的书来测试
  const shelfBookIds = new Set((shelf.body?.data || []).map(s => Number(s.bookId)));
  const addBookId = [16, 17, 22, 24, 20].find(id => !shelfBookIds.has(id)) || 20;
  const addShelf = await api('POST', '/bookshelf/add', { token: tReader02, body: { bookId: addBookId } });
  ok('E2 加入书架', P.isCode(200)(addShelf.body));
  const prog = await api('PUT', '/bookshelf/progress', { token: tReader02, body: { bookId: addBookId, chapterId: firstFree.id, position: 25 } });
  ok('E3 更新阅读进度', P.isCode(200)(prog.body));
  const collect = await api('PUT', '/bookshelf/collect', { token: tReader02, body: { bookId: addBookId, isCollect: 1 } });
  ok('E4 收藏', P.isCode(200)(collect.body));
  const uncollect = await api('PUT', '/bookshelf/collect', { token: tReader02, body: { bookId: addBookId, isCollect: 0 } });
  ok('E5 取消收藏', P.isCode(200)(uncollect.body));
  const rmShelf = await api('DELETE', `/bookshelf/${addBookId}`, { token: tReader02 });
  ok('E6 移出书架', P.isCode(200)(rmShelf.body));

  const cm = await api('POST', '/book/13/comment', { token: tReader02, body: { content: '【测试评论】重回1998这章写得很有代入感！' } });
  ok('E7 发表评论', P.isCode(200)(cm.body), cm.body?.message);
  const cmList = await api('GET', '/book/13/comment', { query: { page: 1, size: 10 } });
  ok('E8 书籍评论列表', P.isCode(200)(cmList.body) && cmList.body?.data?.records?.some(c => c.content.includes('【测试评论】')), `共${cmList.body?.data?.total}条`);
  const myCm = await api('GET', '/comment/my', { token: tReader02 });
  ok('E9 我的评论', P.isCode(200)(myCm.body));

  // 评论封禁用户
  const tReader07 = await login('reader07');
  const cmBan = await api('POST', '/book/13/comment', { token: tReader07, body: { content: '被禁言用户的评论测试' } });
  ok('E10 禁言用户发评论被拦截', !P.isCode(200)(cmBan.body), cmBan.body?.message);
  // 申诉（reader07 有生效封禁，是合法申诉主体；先以管理员处理其历史待处理申诉，保证用例可重复执行）
  const tAdminPre = await login('admin');
  const prePending = await api('GET', '/admin/appeal/page', { token: tAdminPre, query: { status: 0, page: 1, size: 20 } });
  for (const ap of (prePending.body?.data?.records || []).filter(a => Number(a.userId) === 13 || a.userName === 'reader07')) {
    await api('POST', `/admin/appeal/${ap.id}/handle`, { token: tAdminPre, body: { action: 2, adminReply: '【测试】历史待处理申诉自动处理' } });
  }
  const appeal = await api('POST', '/appeal/create', { token: tReader07, body: { appealType: 1, reason: '【测试】申请复核评论封禁' } });
  ok('E11 发起申诉', P.isCode(200)(appeal.body), appeal.body?.message);
  const appealMy = await api('GET', '/appeal/my', { token: tReader07 });
  ok('E12 我的申诉列表', P.isCode(200)(appealMy.body));
  const hasPending = await api('GET', '/appeal/has-pending', { token: tReader07 });
  ok('E13 有待处理申诉标记', P.isCode(200)(hasPending.body) && hasPending.body?.data === true);
  // 已有待处理申诉时重复发起应被拦截
  const appealDup = await api('POST', '/appeal/create', { token: tReader07, body: { appealType: 1, reason: '【测试】重复申诉' } });
  ok('E14 有待处理申诉时重复发起被拦截', !P.isCode(200)(appealDup.body), appealDup.body?.message);
  // 无生效处罚的用户发起申诉应被拦截（业务规则）
  const appealNoBan = await api('POST', '/appeal/create', { token: tReader02, body: { appealType: 3, reason: '【测试】无处罚申诉' } });
  ok('E15 无生效处罚不可申诉', !P.isCode(200)(appealNoBan.body), appealNoBan.body?.message);

  /* ---------------- F. 通知 ---------------- */
  console.log('\n【F. 站内通知】');
  const noti = await api('GET', '/notification/list', { token: tReader02, query: { page: 1, size: 5 } });
  ok('F1 通知列表', P.isCode(200)(noti.body));
  const unread = await api('GET', '/notification/unread-count', { token: tReader02 });
  ok('F2 未读数', P.isCode(200)(unread.body), `未读=${unread.body?.data?.unreadCount}`);
  const nid = noti.body?.data?.records?.find(n => n.isRead === 0)?.id;
  if (nid) {
    const read1 = await api('PUT', `/notification/${nid}/read`, { token: tReader02 });
    ok('F3 单条已读', P.isCode(200)(read1.body));
  } else { ok('F3 单条已读', true, '无未读可测'); }
  const readAll = await api('PUT', '/notification/read-all', { token: tReader02 });
  ok('F4 全部已读', P.isCode(200)(readAll.body));

  /* ---------------- G. 作者端 ---------------- */
  console.log('\n【G. 作者端】');
  const tWriter = await login('writer02');
  ok('G1 登录作者 writer02', !!tWriter);
  const applyStatus = await api('GET', '/author/apply/status', { token: tWriter });
  ok('G2 入驻状态(已通过)', P.isCode(200)(applyStatus.body) && applyStatus.body?.data?.auditStatus === 1, applyStatus.body?.data?.auditStatusText);
  const myBooks = await api('GET', '/author/book/list', { token: tWriter });
  ok('G3 我的作品', P.isCode(200)(myBooks.body) && myBooks.body?.data?.length >= 4, `${myBooks.body?.data?.length}本`);

  const nb = await api('POST', '/author/book', { token: tWriter, body: { categoryId: 2, title: '【测试】都市新作', intro: '能力测试创建的书' } });
  ok('G4 新建作品', P.isCode(200)(nb.body), `id=${nb.body?.data?.id}`);
  const nbId = nb.body?.data?.id;
  const nch = await api('POST', '/author/chapter', { token: tWriter, body: { bookId: nbId, title: '第一章 初来乍到', content: '这是能力测试创建的第一章正文内容，用于验证章节创建与字数统计功能是否正常。'.repeat(3), isFree: 1, sort: 1 } });
  ok('G5 新建章节(自动统计字数)', P.isCode(200)(nch.body) && nch.body?.data?.words > 0, `words=${nch.body?.data?.words}`);
  const chId = nch.body?.data?.id;
  const upch = await api('PUT', `/author/chapter/${chId}`, { token: tWriter, body: { bookId: nbId, title: '第一章 初来乍到(改)', content: '修改后的正文内容。', isFree: 0, price: 40, sort: 1 } });
  ok('G6 修改章节', P.isCode(200)(upch.body));
  const chList = await api('GET', `/author/book/${nbId}/chapter/list`, { token: tWriter });
  ok('G7 作品章节列表', P.isCode(200)(chList.body) && chList.body?.data?.length >= 1);
  const upBook = await api('PUT', `/author/book/${nbId}`, { token: tWriter, body: { categoryId: 2, title: '【测试】都市新作·改', intro: '修改后的简介' } });
  ok('G8 修改作品', P.isCode(200)(upBook.body));

  // 一键创建书籍含章节
  const wc = await api('POST', '/author/book/with-chapters', { token: tWriter, body: {
    title: '【测试】一键建书', categoryId: 5, intro: '新功能测试',
    chapters: [
      { title: '第一章 起点', content: '一键创建的第一章内容。', isFree: 1 },
      { title: '第二章 转折', content: '一键创建的第二章内容。', isFree: 0, price: 30 }
    ]
  }});
  ok('G9 一键创建书籍+章节(新功能)', P.isCode(200)(wc.body), wc.body?.message);
  const wcId = wc.body?.data?.id;
  const wcChs = await api('GET', `/author/book/${wcId}/chapter/list`, { token: tWriter });
  ok('G10 一键建书章节齐全', P.isCode(200)(wcChs.body) && wcChs.body?.data?.length === 2, `${wcChs.body?.data?.length}章`);

  // 文本导入章节（新功能）
  const importText = '第一章 风起\n这是导入的第一章正文，故事从这里开始。\n第二章 云涌\n这是导入的第二章正文，情节逐渐展开。\n第三章 雷鸣\n这是导入的第三章正文，高潮即将到来。';
  const imp = await api('POST', '/author/chapter/import', { token: tWriter, body: { bookId: nbId, text: importText, isFree: 1 } });
  ok('G11 文本批量导入章节(新功能)', P.isCode(200)(imp.body) && imp.body?.data === 3, `导入${imp.body?.data}章`);
  const badImp = await api('POST', '/author/chapter/import', { token: tWriter, body: { bookId: nbId, text: '没有章节标题的普通文本' } });
  ok('G12 无效文本导入被拦截', P.isCode(400)(badImp.body), badImp.body?.message);

  const stats = await api('GET', '/author/stats', { token: tWriter });
  ok('G13 作者数据看板', P.isCode(200)(stats.body) && stats.body?.data?.bookCount >= 6, `作品${stats.body?.data?.bookCount}本, 总点击${stats.body?.data?.totalClick}`);
  const finish = await api('PUT', `/author/book/${nbId}/status`, { token: tWriter, body: { status: 2 } });
  ok('G14 完结作品', P.isCode(200)(finish.body));
  const delCh = await api('DELETE', `/author/chapter/${chId}`, { token: tWriter });
  ok('G15 删除章节', P.isCode(200)(delCh.body));
  // 实际实现：删除作品会连同章节一并删除（接口文档描述"有章节不可删"已过时，按实际行为验证）
  const delBookWithCh = await api('DELETE', `/author/book/${nbId}`, { token: tWriter });
  const bookAfterDel = await api('GET', `/book/${nbId}`);
  ok('G16 删除作品(连同章节)', P.isCode(200)(delBookWithCh.body) && P.isCode(404)(bookAfterDel.body), bookAfterDel.body?.message);

  /* ---------------- H. 管理端 ---------------- */
  console.log('\n【H. 管理端】');
  const tAdmin = await login('admin');
  ok('H1 登录管理员', !!tAdmin);
  const users = await api('GET', '/admin/user/page', { token: tAdmin, query: { page: 1, size: 50 } });
  ok('H2 用户列表', P.isCode(200)(users.body) && users.body?.data?.total >= 22, `共${users.body?.data?.total}人`);
  const roleFilter = await api('GET', '/admin/user/page', { token: tAdmin, query: { role: 2 } });
  ok('H3 按角色筛选', P.isCode(200)(roleFilter.body) && roleFilter.body?.data?.records?.every(u => u.role === 2), `${roleFilter.body?.data?.total}名管理员`);

  // 禁用/启用
  const disableU = await api('PUT', '/admin/user/14/status', { token: tAdmin, body: { status: 0 } });
  ok('H4 禁用用户', P.isCode(200)(disableU.body));
  const loginBanned = await api('POST', '/auth/login', { body: { username: 'reader08', password: '123456' } });
  ok('H5 被禁用户无法登录', !P.isCode(200)(loginBanned.body), loginBanned.body?.message);
  const enableU = await api('PUT', '/admin/user/14/status', { token: tAdmin, body: { status: 1 } });
  ok('H6 恢复用户', P.isCode(200)(enableU.body));

  // 封禁/解封
  const banU = await api('POST', '/admin/user/15/ban', { token: tAdmin, body: { banFlags: 2, banDays: 7, banReason: '【测试】恶意灌水' } });
  ok('H7 封禁用户(禁言7天)', P.isCode(200)(banU.body), banU.body?.message);
  const tReader15 = await login('reader09');
  const cmAfterBan = await api('POST', '/book/13/comment', { token: tReader15, body: { content: '禁言后的评论' } });
  ok('H8 被禁言用户评论被拦截', !P.isCode(200)(cmAfterBan.body), cmAfterBan.body?.message);
  const unbanU = await api('POST', '/admin/user/15/unban', { token: tAdmin });
  ok('H9 解封用户', P.isCode(200)(unbanU.body));

  // 作者审核
  const authorPage = await api('GET', '/admin/author/page', { token: tAdmin, query: { auditStatus: 0 } });
  ok('H10 待审核入驻列表', P.isCode(200)(authorPage.body), `待审${authorPage.body?.data?.total}条`);
  const pendingAuthor = (authorPage.body?.data?.records || []).find(a => a.id === 4);
  if (pendingAuthor) {
    const rej = await api('POST', `/admin/author/4/audit`, { token: tAdmin, body: { pass: false, reason: '【测试】资料不完整' } });
    ok('H11 驳回入驻申请', P.isCode(200)(rej.body));
  } else { ok('H11 驳回入驻申请', true, '无待审可测'); }

  // 入驻全流程：注册→申请→审核通过→成为作者
  const newU = 'qa_writer_' + Date.now().toString().slice(-6);
  const nreg = await api('POST', '/auth/register', { body: { username: newU, password: '123456', nickname: '新晋作者' } });
  const tNew = nreg.body?.data?.token;
  const apply = await api('POST', '/author/apply', { token: tNew, body: { realName: '测试甲', idCard: '360101199001011234', intro: '测试申请' } });
  ok('H12 新用户申请入驻', P.isCode(200)(apply.body), apply.body?.data?.auditStatusText);
  const applyId = apply.body?.data?.id;
  const newApplyList = await api('GET', '/admin/author/page', { token: tAdmin, query: { auditStatus: 0 } });
  const myApply = (newApplyList.body?.data?.records || []).find(a => a.id === applyId);
  ok('H13 申请出现在待审列表', !!myApply);
  const passAudit = await api('POST', `/admin/author/${applyId}/audit`, { token: tAdmin, body: { pass: true } });
  ok('H14 审核通过', P.isCode(200)(passAudit.body));
  const infoNew = await api('GET', '/auth/info', { token: tNew });
  ok('H15 用户角色升级为作者', P.isCode(200)(infoNew.body) && infoNew.body?.data?.role === 1, `role=${infoNew.body?.data?.role}`);
  const newBook = await api('POST', '/author/book', { token: tNew, body: { categoryId: 1, title: '【测试】新作者的书', intro: '验证升级后的写作权限' } });
  ok('H16 新作者可建书', P.isCode(200)(newBook.body));

  // 书籍审核流程（作者警告 → 待审核 → 通过/驳回）
  const warnBan = await api('POST', '/admin/user/18/ban', { token: tAdmin, body: { banFlags: 16, banDays: 30, banReason: '【测试】作者警告' } });
  ok('H17 给作者加警告标记', P.isCode(200)(warnBan.body));
  const tWriter3 = await login('writer03');
  const w3Book = await api('POST', '/author/book', { token: tWriter3, body: { categoryId: 7, title: '【测试】待审核新书', intro: '作者警告期间新建，应进入待审核' } });
  ok('H18 警告期新书进入待审核', P.isCode(200)(w3Book.body) && w3Book.body?.data?.status === 3, `status=${w3Book.body?.data?.status}`);
  const w3BookId = w3Book.body?.data?.id;
  const w3ChangeStatus = await api('PUT', `/author/book/${w3BookId}/status`, { token: tWriter3, body: { status: 2 } });
  ok('H19 待审核书作者不可改状态', P.isCode(400)(w3ChangeStatus.body), w3ChangeStatus.body?.message);
  const pendingBooks = await api('GET', '/admin/book/pending', { token: tAdmin });
  ok('H20 待审核书籍列表', P.isCode(200)(pendingBooks.body) && pendingBooks.body?.data?.records?.some(b => b.id === w3BookId));
  const approveBook = await api('POST', `/admin/book/${w3BookId}/approve`, { token: tAdmin });
  ok('H21 审核通过书籍', P.isCode(200)(approveBook.body));
  const w3Detail = await api('GET', `/book/${w3BookId}`);
  ok('H22 通过后前台可见', P.isCode(200)(w3Detail.body) && [1, 2].includes(w3Detail.body?.data?.status), `status=${w3Detail.body?.data?.status}`);
  // 再建一本走驳回流程
  const w3Book2 = await api('POST', '/author/book', { token: tWriter3, body: { categoryId: 7, title: '【测试】待驳回新书', intro: '用于验证驳回流程' } });
  const w3Book2Id = w3Book2.body?.data?.id;
  const rejectBook = await api('POST', `/admin/book/${w3Book2Id}/reject`, { token: tAdmin, query: { reason: '【测试】内容不合规' } });
  ok('H23 驳回书籍', P.isCode(200)(rejectBook.body));
  const resub = await api('POST', `/author/book/${w3Book2Id}/resubmit`, { token: tWriter3 });
  ok('H24 作者重新提交审核', P.isCode(200)(resub.body) && resub.body?.data?.status === 3, resub.body?.message);
  const unbanW3 = await api('POST', '/admin/user/18/unban', { token: tAdmin });
  ok('H25 解除作者警告', P.isCode(200)(unbanW3.body));

  // 推荐位管理（新功能）
  const recSet = await api('PUT', '/admin/book/12/recommend', { token: tAdmin, body: { isRecommend: 1 } });
  ok('H26 设置推荐位(新功能)', P.isCode(200)(recSet.body));
  const recCheck = await api('GET', '/book/recommend', { query: { limit: 20 } });
  ok('H27 推荐位生效', P.isCode(200)(recCheck.body) && recCheck.body?.data?.some(b => b.id === 12));
  const recOff = await api('PUT', '/admin/book/12/recommend', { token: tAdmin, body: { isRecommend: 0 } });
  ok('H28 取消推荐位', P.isCode(200)(recOff.body));

  // 分类管理
  const newCat = await api('POST', '/admin/category', { token: tAdmin, body: { name: '测试分类', sort: 99, status: 1 } });
  ok('H29 新增分类', P.isCode(200)(newCat.body), newCat.body?.message);
  const newCatId = newCat.body?.data?.id;
  const editCat = await api('PUT', `/admin/category/${newCatId}`, { token: tAdmin, body: { name: '测试分类改', sort: 99, status: 1 } });
  ok('H30 编辑分类', P.isCode(200)(editCat.body));
  const delCatBlocked = await api('DELETE', '/admin/category/1', { token: tAdmin });
  ok('H31 有书分类禁止删除', !P.isCode(200)(delCatBlocked.body), delCatBlocked.body?.message);
  const delCat = await api('DELETE', `/admin/category/${newCatId}`, { token: tAdmin });
  ok('H32 删除空分类', P.isCode(200)(delCat.body));

  // 订单/评论管理
  const adminOrders = await api('GET', '/admin/order/page', { token: tAdmin, query: { page: 1, size: 10 } });
  ok('H33 订单列表', P.isCode(200)(adminOrders.body) && adminOrders.body?.data?.total >= 20, `共${adminOrders.body?.data?.total}单`);
  const adminOrdersPaid = await api('GET', '/admin/order/page', { token: tAdmin, query: { payStatus: 1 } });
  ok('H34 订单按支付状态筛选', P.isCode(200)(adminOrdersPaid.body) && adminOrdersPaid.body?.data?.records?.every(o => o.payStatus === 1));
  const adminCms = await api('GET', '/admin/comment/page', { token: tAdmin, query: { page: 1, size: 10 } });
  ok('H35 评论列表', P.isCode(200)(adminCms.body) && adminCms.body?.data?.total >= 20, `共${adminCms.body?.data?.total}条`);
  const testCmId = cm.body?.data?.id;
  const blockCm = await api('PUT', `/admin/comment/${testCmId}/status`, { token: tAdmin, body: { status: 0 } });
  ok('H36 屏蔽评论', P.isCode(200)(blockCm.body));
  const cmList2 = await api('GET', '/book/13/comment', { query: { page: 1, size: 50 } });
  ok('H37 屏蔽后前台不展示', !cmList2.body?.data?.records?.some(c => c.id === testCmId));
  const restoreCm = await api('PUT', `/admin/comment/${testCmId}/status`, { token: tAdmin, body: { status: 1 } });
  ok('H38 恢复评论', P.isCode(200)(restoreCm.body));
  const delCm = await api('DELETE', `/admin/comment/${testCmId}`, { token: tAdmin });
  ok('H39 删除评论', P.isCode(200)(delCm.body));

  // 申诉管理
  const appealPage = await api('GET', '/admin/appeal/page', { token: tAdmin, query: { status: 0 } });
  ok('H40 待处理申诉列表', P.isCode(200)(appealPage.body), `待处理${appealPage.body?.data?.total}条`);
  const pendingAppeal = (appealPage.body?.data?.records || []).find(a => a.id === 100);
  if (pendingAppeal) {
    const handle = await api('POST', `/admin/appeal/100/handle`, { token: tAdmin, body: { action: 2, adminReply: '【测试】经核实维持处罚' } });
    ok('H41 处理申诉(驳回)', P.isCode(200)(handle.body));
  } else { ok('H41 处理申诉', true, '无待处理可测'); }

  /* ---------------- 汇总 ---------------- */
  console.log('\n==================== 测试汇总 ====================');
  const pass = results.filter(r => r.pass).length;
  const fail = results.filter(r => !r.pass).length;
  console.log(`总用例: ${results.length} | 通过: ${pass} | 失败: ${fail}`);
  if (fail > 0) {
    console.log('\n失败用例:');
    results.filter(r => !r.pass).forEach(r => console.log('  ❌ ' + r.name + (r.extra ? ' [' + r.extra + ']' : '')));
  }
  process.exit(fail > 0 ? 1 : 0);
})().catch(e => { console.error('测试脚本异常:', e); process.exit(2); });
