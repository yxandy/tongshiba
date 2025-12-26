package com.ruoyi.system.service;

/**
 * 视频链接解析服务
 */
public interface IVideoUrlService {

    /**
     * 解析视频短链接
     * 如果是短链接则解析成完整链接，否则原样返回
     * 
     * @param url 原始链接
     * @return 解析后的链接
     */
    String resolveShortUrl(String url);
}
