# 同事吧功能说明文档

## 1. 文档说明

**1.1 范围**
- 本文档仅覆盖基于 RuoYi 的二次开发功能，不包含原框架自带模块。
- 覆盖端：移动端（企业微信内 H5）与后台管理端。

**1.2 目标**
- 为测试人员提供可执行的功能说明与用例依据。
- 每个功能必须细化到“页面元素级”，并说明交互后的页面变化与数据库字段级变化。

**1.3 角色定义**
- `admin`：系统管理员
- `sub_admin`：分级管理员（仅能操作本单位数据）
- `user`：普通用户
- 兼容字段：`isAdmin=1` 等同管理员身份（历史字段）

## 2. 全局规则与权限矩阵

### 2.1 访问与环境规则
- **企业微信环境限制**
  - 生产环境：非企业微信访问时，页面仅显示“禁止访问提示”，不展示业务内容。
  - 开发环境：允许使用模拟用户登录进入业务页面。
- **用户身份获取**
  - 优先使用 URL `userid` 参数（企业微信上游传递）。
  - 获取到 userid 后，必须同步用户（调用 `/mobile/forum/user/sync`）。
  - 同步成功后用户信息写入本地缓存（localStorage），后续接口携带 `wxUserid` 用于权限过滤。

### 2.2 帖子可见性规则
- **限流帖（`forum_post.is_restricted=1`）**
  - 仅作者可见；非作者访问时，详情页显示空白页（无 UI）。
  - 列表页传入 `wxUserid` 后，会在后端过滤不该显示的限流帖。
- **删除帖（`forum_post.del_flag=1`）**
  - 业务列表中不展示；仅后台管理可恢复。

### 2.3 评论可见性规则
- **限流评论（`forum_comment.is_rate_limited=1`）**
  - 仅评论作者可见；其他用户在评论列表中不可见。
  - 评论列表接口需携带 `wxUserid` 才能正确过滤。

### 2.4 发布与编辑规则
- **发帖**
  - 发帖者若处于限流状态（`forum_user.is_rate_limited=1`），新帖自动标记为限流帖（`forum_post.is_restricted=1`）。
- **编辑**
  - 仅作者可编辑，且仅允许发布后 10 分钟内编辑。
  - 编辑前需保存快照到编辑历史表（`forum_post_edit_history`）。

### 2.5 角色权限矩阵（核心）
- **管理员（admin / isAdmin=1）**
  - 可删除任意帖子/评论
  - 可锁帖/解锁、置顶/取消置顶
  - 可设置/解除帖子限流
  - 可分配用户角色、设置用户限流
  - 可查看/导出统计
- **分级管理员（sub_admin）**
  - 仅可操作**本单位**数据：
    - 删除本单位帖子/评论
    - 对本单位用户限流
    - 查看本单位统计数据
- **普通用户（user）**
  - 仅可删除自己的帖子/评论
  - 可编辑自己帖子（10 分钟内）
  - 仅可见自己被限流的帖子/评论

## 3. 移动端功能

### 3.1 登录与企业微信接入

**页面/功能入口**
- 入口页面：帖子列表、发帖页、详情页等会触发登录流程（前端初始化）。
- 权限：仅企业微信内访问；开发环境可用模拟用户。

**页面元素清单**
- 非企微环境提示页（帖子列表/详情/发帖页顶部）
  - 图标：`warning-o`
  - 文案：仅允许在企业微信内访问的提示语
  - 作用：拦截访问并提示
  - 交互：无

**交互与预期结果**
1) 企业微信内访问（URL 带 `userid`）
- 页面变化：正常进入页面；不显示“非企微提示”。
- 接口调用：
  - `GET /mobile/wxwork/user/login?userid=xxx`
  - `POST /mobile/forum/user/sync`
- 数据库变化：
  - `forum_user`
    - 新用户：插入
      - `wx_userid`、`nickname`、`avatar`、`unit`、`department`、`status=0`、`role=user`、`is_rate_limited=0`
    - 已存在用户：更新 `nickname/avatar/unit/department`
  - `forum_unit`
    - `unit` 不存在时新增
  - `forum_department`
    - `department` 不存在时在该 `unit` 下新增

2) 企业微信内访问（URL 不带 `userid`）
- 页面变化：正常进入页面（等待上层服务传入 userid）。
- 接口调用：无
- 数据库变化：无

3) 非企业微信访问（生产环境）
- 页面变化：显示“非企微环境提示页”。
- 接口调用：无
- 数据库变化：无

4) 开发环境访问（非企微）
- 页面变化：正常进入页面。
- 接口调用：
  - `POST /mobile/forum/user/sync`（使用模拟用户）
- 数据库变化：同“企业微信内访问（带 userid）”。

**异常与边界**
- `userid` 为空：提示错误，不落库。
- 登录接口失败：提示获取用户信息失败。
- 重复进入：仅更新用户信息，不重复创建记录。

### 3.2 帖子列表

**页面/功能入口**
- 移动端首页列表（路由：`/posts`）。
- 权限：企业微信环境可访问；非企微生产环境显示拦截提示。

**页面元素清单**
- 顶部导航栏
  - 标题：帖子列表
  - 搜索按钮（放大镜图标）
  - 菜单按钮（三点/更多）
- 分类筛选区
  - “全部”标签
  - 分类标签列表（横向可滚动）
- 内容区
  - 首次加载骨架屏（卡片样式）
  - 下拉刷新容器
  - 无限滚动列表
  - 帖子卡片（`PostItem`）
    - 置顶标签（当帖子置顶且未过期）
    - 标题
    - 内容摘要（支持表情渲染）
    - 纯图片帖图标/纯视频帖图标
    - 作者昵称
    - 浏览数/评论数
    - 新帖蓝条标记（本地判断）
