package com.ruoyi.web.controller.mobile;

import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信 JS-SDK 签名接口
 */
@RestController
@RequestMapping("/mobile/wxwork")
public class WxWorkController {

    private static final Logger log = LoggerFactory.getLogger(WxWorkController.class);

    private static final String REDIS_ACCESS_TOKEN_KEY = "wxwork:access_token";
    private static final String REDIS_JSAPI_TICKET_KEY = "wxwork:jsapi_ticket";

    @Autowired
    private WxWorkConfig wxWorkConfig;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取 JS-SDK 签名
     * 
     * @param url 当前页面URL（不含#及其后面部分）
     */
    @GetMapping("/jsapi/signature")
    public AjaxResult getJsApiSignature(@RequestParam String url) {
        try {
            // 1. 获取 jsapi_ticket
            String ticket = getJsApiTicket();
            if (ticket == null) {
                return AjaxResult.error("获取 jsapi_ticket 失败");
            }

            // 2. 生成签名
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            long timestamp = System.currentTimeMillis() / 1000;

            // 3. 按字典序拼接字符串
            String str = "jsapi_ticket=" + ticket +
                    "&noncestr=" + nonceStr +
                    "&timestamp=" + timestamp +
                    "&url=" + url;

            // 4. SHA1 签名
            String signature = sha1(str);

            // 5. 返回签名信息
            Map<String, Object> result = new HashMap<>();
            result.put("corpId", wxWorkConfig.getCorpId());
            result.put("agentId", wxWorkConfig.getAgentId());
            result.put("timestamp", timestamp);
            result.put("nonceStr", nonceStr);
            result.put("signature", signature);

            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("获取 JS-SDK 签名失败", e);
            return AjaxResult.error("获取签名失败: " + e.getMessage());
        }
    }

    /**
     * 获取 jsapi_ticket（优先从缓存获取）
     */
    @SuppressWarnings("unchecked")
    private String getJsApiTicket() {
        // 尝试从缓存获取
        String ticket = stringRedisTemplate.opsForValue().get(REDIS_JSAPI_TICKET_KEY);
        if (ticket != null) {
            return ticket;
        }

        // 获取 access_token
        String accessToken = getAccessToken();
        if (accessToken == null) {
            return null;
        }

        // 调用企业微信 API 获取 jsapi_ticket
        String apiUrl = "https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?access_token=" + accessToken;
        try {
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            if (response != null && response.get("errcode") != null && (Integer) response.get("errcode") == 0) {
                ticket = (String) response.get("ticket");
                Integer expiresIn = (Integer) response.get("expires_in");
                // 缓存，提前5分钟过期
                stringRedisTemplate.opsForValue().set(REDIS_JSAPI_TICKET_KEY, ticket, expiresIn - 300,
                        TimeUnit.SECONDS);
                return ticket;
            } else {
                log.error("获取 jsapi_ticket 失败: {}", response);
            }
        } catch (Exception e) {
            log.error("调用企业微信 API 失败", e);
        }
        return null;
    }

    /**
     * 获取 access_token（优先从缓存获取）
     */
    @SuppressWarnings("unchecked")
    private String getAccessToken() {
        // 尝试从缓存获取
        String accessToken = stringRedisTemplate.opsForValue().get(REDIS_ACCESS_TOKEN_KEY);
        if (accessToken != null) {
            return accessToken;
        }

        // 调用企业微信 API 获取 access_token
        String apiUrl = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                wxWorkConfig.getCorpId(),
                wxWorkConfig.getCorpSecret());
        try {
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            if (response != null && response.get("errcode") != null && (Integer) response.get("errcode") == 0) {
                accessToken = (String) response.get("access_token");
                Integer expiresIn = (Integer) response.get("expires_in");
                // 缓存，提前5分钟过期
                stringRedisTemplate.opsForValue().set(REDIS_ACCESS_TOKEN_KEY, accessToken, expiresIn - 300,
                        TimeUnit.SECONDS);
                return accessToken;
            } else {
                log.error("获取 access_token 失败: {}", response);
            }
        } catch (Exception e) {
            log.error("调用企业微信 API 失败", e);
        }
        return null;
    }

    /**
     * SHA1 签名
     */
    private String sha1(String str) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(str.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 通过 userid 获取用户信息
     * 生产环境由上层服务传递 userid，无需 OAuth 流程
     * 
     * @param userid 企业微信 UserId（由上层服务传递）
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/user/login")
    public AjaxResult getUserByUserId(@RequestParam String userid) {
        try {
            if (userid == null || userid.trim().isEmpty()) {
                return AjaxResult.error("userid 不能为空");
            }

            String accessToken = getAccessToken();
            if (accessToken == null) {
                return AjaxResult.error("获取 access_token 失败");
            }

            // 获取用户详细信息
            String getUserUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="
                    + accessToken + "&userid=" + userid;
            Map<String, Object> userDetailRes = restTemplate.getForObject(getUserUrl, Map.class);

            if (userDetailRes == null || userDetailRes.get("errcode") == null
                    || (Integer) userDetailRes.get("errcode") != 0) {
                log.error("获取用户详情失败: {}", userDetailRes);
                return AjaxResult.error("获取用户详情失败: " + (userDetailRes != null ? userDetailRes.get("errmsg") : ""));
            }

            // 返回用户信息
            Map<String, Object> result = new HashMap<>();
            result.put("wxUserid", userid);
            result.put("nickname", userDetailRes.get("name"));
            result.put("avatar", userDetailRes.get("avatar"));

            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return AjaxResult.error("用户登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取 OAuth 授权 URL
     * 前端调用此接口获取授权链接，然后跳转
     * 
     * @param redirectUri 授权后的回调地址
     */
    @GetMapping("/oauth/url")
    public AjaxResult getOAuthUrl(@RequestParam String redirectUri) {
        try {
            String oauthUrl = String.format(
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&agentid=%d&state=STATE#wechat_redirect",
                    wxWorkConfig.getCorpId(),
                    java.net.URLEncoder.encode(redirectUri, "UTF-8"),
                    wxWorkConfig.getAgentId());

            Map<String, Object> result = new HashMap<>();
            result.put("oauthUrl", oauthUrl);
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("生成 OAuth URL 失败", e);
            return AjaxResult.error("生成授权链接失败");
        }
    }
}
