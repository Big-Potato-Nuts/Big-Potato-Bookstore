/* ============================================================
 * 在线小说书城 · 演示数据生成器
 * 生成 Potato/sql/seed_demo_data.sql
 * 运行: node generate_seed.js
 * 说明: 使用固定 ID 段(用户8+, 作者5+, 书籍10+, 章节100+, 评论/书架/订单/通知/申诉100+)避免与现有数据冲突
 * ============================================================ */
const fs = require('fs');
const path = require('path');

// 固定种子，保证可复现
let seq = 1;
function rnd(n) { // 0..n-1
  seq = (seq * 9301 + 49297) % 233280;
  return Math.floor((seq / 233280) * n);
}
function rndRange(a, b) { return a + rnd(b - a + 1); }
function pick(arr) { return arr[rnd(arr.length)]; }

const PWD = '$2b$10$SOEgXYHMOmNDJ4Pb6503d.onS2o5JOZKfAonpLRcXFBA.K3AwZO.a'; // 123456

/* ---------- 用户 ---------- */
const users = [
  { id: 8,  username: 'reader02', nickname: '书虫小陈',  role: 0, balance: 8000,  status: 1, ban_flags: 0 },
  { id: 9,  username: 'reader03', nickname: '夜读猫',    role: 0, balance: 15000, status: 1, ban_flags: 0 },
  { id: 10, username: 'reader04', nickname: '桃桃看书',  role: 0, balance: 3000,  status: 1, ban_flags: 0 },
  { id: 11, username: 'reader05', nickname: '风中旅人',  role: 0, balance: 20000, status: 1, ban_flags: 0 },
  { id: 12, username: 'reader06', nickname: '追更君',    role: 0, balance: 5000,  status: 1, ban_flags: 0 },
  { id: 13, username: 'reader07', nickname: '南巷清风',  role: 0, balance: 12000, status: 1, ban_flags: 2 }, // 评论封禁示例
  { id: 14, username: 'reader08', nickname: '布衣书生',  role: 0, balance: 6000,  status: 1, ban_flags: 0 },
  { id: 15, username: 'reader09', nickname: '拾光旅人',  role: 0, balance: 9000,  status: 1, ban_flags: 0 },
  { id: 16, username: 'reader10', nickname: '橘子汽水',  role: 0, balance: 18000, status: 1, ban_flags: 0 },
  { id: 17, username: 'writer02', nickname: '林间书客',  role: 1, balance: 0,     status: 1, ban_flags: 0 },
  { id: 18, username: 'writer03', nickname: '江畔听雨',  role: 1, balance: 0,     status: 1, ban_flags: 0 },
  { id: 19, username: 'writer04', nickname: '半盏清茶',  role: 1, balance: 0,     status: 1, ban_flags: 0 },
  { id: 20, username: 'reader11', nickname: '山间明月',  role: 0, balance: 1000,  status: 1, ban_flags: 0 },
  { id: 21, username: 'reader12', nickname: '东篱采菊',  role: 0, balance: 7000,  status: 1, ban_flags: 0 },
  { id: 22, username: 'reader13', nickname: '雪落无声',  role: 0, balance: 26000, status: 1, ban_flags: 0 }
];

/* ---------- 作者 ---------- */
const authors = [
  { id: 5, user_id: 17, real_name: '陈望舒', id_card: '360101199203154328', intro: '擅长东方玄幻与古典言情，文风热血细腻兼具。', audit_status: 1 },
  { id: 6, user_id: 18, real_name: '江离',   id_card: '440101198807126541', intro: '悬疑推理爱好者，专注反转与氛围营造。',     audit_status: 1 },
  { id: 7, user_id: 19, real_name: '李清和', id_card: '510101199501238716', intro: '都市与历史题材写手，细节考据控。',         audit_status: 1 }
];