- 底部发布按钮
  - 文案：发表帖子

**交互与预期结果**
1) 首次进入页面
- 页面变化：显示骨架屏，随后展示帖子列表。
- 接口调用：
  - `GET /mobile/forum/post/category/list`
  - `GET /mobile/forum/post/list?pageNum=1&pageSize=20[&categoryId][&wxUserid]`
- 数据库变化：无（只读列表）。

2) 点击分类标签
- 页面变化：列表清空并回到顶部，重新加载所选分类的帖子。
- 接口调用：`GET /mobile/forum/post/list`（带 `categoryId`）。
- 数据库变化：无。

3) 下拉刷新
- 页面变化：列表回到第一页并刷新数据。
- 接口调用：`GET /mobile/forum/post/list?pageNum=1...`
- 数据库变化：无。

4) 上拉加载更多
- 页面变化：在列表底部追加下一页帖子。
- 接口调用：`GET /mobile/forum/post/list?pageNum=N...`
- 数据库变化：无。

5) 点击帖子卡片
- 页面变化：进入帖子详情页。
- 接口调用：详情页触发（见 3.3）。
- 数据库变化：无（列表页本身不写库）。

6) 点击搜索按钮
- 页面变化：跳转到搜索页（`/post/search`）。
- 接口调用：无（进入搜索页后触发）。
- 数据库变化：无。

7) 点击菜单按钮
- 页面变化：弹出菜单选项。
  - 选择“关注的帖子”：跳转 `/followed`
  - 选择“发过的帖子”：跳转 `/my-posts`
- 接口调用：无（跳转页面后再触发）。
- 数据库变化：无。

8) 点击底部“发表帖子”
- 页面变化：进入发帖页（`/post/create`）。
- 接口调用：无（进入页面后触发）。
- 数据库变化：无。

9) 新帖蓝条标记
- 页面变化：新帖卡片左侧显示蓝色标记。
- 规则：本地保存 `forum_last_read_max_id`，当列表最大 `postId` 更新时刷新该值；`postId` 大于该值视为新帖。
- 数据库变化：无（仅 localStorage 变化）。

10) 从详情返回列表
- 页面变化：不触发列表刷新，滚动位置恢复到进入详情前的坐标。
- 触发方式：左滑返回或企业微信返回按钮。
- 数据变化：本地保存滚动位置（内存状态），不写库。

**异常与边界**
- 非企业微信环境（生产）：仅显示访问受限提示，不加载列表。
- 接口失败：显示空列表或保持加载失败状态，不崩溃。
- 限流帖：列表接口带 `wxUserid` 时，后端过滤不可见的限流帖。

### 3.3 帖子详情

**页面/功能入口**
- 入口路径：`/post/{postId}`。
- 来源：帖子列表、搜索结果、关注列表、我的帖子列表。
- 权限：企业微信环境可访问；非企微生产环境显示拦截提示。

**页面元素清单**
- 顶部导航栏
  - 标题：详情
  - 分享按钮（图标）
- 加载态
  - 帖子骨架屏
- 帖子内容区
  - 标题
  - 正文（支持表情渲染）
  - 图片列表（可预览）
  - 视频区域
    - 可嵌入视频（iframe）
    - 不可嵌入时显示“点击播放”卡片
  - 元信息区
    - 作者昵称（可点击打开企业微信用户资料）
    - 发布时间
    - 锁定状态标记
    - 编辑按钮（作者且 10 分钟内）
    - 删除按钮（作者/管理员/分级管理员）
    - 分类标签
  - 统计区
    - 浏览数
    - 评论数
    - 关注按钮（关注/已关注）
- 评论区
  - 评论列表（含头像、昵称、楼主标记、内容、楼层、时间）
  - 评论删除按钮（作者/管理员/分级管理员可见）
  - 空状态文案
- 底部评论输入区（帖子未锁定时显示）
  - 文本输入框
  - 表情按钮
  - 表情面板（最近使用/全部）
  - 删除表情按钮
  - 发送按钮

**交互与预期结果**
1) 进入详情页
- 页面变化：显示骨架屏，加载完成后展示帖子内容与评论区。
- 接口调用：
  - `GET /mobile/forum/post/{postId}[?wxUserid]`
  - `GET /mobile/forum/comment/list/{postId}[?wxUserid]`
  - `GET /mobile/forum/post/follow/check/{postId}?wxUserid=xxx`
- 数据库变化：
  - `forum_post.view_count` 自增 1。

2) 帖子不可见（限流或不存在）
- 页面变化：显示空白页（无 UI）。
- 触发条件：
  - 帖子不存在
  - 帖子为限流且访问者不是作者
- 数据库变化：无。

3) 点击分享按钮
- 页面变化：弹出“正在打开”短暂提示。
- 交互结果：调用企业微信分享接口。
- 接口调用：
  - 前端调用企业微信 JS-SDK（需要后端签名接口）
  - `GET /mobile/wxwork/jsapi/signature?url=当前页URL`
- 数据库变化：无。

4) 点击作者昵称或头像
- 页面变化：弹出“正在打开”短暂提示。
- 交互结果：打开企业微信用户资料页。
- 接口调用：无（JS-SDK 本地调用）。
- 数据库变化：无。

5) 点击关注/已关注
- 页面变化：按钮状态在“关注/已关注”之间切换。
- 接口调用：
  - 关注：`POST /mobile/forum/post/follow`
  - 取消关注：`POST /mobile/forum/post/unfollow`
- 数据库变化：
  - 关注：新增 `forum_post_follow` 记录（`user_id`、`post_id`）
  - 取消关注：删除对应 `forum_post_follow` 记录

