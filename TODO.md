# Redis缓存
1. 字典数据查询 (如：报修类型下拉框)

    现状：前端每次打开报修页面，都会去查 sys_dict_data 表。

    痛点：字典数据百年不变，全小区一千户人家早晨同时报修，数据库会被这些毫无意义的重复查询拖垮。

    补救方案：在字典的查询接口中加入 Redis Hash 缓存。新增/修改字典时，删除对应的 Cache。

2. 社区公告详情 (富文本正文)

    现状：AppNoticeController.getNoticeDetail 直接查了 MySQL。

    痛点：物业群发了一条“关于今晚停水的紧急通知”。瞬间 3000 个业主同时点开 App 查看详情。MySQL 疯狂读取 3000 次长篇 HTML 富文本（LongText），极易引发 I/O 阻塞甚至宕机。

    补救方案：查询公告详情时，先查 Redis (String 结构，设置 24 小时过期)。如果没有，再查 MySQL 并回写 Redis。