/* ---------- 书籍: [id, author_id, category_id, title, status, is_recommend, click, collect, intro, chapterTitles] ---------- */
const GENRE_PARA = {
  玄幻: ['灵气复苏之后，这片大陆的格局被彻底改写。', '古老宗门重现人间，藏于云海深处的传承逐渐苏醒。', '少年握紧手中残破的剑，眼中映着满天星斗。', '强敌环伺，唯有不断破境，方能护住身后之人。'],
  都市: ['都市霓虹之下，藏着一个不为人知的江湖。', '写字楼里的西装少年，白天上班，夜晚却有另一重身份。', '命运的车轮悄然转动，一次偶遇改变了所有人的轨迹。', '商场如战场，每个决定都牵动着无数人的命运。'],
  言情: ['春风拂过檐角，檐下的灯笼轻轻晃动。', '她站在长街尽头，回望那一眼，从此山水迢迢。', '旧时光里的约定，被一封信重新翻开。', '有些人一旦错过，便是一生；有些情，藏在岁月深处。'],
  悬疑: ['雨夜，小镇的钟楼敲响第十一下。', '档案室里那本泛黄的卷宗，指向一个尘封多年的名字。', '现场没有留下任何痕迹，除了那枚铜质的纽扣。', '真相像雾一样浓，越接近，越看不清。'],
  科幻: ['深空之下，人类的脚步从未停歇。', '量子跃迁引擎的嗡鸣声中，舰队驶向未知坐标。', 'AI 的代码里，藏着一道不属于任何人的签名。', '星门的另一边，是什么在等待着他们？'],
  历史: ['庙堂之上，暗流涌动。', '一纸诏书，改变了多少人的命运。', '史书只记大事，而小人物的悲欢同样值得书写。', '刀光剑影里，谁在守护这座城池最后的灯火？'],
  仙侠: ['青云之上，仙路漫漫。', '山中无甲子，寒尽不知年。', '飞剑破空，惊起漫天流云。', '修道路上，一步一劫，一步一果。'],
  军事: ['边境线上，烽火连天。', '钢铁洪流碾过残破的阵地，硝烟遮蔽了朝阳。', '一支小队的生死抉择，牵动着整个战局。', '和平年代，总有人在看不见的地方负重前行。']
};
const OPENERS = ['夜色渐沉，', '天刚蒙蒙亮，', '风声掠过耳畔，', '推开门的那一刻，', '时间仿佛凝滞，', '一切从那个清晨开始，'];
const CLOSERS = ['远处传来一声闷响，新的篇章就此揭开。', '他抬起头，目光落向更远的地方。', '故事才刚刚开始。', '而这一切，不过是个序章。', '命运的齿轮，已然转动。'];