6) 点击编辑（作者且 10 分钟内）
- 页面变化：跳转编辑页 `/post/edit/{postId}`。
- 接口调用：编辑页进入后调用详情接口。
- 数据库变化：无（跳转不写库）。

7) 点击删除（作者/管理员/分级管理员）
- 页面变化：弹出确认对话框，确认后返回列表页并刷新。
- 接口调用：`POST /mobile/forum/post/delete`（携带 `postId`、`wxUserid`）。
- 数据库变化：
  - `forum_post`：逻辑删除（`del_flag=1`）
  - 同帖评论：逻辑删除或删除处理（后端实现）
  - `forum_post_log`：新增“delete”日志（记录操作者角色/昵称）

8) 评论列表加载
- 页面变化：按分页加载评论，末页显示结束。
- 接口调用：`GET /mobile/forum/comment/list/{postId}`（带 `wxUserid` 过滤限流评论）。
- 数据库变化：无。

9) 发表评论
- 页面变化：
  - 显示“评论成功”提示
  - 评论即时插入列表末尾
  - 评论数 +1
- 接口调用：`POST /mobile/forum/comment`
- 数据库变化：
  - `forum_comment`：新增记录
    - `post_id`、`user_id`、`content`、`user_unit`、`user_dept`
    - 若用户限流：`is_rate_limited=1`
  - `forum_post.comment_count`：+1
  - `forum_post.last_reply_time`：更新为当前时间

10) 删除评论（作者/管理员/分级管理员）
- 页面变化：弹出确认对话框，确认后评论从列表移除，评论数 -1。
- 接口调用：`POST /mobile/forum/comment/delete`
- 数据库变化：
  - `forum_comment`：逻辑删除（`del_flag=1`，记录 `deleted_by`）
  - `forum_post.comment_count`：-1
  - `forum_comment_log`：新增删除日志（含楼层与摘要）

11) 帖子锁定
- 页面变化：底部评论输入区隐藏，不可发表评论。
- 接口调用：无（状态由详情数据 `is_locked=1` 决定）。
- 数据库变化：无（仅渲染状态）。


**异常与边界**
- 无权限删除：按钮不显示或返回“无权限”提示。
- 评论为空：显示空状态文案。
- 视频链接不可嵌入：展示跳转卡片，点击后打开外部链接。
- 软键盘弹起：评论输入区随键盘上移，防遮挡。

### 3.4 发帖/编辑

**页面/功能入口**
- 新建入口：`/post/create`（从列表页“发表帖子”进入）。
- 编辑入口：`/post/edit/{postId}`（详情页“编辑”进入）。
- 权限：企业微信环境可访问；非企微生产环境显示拦截提示。

**页面元素清单**
- 顶部表单区域
  - 分类选择区（必选）
    - 分类标签列表
    - 未选择提示
  - 标题输入框（必填）
  - 正文输入框（可空，但需满足内容/图片/视频至少一项）
- 图片预览区
  - 已选图片缩略图
  - 删除图片按钮
- 视频预览区
  - 视频平台标识
  - 视频链接摘要
  - 删除视频按钮
- 底部工具栏
  - 关闭按钮
  - 图片按钮
  - 视频按钮
  - 表情按钮
  - 发送按钮（提交）
- 表情面板
  - 最近使用
  - 全部表情
  - 删除表情按钮
- 视频链接弹窗
  - 输入框
  - 取消/确认按钮
  - 平台支持提示

**交互与预期结果**
1) 进入新建页面
- 页面变化：加载分类列表；若存在草稿则弹出恢复提示。
- 接口调用：`GET /mobile/forum/post/category/list`
- 数据库变化：无。

2) 进入编辑页面
- 页面变化：加载帖子内容并填充表单。
- 接口调用：`GET /mobile/forum/post/{postId}?wxUserid=xxx`
- 数据库变化：无（仅读取）。

3) 选择分类
- 页面变化：分类标签高亮，必选提示消失。
- 接口调用：无。
- 数据库变化：无。

4) 输入标题/正文
- 页面变化：表单内容更新。
- 接口调用：无。
- 数据库变化：无。

5) 草稿自动保存（仅新建）
- 触发条件：标题/正文/图片/视频/分类任意变化。
- 页面变化：无（静默保存）。
- 数据变化：localStorage 保存草稿 `forum_post_draft`，默认保留到下次进入发帖页时提示恢复。
- 数据库变化：无。

6) 恢复草稿
- 页面变化：弹窗确认；确认后回填表单。
- 接口调用：无。
- 数据变化：localStorage 读取并回填。
- 数据库变化：无。

7) 选择图片
- 页面变化：图片缩略图显示，可删除。
- 接口调用：
  - 大图压缩后上传：`POST /mobile/forum/post/upload/image`
- 数据库变化：无（图片仅上传文件，未写帖子表）。

8) 图片压缩规则
- 大于阈值（约 500KB）的静态图片：压缩后上传。
- GIF：不压缩（避免动图变静图）。
- 数据库变化：无。

9) 选择视频链接
- 页面变化：显示视频预览条；支持删除。
- 交互规则：
  - 校验 URL 格式
  - 仅支持优酷/腾讯/B 站链接
  - 支持从分享文本提取 URL
- 接口调用：无（保存到表单）。
- 数据库变化：无。

10) 点击发送（新建）
- 页面变化：显示上传进度遮罩；成功后提示并返回列表，同时清理草稿箱。
- 校验规则：
  - 分类必选
  - 标题必填
  - 正文/图片/视频至少一项
