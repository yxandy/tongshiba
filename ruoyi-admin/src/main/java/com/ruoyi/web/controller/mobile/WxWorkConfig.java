package com.ruoyi.web.controller.mobile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 企业微信配置
 */
@Component
public class WxWorkConfig {

    @Value("${wxwork.corpId}")
    private String corpId;

    @Value("${wxwork.agentId}")
    private Integer agentId;

    @Value("${wxwork.corpSecret}")
    private String corpSecret;

    public String getCorpId() {
        return corpId;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public String getCorpSecret() {
        return corpSecret;
    }
}