const books = [
  { id: 10, a: 5, c: 1, t: '九霄神帝', st: 1, rec: 1, click: 8560, collect: 320, intro: '少年楚渊身负九霄神印，自玄天宗外门崛起，一路逆天改命，终成一代神帝。', chs: ['废柴觉醒', '入门考核', '一鸣惊人', '宗门大比', '秘境风云', '仇家上门', '九霄神印', '绝境突破', '名动玄天', '神帝之路'] },
  { id: 11, a: 6, c: 1, t: '万古第一剑', st: 2, rec: 0, click: 4230, collect: 185, intro: '剑冢枯骨生新芽，少年持一柄断剑，问剑万古，斩尽诸天。', chs: ['剑冢遗孤', '断剑重铸', '初试锋芒', '问剑台', '剑意初成', '风雪论剑', '一剑破万法', '万古第一', '剑指苍穹', '终章·归鞘'] },
  { id: 12, a: 3, c: 1, t: '荒天记', st: 1, rec: 0, click: 5180, collect: 96, intro: '荒域大泽，少年叶荒自蛮荒深处走出，携一株青莲，踏碎荒天。', chs: ['蛮荒少年', '青莲伴身', '大泽试炼', '荒兽之潮', '血战孤峰', '古族遗秘', '青莲绽放', '问鼎荒域'] },
  { id: 13, a: 7, c: 2, t: '重回1998', st: 1, rec: 1, click: 12400, collect: 640, intro: '一觉醒来，陈默重回1998年。这一次，他要把错过的人、错过的时代，统统握在手里。', chs: ['重回九八', '第一桶金', '南下风云', '时代浪潮', '暗流涌动', '商海浮沉', '情定三生', '鹏程万里', '岁月如歌', '再续前缘'] },
  { id: 14, a: 3, c: 2, t: '都市之最强狂兵', st: 2, rec: 0, click: 9100, collect: 210, intro: '五年前，他被逐出家门；五年后，王者归来，搅动一城风云。', chs: ['王者归来', '初见锋芒', '一怒为红颜', '黑道风云', '豪门夜宴', '旧敌重逢', '横扫千军', '尘埃落定'] },
  { id: 15, a: 5, c: 3, t: '春风不渡', st: 1, rec: 1, click: 13800, collect: 980, intro: '他是权倾朝野的摄政王，她是寄人篱下的孤女。一纸婚约，将两人的命运绑在一起。', chs: ['初入王府', '暗夜惊变', '一纸婚约', '步步惊心', '春风几度', '情深缘浅', '宫墙内外', '愿者上钩', '山雨欲来', '此情可待'] },
  { id: 16, a: 7, c: 3, t: '许你一世长安', st: 2, rec: 0, click: 7200, collect: 450, intro: '青梅竹马，两小无猜。他说，待我功成名就，许你一世长安。', chs: ['青梅竹马', '鸿雁传书', '一朝别离', '长安落雪', '千里赴约', '执子之手', '盛世婚礼', '白头偕老'] },
  { id: 17, a: 6, c: 3, t: '山月不知心底事', st: 1, rec: 0, click: 6100, collect: 320, intro: '小镇书店的老板娘，与归乡的钢琴师，在旧时光里相遇。山月不知心底事，水风空落眼前花。', chs: ['旧书店', '归乡人', '一曲琴音', '雨夜长谈', '往事如烟', '心门微启', '山月照人', '此心安处'] },
  { id: 18, a: 6, c: 4, t: '雾隐迷城', st: 1, rec: 0, click: 8900, collect: 260, intro: '一座常年被浓雾笼罩的城市，接连出现离奇失踪案。刑警队长顾砚，一步步逼近迷雾深处的真相。', chs: ['雾城谜案', '消失的房客', '钟楼魅影', '死者来信', '暗巷追踪', '真相裂缝', '迷雾深处', '黎明之前', '最后的证词', '雾散天晴'] },
  { id: 19, a: 5, c: 4, t: '第七个目击者', st: 2, rec: 1, click: 10300, collect: 380, intro: '深夜的便利店，一场抢劫案，七个目击者，却给出了七份完全不同的证词。', chs: ['深夜便利店', '第七个人', '证词疑云', '时间线', '失踪的监控', '谎言与真相', '局中局', '尘埃落定'] },
  { id: 20, a: 6, c: 5, t: '银河猎手', st: 1, rec: 0, click: 5600, collect: 140, intro: '在星际赏金猎人执照过期的最后一天，老猎人陆遥接下了改变他一生的委托。', chs: ['赏金猎人', '过期执照', '神秘委托', '跃迁风暴', '失落的航线', '猎手本能', '星海追逐', '归途'] },
  { id: 21, a: 7, c: 6, t: '大宋风流', st: 1, rec: 1, click: 9800, collect: 520, intro: '穿越大宋，且看一个现代灵魂如何在汴京的市井与朝堂之间，活出风流写意的一生。', chs: ['梦回汴京', '市井烟火', '初入太学', '词动京城', '朝堂风云', '变法暗流', '大江东去', '风流不减', '山河万里', '此间少年'] },
  { id: 22, a: 5, c: 6, t: '汉家儿郎', st: 2, rec: 0, click: 6600, collect: 240, intro: '犯我强汉者，虽远必诛。一个边关小卒，一步步成长为守护家国的百战将军。', chs: ['边关小卒', '第一战', '狼烟四起', '孤城守将', '千里奔袭', '封狼居胥', '家国天下', '汉魂永存'] },
  { id: 23, a: 6, c: 7, t: '青云之上', st: 1, rec: 1, click: 11200, collect: 470, intro: '青云宗外门弟子秦川，偶得上古剑仙传承，自此青云之上，再无对手。', chs: ['外门弟子', '上古传承', '青云试剑', '一鸣惊座', '仙路漫漫', '雷劫淬体', '名震青云', '问道长生', '剑仙遗秘', '青云之巅'] },
  { id: 24, a: 3, c: 7, t: '剑来问道', st: 1, rec: 0, click: 7800, collect: 190, intro: '一柄凡铁剑，一个拾剑少年。问道之路，始于足下。', chs: ['拾剑少年', '铁剑无声', '问道山门', '初悟剑意', '山中岁月', '剑鸣千里', '一战成名', '问道长生'] },
  { id: 25, a: 3, c: 8, t: '烽火连城', st: 1, rec: 0, click: 4700, collect: 130, intro: '边城告急，援军断绝。守城将军沈烈，用最后的三千将士，写下烽火中的誓言。', chs: ['边城告急', '三千将士', '断粮之夜', '血战三日', '火线传信', '黎明之前', '援军终至', '烽火永存'] },
  { id: 26, a: 7, c: 8, t: '孤军突击', st: 2, rec: 1, click: 6900, collect: 280, intro: '敌后孤军，绝境求生。一支特种小队，在敌占区打出了教科书般的穿插突击。', chs: ['深夜空降', '敌后潜伏', '情报突袭', '围点打援', '绝境突围', '孤军奋战', '黎明突袭', '凯旋'] }
];