- 接口调用：`POST /mobile/forum/post`
- 数据库变化：
  - `forum_post`：新增记录
    - `user_id`、`title`、`content`、`images`、`video_url`、`category_id`
    - `user_unit`、`user_dept` 写入发帖时单位/部门快照
    - 若用户限流：`is_restricted=1`
  - `forum_post_log`：新增“create”日志

11) 点击发送（编辑）
- 页面变化：提示修改成功并返回详情页。
- 接口调用：`PUT /mobile/forum/post`
- 数据库变化：
  - `forum_post_edit_history`：插入编辑前快照
  - `forum_post`：更新 `title/content/images/video_url/category_id`
  - `forum_post_log`：新增“edit”日志

12) 关闭/返回
- 页面变化：若表单有内容提示“放弃编辑”；确认后返回上一页并清理草稿箱。
- 接口调用：无。
- 数据库变化：无。

**异常与边界**
- 用户禁言（后端预留、无后台操作入口）：若数据库处于禁言状态，发布接口返回错误，前端提示“无法发帖”。
- 编辑超时：超过 10 分钟不允许编辑，接口返回错误提示。
- 图片上传失败：提示错误，允许重新上传。
- 草稿仅对新建有效，编辑模式不保存草稿。

### 3.5 评论

**页面/功能入口**
- 评论功能入口：帖子详情页评论区与底部评论输入框。
- 权限：企业微信环境可访问；帖子锁定时不可评论。

**页面元素清单**
- 评论列表区域
  - 评论头像（可点击打开用户资料）
  - 评论昵称
  - 楼主标记（评论用户等于发帖人）
  - 评论内容（支持表情渲染）
  - 楼层号
  - 时间
  - 删除按钮（作者/管理员/分级管理员可见）
- 空状态提示
  - 文案：暂无评论/快来抢沙发
- 底部评论输入区（未锁定时显示）
  - 文本输入框
  - 表情按钮
  - 表情面板（最近使用/全部）
  - 删除表情按钮
  - 发送按钮

**交互与预期结果**
1) 加载评论列表
- 页面变化：分页加载评论，加载完成后显示评论列表。
- 接口调用：`GET /mobile/forum/comment/list/{postId}[?wxUserid]`
- 数据库变化：无。

2) 发表评论
- 页面变化：
  - 显示“评论成功”提示
  - 评论即时插入列表末尾
  - 评论数 +1
- 接口调用：`POST /mobile/forum/comment`
- 数据库变化：
  - `forum_comment`：新增记录
    - `post_id`、`user_id`、`content`、`user_unit`、`user_dept`
    - 若用户限流：`is_rate_limited=1`
  - `forum_post.comment_count`：+1
  - `forum_post.last_reply_time`：更新为当前时间

3) 删除评论（作者/管理员/分级管理员）
- 页面变化：确认弹窗后，评论从列表移除，评论数 -1。
- 接口调用：`POST /mobile/forum/comment/delete`
- 数据库变化：
  - `forum_comment`：逻辑删除（`del_flag=1`，记录 `deleted_by`）
  - `forum_post.comment_count`：-1
  - `forum_comment_log`：新增删除日志（含楼层与摘要）

4) 评论可见性（限流评论）
- 页面变化：非作者看不到限流评论。
- 接口调用：评论列表需传 `wxUserid` 以触发后端过滤。
- 数据库变化：无。

5) 锁定帖子
- 页面变化：底部评论输入区隐藏，不可发表评论。
- 接口调用：无（由 `forum_post.is_locked=1` 决定）。
- 数据库变化：无。

**异常与边界**
- 用户禁言（后端预留、无后台操作入口）：若数据库处于禁言状态，发表评论接口返回错误，前端提示“无法评论”。
- 帖子被锁定：接口返回错误或前端禁止发送。
- 评论为空：显示空状态文案。

### 3.6 搜索

**页面/功能入口**
- 入口：帖子列表顶部“搜索”按钮。
- 路由：`/post/search`。

**页面元素清单**
- 顶部搜索栏
  - 搜索输入框
  - 搜索按钮
  - 清空按钮
- 分类筛选区
  - “全部”标签
  - 分类标签列表（横向可滚动）
- 搜索历史区（未搜索时显示）
  - 历史关键词标签
  - 清空历史按钮
- 搜索结果区
  - 骨架屏
  - 结果列表（复用 `PostItem`）
  - 空结果提示

**交互与预期结果**
1) 进入搜索页
- 页面变化：显示搜索输入框与搜索历史（若有）。
- 接口调用：
  - `GET /mobile/forum/post/category/list`
- 数据库变化：无。

2) 执行搜索
- 操作方式：点击搜索按钮或回车。
- 页面变化：显示骨架屏并加载结果。
- 接口调用：
  - `GET /mobile/forum/post/list?pageNum=1&pageSize=10&title=关键字[&categoryId][&wxUserid]`
- 数据库变化：无。

3) 搜索历史
- 页面变化：新搜索词加入历史标签列表（去重、置顶）。
- 数据变化：localStorage 保存 `post_search_history`（最多 10 条）。
- 数据库变化：无。

4) 点击历史关键词
- 页面变化：关键词写入搜索框并触发搜索。
- 接口调用：同“执行搜索”。
- 数据库变化：无。

5) 切换分类
- 页面变化：若已搜索则重新拉取结果。
- 接口调用：同“执行搜索”，携带 `categoryId`。
- 数据库变化：无。

6) 清空搜索
- 页面变化：清空输入框，结果列表清空，恢复历史区展示。
- 接口调用：无。
- 数据库变化：无。

7) 清空历史
- 页面变化：历史关键词清空。
- 数据变化：localStorage 删除 `post_search_history`。
- 数据库变化：无。

8) 点击搜索结果
- 页面变化：进入帖子详情页。
- 接口调用：详情页触发（见 3.3）。
- 数据库变化：无。

