# 阿里云 OSS 配置与使用说明

## 📋 配置说明

### 1. application.yml 配置项

```yaml
aliyun:
  oss:
    bucket-name: wuhututu                    # 存储桶名称（Bucket）
    endpoint: oss-cn-hangzhou.aliyuncs.com   # 地域 Endpoint（根据实际地域选择）
    access-key-id: LTAI5tJ5Nkz6MfHZ1PBpGoer      # AccessKey ID（子账号或主账号）
    access-key-secret: zDDedh2iX6xXG8Ec1y82cPmLwWJHNn  # AccessKey Secret
    file-host: wuhututu/                     # 上传文件的基础路径（OSS 中的目录前缀）

upload:
  type: local  # 上传类型配置
  # type: local     - 使用本地存储（开发环境推荐）
  # type: aliyun    - 使用阿里云 OSS（生产环境推荐）
  path: ${user.dir}/uploads/images/
  url-prefix: /uploads/images/
```

---

## 🔧 切换存储方式

### 使用本地存储（开发环境）
```yaml
upload:
  type: local
```

**特点**：
- ✅ 文件保存在服务器本地磁盘
- ✅ 无需额外费用
- ✅ 配置简单
- ❌ 不适合分布式部署
- ❌ 需要自己处理静态资源映射

### 使用阿里云 OSS（生产环境）
```yaml
upload:
  type: aliyun
```

**特点**：
- ✅ 高可用、高可靠
- ✅ 支持 CDN 加速
- ✅ 按量付费，成本低
- ✅ 适合分布式部署
- ✅ 无需处理静态资源映射

---

## 🚀 阿里云 OSS 开通步骤