/* ---------- 评论模板 ---------- */
const COMMENT_POOL = [
  '开头就很吸引人，一口气看到最新章，催更！',
  '设定不错，就是更新有点慢，作者加油！',
  '人物刻画很到位，主角终于成长起来了。',
  '剧情渐入佳境，伏笔埋得真好。',
  '这本书的节奏我很喜欢，不拖沓。',
  '看到半夜停不下来，明天还要上班呢……',
  '配角也写得很立体，不是工具人。',
  '这章转折有点出乎意料，期待后续。',
  '文笔细腻，场景描写很有画面感。',
  '主角智商在线，爽点密集，好评。',
  '收藏了，养肥再看。',
  '建议作者多更新几章，根本不够看！',
  '这个设定让我想起以前看过的一本书，但写得更好。',
  '感情线不突兀，水到渠成。',
  '细节控表示很满意，伏笔都能回收。'
];

/* ---------- 评论用户池 ---------- */
const commentUsers = [8, 9, 10, 11, 12, 14, 15, 16, 20, 21, 22];

/* ---------- 生成章节正文 ---------- */
function genContent(book, chTitle, idx) {
  const genre = CAT_NAME[book.c];
  const paras = [];
  paras.push(pick(OPENERS) + pick(GENRE_PARA[genre]));
  paras.push(`这一章《${chTitle}》，局势悄然生变。${pick(GENRE_PARA[genre])}${pick(GENRE_PARA[genre])}`);
  paras.push(pick(GENRE_PARA[genre]) + pick(GENRE_PARA[genre]));
  paras.push(`书中的主角回望来路，心中百感交集。${pick(GENRE_PARA[genre])}${pick(GENRE_PARA[genre])}`);
  paras.push(pick(CLOSERS));
  return paras.join('');
}

/* ---------- 组装 SQL ---------- */
const lines = [];
const ts = (daysAgo, hour) => `DATE_SUB(NOW(), INTERVAL ${daysAgo} DAY) + INTERVAL ${hour} HOUR`;
function day() { return rndRange(5, 40); }
function hour() { return rndRange(8, 22); }

lines.push('-- ============================================================');
lines.push('-- 在线小说书城 · 演示数据填充脚本（由 generate_seed.js 生成）');
lines.push('-- 密码均为 123456；固定 ID 段避免与现有数据冲突');
lines.push('-- ============================================================');
lines.push('SET NAMES utf8mb4;');

/* 用户 */
const userVals = users.map(u =>
  `(${u.id}, '${u.username}', '${PWD}', '${u.nickname}', ${u.role}, ${u.balance}, ${u.status}, ${u.ban_flags}, NULL, '${u.username} 是本次演示填充的测试账号', ${ts(day(), hour())})`
).join(',\n');
lines.push(`INSERT INTO \`user\` (id, username, password, nickname, role, balance, status, ban_flags, ban_expire_time, ban_reason, create_time) VALUES\n${userVals};`);

/* 作者 */
lines.push(`INSERT INTO author (id, user_id, real_name, id_card, intro, audit_status, audit_time, create_time) VALUES`);
const authorVals = authors.map(a =>
  `(${a.id}, ${a.user_id}, '${a.real_name}', '${a.id_card}', '${a.intro}', ${a.audit_status}, ${ts(day(), hour())}, ${ts(day() + 2, hour())})`
).join(',\n');
lines.push(`${authorVals};`);

/* 分类名映射 */
const CAT_NAME = { 1: '玄幻', 2: '都市', 3: '言情', 4: '悬疑', 5: '科幻', 6: '历史', 7: '仙侠', 8: '军事' };

/* 书籍 */
lines.push(`INSERT INTO book (id, author_id, category_id, title, intro, status, is_recommend, click_count, collect_count, create_time, update_time) VALUES`);
const bookVals = books.map(b => {
  const cd = day();
  const ud = rndRange(0, 12);
  return `(${b.id}, ${b.a}, ${b.c}, '${b.t}', '${b.intro}', ${b.st}, ${b.rec}, ${b.click}, ${b.collect}, ${ts(cd, hour())}, ${ts(ud, hour())})`;
}).join(',\n');
lines.push(`${bookVals};`);