9) 从详情返回列表
- 页面变化：不触发列表刷新，滚动位置恢复到进入详情前的坐标。
- 触发方式：左滑返回或企业微信返回按钮。
- 数据变化：本地保存滚动位置（内存状态），不写库。
**异常与边界**
- 关键词为空：不触发搜索。
- 接口失败：显示空结果或保持失败状态，不崩溃。
- 限流帖：带 `wxUserid` 时后端过滤不可见结果。

### 3.7 我发过的帖子

**页面/功能入口**
- 入口：帖子列表页面右上角菜单“发过的帖子”。
- 路由：`/my-posts`。

**页面元素清单**
- 顶部导航栏
  - 标题：发过的帖子
- 帖子列表
  - 帖子卡片（标题、摘要、时间、浏览数、评论数）
  - 锁定标记
- 空状态
  - 图标 + 提示文案

**交互与预期结果**
1) 进入页面
- 页面变化：加载并显示当前用户发过的帖子列表。
- 接口调用：
  - `GET /mobile/forum/post/my/{userId}?pageNum=1&pageSize=20`（前端从 localStorage 取 `userId`）
- 数据库变化：无。

2) 上拉加载更多
- 页面变化：列表追加下一页数据。
- 接口调用：
  - `GET /mobile/forum/post/my/{userId}?pageNum=N&pageSize=20`
- 数据库变化：无。

3) 点击帖子卡片
- 页面变化：进入帖子详情页。
- 接口调用：详情页触发（见 3.3）。
- 数据库变化：无。

4) 从详情返回列表
- 页面变化：不触发列表刷新，滚动位置恢复到进入详情前的坐标。
- 触发方式：左滑返回或企业微信返回按钮。
- 数据变化：本地保存滚动位置（内存状态），不写库。
**异常与边界**
- localStorage 无 `userId`：不请求接口，直接显示空状态。
- 接口失败：显示空列表或保持失败状态。

### 3.8 关注的帖子

**页面/功能入口**
- 入口：帖子列表页面右上角菜单“关注的帖子”。
- 路由：`/followed`。

**页面元素清单**
- 顶部导航栏
  - 标题：关注的帖子
- 帖子列表
  - 帖子卡片（标题、摘要、作者、浏览数、评论数）
  - 锁定标记
- 空状态
  - 图标 + 提示文案

**交互与预期结果**
1) 进入页面
- 页面变化：加载并显示当前用户关注的帖子列表。
- 接口调用：
  - `GET /mobile/forum/post/follow/list?wxUserid=xxx&pageNum=1&pageSize=20`
- 数据库变化：无。

2) 上拉加载更多
- 页面变化：列表追加下一页数据。
- 接口调用：
  - `GET /mobile/forum/post/follow/list?wxUserid=xxx&pageNum=N&pageSize=20`
- 数据库变化：无。

3) 点击帖子卡片
- 页面变化：进入帖子详情页。
- 接口调用：详情页触发（见 3.3）。
- 数据库变化：无。

**异常与边界**
- localStorage 无 `wxUserid`：不请求接口，直接显示空状态。
- 接口失败：显示空列表或保持失败状态。
- 列表默认过滤已删除/限流帖子（后端处理）。

## 4. 后台管理功能

### 4.1 论坛用户管理

**页面/功能入口**
- 后台菜单：系统管理 / 论坛用户管理。
- 权限：`system:forumUser:list`、`system:forumUser:assignRole`、`system:forumUser:rateLimit`、`system:forumUser:export`。

**页面元素清单**
- 查询区
  - 昵称输入框
  - 单位下拉框（联动部门）
  - 部门下拉框
  - 角色下拉框（管理员/分级管理员/普通用户）
  - 搜索按钮
  - 重置按钮
- 操作区
  - 导出按钮
- 列表区
  - 用户ID
  - 昵称
  - 企业微信ID
  - 单位
  - 部门
  - 角色标签
  - 限流状态标签
  - 创建时间
  - 操作按钮
    - 分配角色
    - 限流/解除限流
- 弹窗
  - 分配角色对话框
    - 用户昵称（只读）
    - 角色单选（user/sub_admin/admin）
    - 确定/取消

**交互与预期结果**
1) 页面加载
- 页面变化：加载列表与单位下拉数据。
- 接口调用：
  - `GET /system/forum/user/list`
  - `GET /system/forum/user/unitList`
- 数据库变化：无。

2) 查询筛选
- 页面变化：按条件刷新列表。
- 接口调用：`GET /system/forum/user/list`（携带查询参数）。
- 数据库变化：无。

3) 选择单位联动部门
- 页面变化：选择单位后加载对应部门下拉选项；更换单位时清空部门。
- 接口调用：`GET /system/forum/user/deptList/{unitId}`
- 数据库变化：无。

4) 分配角色
- 页面变化：弹出角色分配对话框；提交后列表刷新。
- 接口调用：`PUT /system/forum/user/assignRole`
- 权限规则：
  - 仅 `admin` 可分配 `admin` 角色
- 数据库变化：
  - `forum_user.role` 更新
  - `forum_user.update_by` 更新为当前后台账号

5) 用户限流/解除限流
- 页面变化：弹出确认框；成功后列表状态更新。
- 接口调用：`PUT /system/forum/user/rateLimit`
- 权限规则：
  - `sub_admin` 仅可操作本单位用户
- 数据库变化：
  - `forum_user.is_rate_limited` 更新
  - `forum_user.update_by` 更新为当前后台账号

6) 导出
- 页面变化：下载 Excel 文件。
- 接口调用：`POST /system/forum/user/export`
- 数据库变化：无。

