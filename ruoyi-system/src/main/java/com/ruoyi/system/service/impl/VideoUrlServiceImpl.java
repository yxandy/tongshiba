package com.ruoyi.system.service.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.system.service.IVideoUrlService;

/**
 * 视频链接解析服务实现
 */
@Service
public class VideoUrlServiceImpl implements IVideoUrlService {

    private static final Logger log = LoggerFactory.getLogger(VideoUrlServiceImpl.class);

    @Override
    public String resolveShortUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // 只处理 b23.tv 短链接
        if (!url.contains("b23.tv")) {
            return url;
        }

        try {
            // 发送 HEAD 请求获取重定向后的真实 URL
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == 302) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();

                if (location != null && !location.isEmpty()) {
                    log.info("Resolved short URL: {} -> {}", url, location);
                    // 清理 URL 中的查询参数
                    int queryIndex = location.indexOf('?');
                    if (queryIndex > 0) {
                        location = location.substring(0, queryIndex);
                    }
                    return location;
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            log.warn("Failed to resolve short URL: {}, error: {}", url, e.getMessage());
        }

        // 解析失败，返回原链接
        return url;
    }
}