/* 章节 */
const chapterRows = [];
let chId = 100;
books.forEach(b => {
  const chCount = b.chs.length;
  b.chs.forEach((title, i) => {
    const sort = i + 1;
    const isFree = i < 2 ? 1 : 0;
    const price = isFree ? 0 : (rndRange(3, 8) * 10);
    const content = genContent(b, title, i);
    const words = content.length;
    const readCount = Math.floor(b.click * rndRange(3, 12) / 100);
    const chapterRowsLocal = `(${chId}, ${b.id}, '第${sort}章 ${title}', '${content}', ${isFree}, ${price}, ${sort}, ${words}, ${readCount}, 0, ${ts(rndRange(0, 20), hour())}, ${ts(0, hour())})`;
    chapterRows.push(chapterRowsLocal);
    b._chapters = b._chapters || [];
    b._chapters.push({ id: chId, sort, isFree, price, title });
    chId++;
  });
});
lines.push(`INSERT INTO chapter (id, book_id, title, content, is_free, price, sort, words, read_count, pay_count, create_time, update_time) VALUES\n${chapterRows.join(',\n')};`);

/* 评论 */
const comments = [];
let cmId = 100;
books.forEach(b => {
  const n = rndRange(2, 4);
  for (let i = 0; i < n; i++) {
    const uid = pick(commentUsers);
    comments.push(`(${cmId}, ${uid}, ${b.id}, '《${b.t}》${pick(COMMENT_POOL)}', 1, ${ts(rndRange(0, 30), hour())})`);
    cmId++;
  }
});
// 给书1(星海征途)再补几条
[8, 11, 16, 22].forEach(uid => {
  comments.push(`(${cmId}, ${uid}, 1, '《星海征途》${pick(COMMENT_POOL)}', 1, ${ts(rndRange(0, 15), hour())})`);
  cmId++;
});
lines.push(`INSERT INTO comment (id, user_id, book_id, content, status, create_time) VALUES\n${comments.join(',\n')};`);

/* 书架：读者每人收藏 3-6 本书，带阅读进度 */
const shelfRows = [];
let shId = 100;
const readers = [8, 9, 10, 11, 12, 14, 15, 16, 20, 21, 22];
readers.forEach(uid => {
  const n = rndRange(3, 6);
  const chosen = [];
  while (chosen.length < n) {
    const b = pick(books);
    if (!chosen.includes(b.id)) chosen.push(b.id);
  }
  chosen.forEach(bid => {
    const book = books.find(x => x.id === bid);
    const ch = pick(book._chapters);
    const isCollect = rnd(100) < 70 ? 1 : 0;
    shelfRows.push(`(${shId}, ${uid}, ${bid}, ${ch.id}, ${rndRange(0, 90)}, ${isCollect}, ${ts(rndRange(0, 20), hour())}, ${ts(rndRange(5, 40), hour())})`);
    shId++;
  });
});
lines.push(`INSERT INTO bookshelf (id, user_id, book_id, read_chapter_id, read_position, is_collect, update_time, create_time) VALUES\n${shelfRows.join(',\n')};`);