**异常与边界**
- 权限不足：按钮不显示或接口返回无权限。
- 分级管理员跨单位操作：接口返回错误提示。
- 导出为空：仍可下载空表。

### 4.2 帖子管理

**页面/功能入口**
- 后台菜单：论坛管理 / 帖子管理。
- 权限：`forum:post:list`、`forum:post:query`、`forum:post:remove`、`forum:post:lock`。

**页面元素清单**
- 查询区
  - 标题/作者等过滤（根据现有字段）
  - 分类下拉框
  - 搜索按钮/重置按钮
- 列表区
  - 帖子基础信息（标题、作者、分类、创建时间等）
  - 状态字段（锁定、置顶、限流、删除）
  - 操作按钮
    - 查看详情
    - 删除
    - 恢复
    - 锁定/解锁
    - 置顶/取消置顶
    - 限流/解除限流
    - 查看操作日志
    - 查看评论删除日志
    - 查看编辑历史
- 弹窗
  - 详情对话框
  - 置顶时长选择对话框
  - 操作日志对话框
  - 评论删除日志对话框
  - 编辑历史对话框

**交互与预期结果**
1) 页面加载
- 页面变化：加载列表与分类选项。
- 接口调用：
  - `GET /forum/post/list`
  - `GET /forum/category/listAll`
- 数据库变化：无。

2) 查看详情
- 页面变化：弹出详情对话框，显示帖子内容、作者、分类、图片/视频等。
- 接口调用：`GET /forum/post/{postId}`
- 数据库变化：无。

3) 删除帖子
- 页面变化：确认后列表状态更新为“已删除”。
- 接口调用：`DELETE /forum/post/{postIds}`
- 数据库变化：
  - `forum_post.del_flag=1`
  - 同帖评论逻辑删除或删除（后端实现）
  - `forum_post_log`：新增“delete”日志（operatorId/operatorName）
- 前端体验影响：
  - 移动端列表/搜索/关注列表不再展示该帖。

4) 恢复帖子
- 页面变化：状态从“已删除”恢复。
- 接口调用：`PUT /forum/post/restore/{postId}`
- 数据库变化：
  - `forum_post.del_flag=0`
  - `forum_post_log`：新增“restore”日志
- 前端体验影响：
  - 移动端可再次检索/展示该帖。

5) 锁定/解锁
- 页面变化：列表状态更新为“已锁定/正常”。
- 接口调用：
  - 锁定：`PUT /forum/post/lock/{postId}`
  - 解锁：`PUT /forum/post/unlock/{postId}`
- 数据库变化：
  - `forum_post.is_locked` 更新为 `1/0`
  - `forum_post_log`：新增“lock/unlock”日志
- 前端体验影响：
  - 移动端详情页显示“已锁定”标记
  - 评论输入区隐藏，禁止新增评论

6) 置顶/取消置顶
- 页面变化：弹出置顶时长选择；确认后列表显示置顶状态。
- 接口调用：
  - 置顶：`PUT /forum/post/pin/{postId}?hours=xx`
  - 取消置顶：`PUT /forum/post/unpin/{postId}`
- 数据库变化：
  - `forum_post.is_pinned` 更新为 `1/0`
  - `forum_post.pin_expire_time` 设为过期时间或 `NULL`
  - `forum_post_log`：新增“pin/unpin”日志
- 前端体验影响：
  - 移动端列表显示置顶标签
  - 过期后不再显示置顶标签

7) 限流/解除限流
- 页面变化：列表状态更新为“限流/正常”。
- 接口调用：
  - 限流：`PUT /forum/post/restrict/{postId}`
  - 解除限流：`PUT /forum/post/unrestrict/{postId}`
- 数据库变化：
  - `forum_post.is_restricted` 更新为 `1/0`
  - `forum_post_log`：新增“restrict/unrestrict”日志
- 前端体验影响：
  - 限流后，仅作者可在移动端查看详情
  - 列表/搜索/关注列表对非作者不可见

8) 查看操作日志
- 页面变化：弹出日志列表（时间、操作类型、操作者、描述）。
- 接口调用：`GET /forum/post/log/{postId}`
- 数据库变化：无。

9) 查看评论删除日志
- 页面变化：弹出评论删除日志对话框。
- 接口调用：`GET /forum/comment/log/{postId}`
- 数据库变化：无。

10) 查看编辑历史
- 页面变化：弹出编辑历史对话框，显示标题/内容/分类等变更记录。
- 接口调用：`GET /forum/post/edit-history/list/{postId}`
- 数据库变化：无。

**异常与边界**
- 无权限操作：按钮不显示或接口返回无权限。
- 置顶已删除帖子：接口返回错误提示。
- 分页/筛选：保持当前查询条件。

### 4.3 评论管理

**页面/功能入口**
- 后台菜单：论坛管理 / 评论管理。
- 权限：`forum:post:list`、`forum:post:remove`。

**页面元素清单**
- 查询区
  - 按帖子、作者、内容等过滤（按现有字段）
  - 搜索按钮/重置按钮
- 列表区
  - 评论信息（所属帖子、作者、内容、楼层、时间）
  - 删除状态
  - 操作按钮
    - 删除评论
    - 恢复评论
    - 查看评论删除日志

**交互与预期结果**
1) 页面加载
- 页面变化：加载评论列表。
- 接口调用：`GET /forum/comment/list`
- 数据库变化：无。

2) 删除评论
- 页面变化：确认后评论状态更新为已删除。
- 接口调用：`DELETE /forum/comment/{commentIds}`
- 数据库变化：
  - `forum_comment.del_flag=1`
  - `forum_post.comment_count`：-1
  - `forum_comment_log`：新增删除日志（楼层、摘要、操作者）