### 步骤 1：开通 OSS 服务
1. 访问 [阿里云官网](https://www.aliyun.com/)
2. 登录/注册账号
3. 进入 [对象存储 OSS 控制台](https://oss.console.aliyun.com/)
4. 点击"开通 OSS 服务"

### 步骤 2：创建 Bucket
1. 在 OSS 控制台点击"创建 Bucket"
2. 填写 Bucket 名称（如：`wuhututu`）
3. 选择地域（如：华东 1（杭州）→ `oss-cn-hangzhou`）
4. 读写权限选择"私有"（推荐）
5. 点击"完成"

### 步骤 3：获取 AccessKey
**推荐使用 RAM 子账号的 AccessKey（更安全）**

1. 访问 [RAM 访问控制](https://ram.console.aliyun.com/)
2. 创建用户 → 勾选"编程访问"
3. 为用户授权"AliyunOSSFullAccess"权限
4. 保存 AccessKey ID 和 AccessKey Secret

**或使用主账号 AccessKey（不推荐）**
1. 访问 [AccessKey 管理](https://ram.console.aliyun.com/manage/ak)
2. 创建 AccessKey
3. 保存 AccessKey ID 和 AccessKey Secret

### 步骤 4：配置 CORS 规则（允许前端跨域访问）
1. 进入 Bucket 管理页面
2. 左侧菜单"数据安全" → "跨域设置"
3. 点击"创建跨域规则"
   - 来源：`*`（或指定域名）
   - 允许 Methods：勾选 GET、POST、PUT
   - 允许 Headers：`*`
   - 暴露 Headers：ETag
   - 缓存时间：0
4. 点击"确定"

---

## 📊 Endpoint 地域对照表

| 地域 | Endpoint |
|------|----------|
| 华东 1（杭州） | oss-cn-hangzhou.aliyuncs.com |
| 华东 2（上海） | oss-cn-shanghai.aliyuncs.com |
| 华北 1（青岛） | oss-cn-qingdao.aliyuncs.com |
| 华北 2（北京） | oss-cn-beijing.aliyuncs.com |
| 华南 1（深圳） | oss-cn-shenzhen.aliyuncs.com |
| 香港 | oss-cn-hongkong.aliyuncs.com |
| 美国西部 1（硅谷） | oss-us-west-1.aliyuncs.com |

---

## 💰 计费说明（参考）

### 标准存储（ locally ）
- 存储费用：约 ¥0.12/GB/月
- 流量费用：约 ¥0.50/GB（外网流出）
- 请求费用：¥0.01/万次

### 示例成本
假设每月上传 1GB 图片，被访问 100 次（每次 1MB）：
- 存储费：1GB × ¥0.12 = ¥0.12/月
- 流量费：100MB × ¥0.50/GB ≈ ¥0.05/月
- **总计：约 ¥0.17/月**

---

## 🔒 安全建议

### 1. 使用 HTTPS
- ✅ 所有上传和访问都使用 HTTPS
- ✅ 防止数据被窃听

### 2. 使用 RAM 子账号
- ✅ 不要使用主账号 AccessKey
- ✅ 为子账号分配最小必要权限

### 3. 设置 Referer 白名单
- ✅ 在 OSS 控制台设置 Referer 防盗链
- ✅ 只允许自己的域名访问

### 4. 定期轮换 AccessKey
- ✅ 定期更换 AccessKey
- ✅ 旧 Key 保留一段时间过渡期

---

## 🛠️ 常见问题

### Q1: 上传失败，提示"AccessDenied"
**原因**：
- Bucket 权限设置为私有
- AccessKey 没有对应权限

**解决**：
- 检查 RAM 用户是否有 AliyunOSSFullAccess 权限
- 或在 Bucket 设置中授予相应权限

### Q2: 跨域访问失败
**原因**：未配置 CORS 规则

**解决**：
- 按照"步骤 4"配置 CORS 规则
- 确保暴露 Headers 包含 ETag

### Q3: 图片无法访问
**原因**：
- Bucket 是私有读写权限
- 未使用签名 URL

**解决**：
- 方案 1：将文件 ACL 设为"公共读"（上传时设置）
- 方案 2：使用签名 URL（代码中生成临时访问链接）

### Q4: 如何在代码中生成签名 URL？
```java
// 如果需要访问私有文件，可以生成签名 URL
Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 小时后过期
URL signedUrl = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
return signedUrl.toString();
```

---

## 📝 测试验证

### 1. 切换到 OSS 模式
```yaml
# application.yml
upload:
  type: aliyun
```

### 2. 重启后端服务
```bash
cd c:\Users\90924\Desktop\subjects\CityEase
mvn clean compile
# 重启后端
```

### 3. 测试上传
1. 打开商城管理页面
2. 新增商品
3. 上传图片
4. 观察返回的 URL 是否以 `https://` 开头
5. 在浏览器中打开该 URL，确认能正常访问

### 4. 查看日志
```
图片上传到阿里云 OSS 成功：https://wuhututu.oss-cn-hangzhou.aliyuncs.com/wuhututu/2026/03/09/uuid.jpg
```

---

## 🎯 最佳实践

### 1. 文件命名
- ✅ 使用 UUID 避免重名
- ✅ 按日期分目录存储（已实现）
- ✅ 保留原文件扩展名

### 2. 图片优化
- ✅ 上传前压缩图片（可后续添加）
- ✅ 使用 WebP 格式节省空间
- ✅ 缩略图单独存储

### 3. CDN 加速
- ✅ 绑定自定义域名
- ✅ 开启 CDN 加速
- ✅ 配置缓存策略

### 4. 备份策略
- ✅ 开启 OSS 版本控制
- ✅ 定期备份到其他地域
- ✅ 设置生命周期规则

---

## 📞 技术支持

- [阿里云 OSS 官方文档](https://help.aliyun.com/product/31815.html)
- [OSS SDK for Java](https://help.aliyun.com/document_detail/32008.html)
- [OSS 开发指南](https://help.aliyun.com/document_detail/31883.html)

---

## ✅ 完成清单

- [x] 创建 AliyunOssServiceImpl 实现类
- [x] 配置 application.yml OSS 参数
- [x] 创建 FileUploadConfig 配置类
- [x] 支持本地/OSS 切换
- [x] 文件校验（大小、格式）
- [x] 按日期分目录存储
- [x] 返回 HTTPS 访问 URL
- [ ] 测试上传功能
- [ ] 配置 CORS 规则
- [ ] 绑定 CDN 域名（可选）

---

**🎉 配置完成后，只需修改 `upload.type` 即可在本地存储和云存储之间切换！**