/* 订单：一部分已支付（单章+整本），一个待支付用于演示超时 */
const orderRows = [];
let odId = 100;
function orderNo() {
  const d = new Date();
  const p = (x) => String(x).padStart(2, '0');
  const stamp = `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`;
  return `NO${stamp}${String(rndRange(10000, 99999))}`;
}
const paidTargets = [
  { uid: 8,  bid: 13, chs: [3, 4] },   // 重回1998 付费章3、4
  { uid: 8,  bid: 15, chs: [3] },      // 春风不渡
  { uid: 9,  bid: 10, chs: [3, 4, 5] },
  { uid: 9,  bid: 23, chs: [3] },
  { uid: 10, bid: 21, chs: [3] },
  { uid: 11, bid: 19, chs: [3, 4] },
  { uid: 11, bid: 15, chs: [4] },
  { uid: 12, bid: 18, chs: [3] },
  { uid: 14, bid: 13, chs: [3] },
  { uid: 15, bid: 23, chs: [3, 4] },
  { uid: 16, bid: 10, chs: [3] },
  { uid: 16, bid: 21, chs: [3, 4, 5] },
  { uid: 20, bid: 26, chs: [3] },
  { uid: 21, bid: 15, chs: [4] },
  { uid: 22, bid: 13, chs: [5] }
];
paidTargets.forEach(t => {
  t.chs.forEach(ci => {
    const book = books.find(b => b.id === t.bid);
    const ch = book._chapters.find(c => c.sort === ci);
    if (!ch || ch.isFree) return;
    const daysAgo = rndRange(1, 25);
    const payTime = ts(daysAgo, hour());
    orderRows.push(`(${odId}, '${orderNo()}', ${t.uid}, ${t.bid}, ${ch.id}, 1, ${ch.price}, 1, ${payTime}, NULL, ${ts(daysAgo, hour())})`);
    odId++;
  });
});
// 一个待支付订单（演示超时关闭）
const book = books.find(b => b.id === 13);
const ch0 = book._chapters.find(c => c.sort === 5);
orderRows.push(`(${odId}, '${orderNo()}', 10, 13, ${ch0.id}, 1, ${ch0.price}, 0, NULL, NULL, ${ts(0, hour())})`);
lines.push(`INSERT INTO orders (id, order_no, user_id, book_id, chapter_id, type, amount, pay_status, pay_time, close_time, create_time) VALUES\n${orderRows.join(',\n')};`);

/* 通知 */
const notiRows = [];
let ntId = 100;
paidTargets.forEach(t => {
  t.chs.forEach(ci => {
    const book = books.find(b => b.id === t.bid);
    const ch = book._chapters.find(c => c.sort === ci);
    if (!ch || ch.isFree) return;
    const n = rnd(100);
    notiRows.push(`(${ntId}, ${t.uid}, 'PURCHASE', '购买成功', '你已成功购买《${book.t}》章节「第${ch.sort}章 ${ch.title}」，快去阅读吧！', ${n < 50 ? 1 : 0}, ${ts(rndRange(1, 25), hour())})`);
    ntId++;
  });
});
// 系统通知示例
[8, 11, 15].forEach((uid, i) => {
  notiRows.push(`(${ntId}, ${uid}, 'SYSTEM', '欢迎来到小说书城', '注册即送阅读券，首充更有好礼相送，快去逛逛吧！', 0, ${ts(rndRange(2, 10), hour())})`);
  ntId++;
});
lines.push(`INSERT INTO notification (id, user_id, type, title, content, is_read, create_time) VALUES\n${notiRows.join(',\n')};`);

/* 申诉 */
lines.push(`INSERT INTO user_appeal (id, user_id, appeal_type, reason, status, admin_reply, admin_id, handle_time, create_time) VALUES`);
lines.push(`(100, 13, 1, '我的评论只是正常讨论剧情，并没有违规，请管理员复核！', 0, NULL, NULL, NULL, ${ts(1, hour())}),`);
lines.push(`(101, 8, 3, '想咨询一下作者入驻的审核时长大概多久？', 2, '请以站内公告为准，一般3个工作日内完成审核。', 1, ${ts(3, hour())}, ${ts(4, hour())}),`);
lines.push(`(102, 12, 2, '账号被人盗用发布了违规内容，并非本人操作，申请解封。', 3, '已核实部分属实，减轻处罚，请规范使用账号。', 1, ${ts(2, hour())}, ${ts(6, hour())});`);

/* 修正现有脏数据 */
lines.push(`-- 修正：修复《星海征途》章节排序错乱`);
lines.push(`UPDATE chapter SET sort = 1 WHERE id = 1 AND book_id = 1;`);
lines.push(`UPDATE chapter SET sort = 2 WHERE id = 2 AND book_id = 1;`);
lines.push(`UPDATE chapter SET sort = 3 WHERE id = 3 AND book_id = 1;`);
lines.push(`-- 修正：下架联调遗留的测试书《123》，保持书城整洁`);
lines.push(`UPDATE book SET status = 0 WHERE id = 3 AND title = '123';`);

/* 输出 */
const outPath = path.join(__dirname, 'seed_demo_data.sql');
fs.writeFileSync(outPath, lines.join('\n'), 'utf8');
console.log('生成完成:', outPath, '共', lines.length, '行');
console.log('书籍:', books.length, '本; 章节:', chapterRows.length, '章; 评论:', comments.length, '条; 书架:', shelfRows.length, '条; 订单:', orderRows.length, '条; 通知:', notiRows.length, '条');