- 前端体验影响：
  - 移动端评论列表不再展示该评论
  - 评论数同步减少

3) 恢复评论
- 页面变化：评论状态恢复为正常。
- 接口调用：`PUT /forum/comment/restore/{commentId}`
- 数据库变化：
  - `forum_comment.del_flag=0`
  - `forum_post.comment_count`：+1
- 前端体验影响：
  - 移动端可再次看到该评论

4) 查看评论删除日志
- 页面变化：弹出评论删除日志对话框。
- 接口调用：`GET /forum/comment/log/{postId}`
- 数据库变化：无。

**异常与边界**
- 无权限操作：按钮不显示或接口返回无权限。
- 批量删除：逐条记录删除日志。

### 4.4 分类管理

**页面/功能入口**
- 后台菜单：论坛管理 / 分类管理。
- 权限：`forum:category:list`、`forum:category:add`、`forum:category:edit`、`forum:category:remove`。

**页面元素清单**
- 查询区
  - 分类名称/状态过滤（按现有字段）
  - 搜索按钮/重置按钮
- 列表区
  - 分类名称
  - 状态（启用/停用）
  - 排序
  - 操作按钮（新增/编辑/删除）
- 弹窗
  - 新增分类对话框
  - 编辑分类对话框

**交互与预期结果**
1) 页面加载
- 页面变化：加载分类列表。
- 接口调用：`GET /forum/category/list`
- 数据库变化：无。

2) 新增分类
- 页面变化：弹窗填写并提交后，列表刷新。
- 接口调用：`POST /forum/category`
- 数据库变化：
  - `forum_category`：新增记录

3) 编辑分类
- 页面变化：弹窗编辑并提交后，列表刷新。
- 接口调用：`PUT /forum/category`
- 数据库变化：
  - `forum_category`：更新记录

4) 删除分类
- 页面变化：确认后删除；若分类下有帖子则提示不可删除。
- 接口调用：`DELETE /forum/category/{categoryIds}`
- 数据库变化：
  - 无关联帖子时：`forum_category` 删除
  - 有关联帖子时：不删除，返回错误提示
- 前端体验影响：
  - 移动端分类筛选项同步减少

5) 移动端分类列表
- 页面变化：分类启用状态变化后，移动端列表/发帖页分类同步更新。
- 接口调用：
  - `GET /mobile/forum/post/category/list`
- 数据库变化：无。

**异常与边界**
- 无权限操作：按钮不显示或接口返回无权限。
- 删除含帖子分类：返回错误提示并阻止删除。

### 4.5 统计报表

**页面/功能入口**
- 后台菜单：论坛管理 / 数据统计。
- 权限：`forum:statistics:list`、`forum:statistics:export`。

**页面元素清单**
- 查询区
  - 开始日期
  - 截止日期
  - 搜索/重置按钮
- 操作区
  - 导出按钮
  - 视图切换（第二层：按部门/按人员）
- 表格区
  - 第一层：单位统计（单位名称、发帖数、置顶数）
  - 第二层：部门统计或人员统计（名称、发帖数、置顶数）
- 面包屑导航
  - 返回单位统计

**交互与预期结果**
1) 页面加载
- 页面变化：默认展示单位统计表格。
- 接口调用：`GET /forum/statistics/unit`
- 数据库变化：无。

2) 日期筛选
- 页面变化：按日期范围刷新统计结果。
- 接口调用：`GET /forum/statistics/unit?startDate=...&endDate=...`
- 数据库变化：无。

3) 钻取到单位下的部门/人员
- 页面变化：切换到第二层统计表；面包屑显示单位名称。
- 接口调用：
  - 部门视图：`GET /forum/statistics/dept?unitName=...`
  - 人员视图：`GET /forum/statistics/user?unitName=...`
- 数据库变化：无。

4) 视图切换（部门/人员）
- 页面变化：在部门/人员统计表间切换。
- 接口调用：对应统计接口。
- 数据库变化：无。

5) 导出
- 页面变化：下载 Excel 文件。
- 接口调用：
  - 单位导出：`POST /forum/statistics/export/unit`
  - 部门导出：`POST /forum/statistics/export/dept`
  - 人员导出：`POST /forum/statistics/export/user`
- 数据库变化：无。

**权限与范围规则**
- 分级管理员仅能查看本单位统计；后端自动约束 `unitName`。

**统计口径说明**
- 统计结果已排除锁定、限流、删除的帖子。

**异常与边界**
- 第二层未指定单位：接口返回错误提示。
- 无数据：表格显示空状态。

## 5. 数据与接口附录

### 5.1 关键表与字段

**forum_user（论坛用户）**
- `user_id`：用户ID
- `wx_userid`：企业微信用户ID
- `nickname`：昵称
- `avatar`：头像
- `unit`：所属单位
- `department`：所属部门
- `status`：禁言状态（0 正常 / 1 禁言，后端预留）
- `ban_end_time`：禁言结束时间（后端预留）
- `role`：角色（admin/sub_admin/user）
- `is_admin`：管理员标记（历史字段）
- `is_rate_limited`：限流状态（0/1）

**forum_post（帖子）**
- `post_id`：帖子ID
- `user_id`：作者ID
- `user_unit`：发帖时单位快照
- `user_dept`：发帖时部门快照
- `category_id`：分类ID
- `title`：标题
- `content`：正文
- `images`：图片 JSON 数组
- `video_url`：视频链接
- `view_count`：浏览数
- `comment_count`：评论数
- `last_reply_time`：最后回复时间
- `is_locked`：锁定状态（0/1）
- `is_pinned`：置顶状态（0/1）
- `pin_expire_time`：置顶过期时间（NULL 为永久）
- `is_restricted`：限流状态（0/1）
- `del_flag`：删除标记（0/1）

**forum_comment（评论）**
- `comment_id`：评论ID
- `post_id`：所属帖子ID
- `user_id`：评论用户ID
- `user_unit`：评论时单位快照
- `user_dept`：评论时部门快照
- `content`：评论内容
- `floor_num`：楼层号
- `is_rate_limited`：限流评论标记（0/1）
- `del_flag`：删除标记（0/1）
- `deleted_by`：删除人用户ID

**forum_category（分类）**
- `category_id`：分类ID
- `name`：分类名称
- `status`：启用状态
- `sort`：排序

**forum_post_follow（关注关系）**
- `follow_id`：关注ID
- `user_id`：关注用户ID
- `post_id`：帖子ID

**forum_post_log（帖子操作日志）**
- `log_id`：日志ID
- `post_id`：帖子ID
- `action`：操作类型（create/delete/restore/pin/unpin/lock/unlock/restrict/unrestrict/edit）
- `operator_id`：操作者ID
- `operator_name`：操作者昵称
- `description`：操作描述
- `operate_time`：操作时间

**forum_comment_log（评论删除日志）**
- `log_id`：日志ID
- `post_id`：帖子ID
- `comment_id`：评论ID
- `floor_num`：楼层号
- `content_summary`：内容摘要
- `operator_name`：操作者昵称
- `operate_time`：操作时间

**forum_post_edit_history（帖子编辑历史）**
- `history_id`：历史记录ID
- `post_id`：帖子ID
- `user_id`：编辑者ID
- `title`：编辑前标题
- `content`：编辑前正文
- `images`：编辑前图片 JSON
- `video_url`：编辑前视频链接
- `category_id`：编辑前分类ID
- `edit_time`：编辑时间

**forum_unit（单位）**
- `unit_id`：单位ID
- `unit_name`：单位名称
- `display_name`：单位显示名

**forum_department（部门）**
- `dept_id`：部门ID
- `unit_id`：所属单位ID
- `dept_name`：部门名称

### 5.2 关键接口清单

**企业微信接入**
- `GET /mobile/wxwork/jsapi/signature`：获取 JS-SDK 签名
- `GET /mobile/wxwork/user/login`：根据 userid 获取企业微信用户信息
- `GET /mobile/wxwork/oauth/url`：获取 OAuth 授权链接（备用）

**移动端用户**
- `POST /mobile/forum/user/sync`：同步企业微信用户
- `GET /mobile/forum/user/info/{wxUserid}`：获取当前用户信息

**移动端帖子**
- `GET /mobile/forum/post/list`：帖子列表
- `GET /mobile/forum/post/{postId}`：帖子详情
- `POST /mobile/forum/post`：发布帖子
- `PUT /mobile/forum/post`：编辑帖子
- `POST /mobile/forum/post/delete`：删除帖子
- `POST /mobile/forum/post/upload/image`：上传图片

**移动端分类**
- `GET /mobile/forum/post/category/list`：分类列表（启用状态）

**移动端关注**
- `POST /mobile/forum/post/follow`：关注帖子
- `POST /mobile/forum/post/unfollow`：取消关注
- `GET /mobile/forum/post/follow/check/{postId}`：关注状态
- `GET /mobile/forum/post/follow/list`：关注列表

**移动端评论**
- `GET /mobile/forum/comment/list/{postId}`：评论列表
- `POST /mobile/forum/comment`：发表评论
- `POST /mobile/forum/comment/delete`：删除评论

**后台帖子管理**
- `GET /forum/post/list`：帖子列表
- `GET /forum/post/{postId}`：帖子详情
- `DELETE /forum/post/{postIds}`：删除帖子
- `PUT /forum/post/restore/{postId}`：恢复帖子
- `PUT /forum/post/lock/{postId}`：锁定帖子
- `PUT /forum/post/unlock/{postId}`：解锁帖子
- `PUT /forum/post/pin/{postId}`：置顶帖子（带 hours）
- `PUT /forum/post/unpin/{postId}`：取消置顶
- `PUT /forum/post/restrict/{postId}`：限流帖子
- `PUT /forum/post/unrestrict/{postId}`：解除限流
- `GET /forum/post/log/{postId}`：帖子操作日志
- `GET /forum/post/edit-history/list/{postId}`：编辑历史列表

**后台评论管理**
- `GET /forum/comment/list`：评论列表
- `GET /forum/comment/listByPost/{postId}`：按帖子查询评论（含已删）
- `DELETE /forum/comment/{commentIds}`：删除评论
- `PUT /forum/comment/restore/{commentId}`：恢复评论
- `GET /forum/comment/log/{postId}`：评论删除日志

**后台分类管理**
- `GET /forum/category/list`：分类列表
- `GET /forum/category/listAll`：启用分类列表
- `GET /forum/category/{categoryId}`：分类详情
- `POST /forum/category`：新增分类
- `PUT /forum/category`：编辑分类
- `DELETE /forum/category/{categoryIds}`：删除分类

**后台用户管理**
- `GET /system/forum/user/list`：论坛用户列表
- `GET /system/forum/user/unitList`：单位列表
- `GET /system/forum/user/deptList/{unitId}`：部门列表
- `PUT /system/forum/user/assignRole`：分配角色
- `PUT /system/forum/user/rateLimit`：用户限流
- `POST /system/forum/user/export`：导出用户

**后台统计**
- `GET /forum/statistics/unit`：单位统计
- `GET /forum/statistics/dept`：部门统计
- `GET /forum/statistics/user`：人员统计
- `POST /forum/statistics/export/unit`：导出单位统计
- `POST /forum/statistics/export/dept`：导出部门统计
- `POST /forum/statistics/export/user`：导出人员统计
