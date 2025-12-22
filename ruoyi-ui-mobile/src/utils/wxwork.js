/**
 * 企业微信工具类
 */
import request from '@/utils/request'

// 检测是否在企业微信环境
export function isWxWorkEnv() {
    const ua = navigator.userAgent.toLowerCase()
    return ua.includes('wxwork')
}

// 检测是否在微信环境
export function isWeChatEnv() {
    const ua = navigator.userAgent.toLowerCase()
    return ua.includes('micromessenger')
}

// 获取不允许访问的提示信息
export function getAccessDeniedMessage() {
    return '该网页只能在企业微信内部浏览'
}

// 模拟用户数据（开发环境使用）
export function getMockUser() {
    return {
        wxUserid: 'test_user_001',
        nickname: '测试用户',
        avatar: 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKq2CRmib1mG2r7icH5Xn0MIicFqLjuJED0hibNfLf2wictR4ZkKHXxoTfXZKibibHjwPheEic8cWX93rMmgQ/132',
        department: '技术部'
    }
}

// JS-SDK 是否已初始化
let sdkReady = false
let sdkConfig = null

/**
 * 初始化企业微信 JS-SDK
 * @returns {Promise<boolean>}
 */
export async function initWxWorkJsSdk() {
    if (sdkReady) return true
    if (!isWxWorkEnv()) {
        console.log('非企业微信环境，跳过 JS-SDK 初始化')
        return false
    }

    try {
        // 获取当前页面 URL（不含 hash）
        const url = window.location.href.split('#')[0]

        // 调用后端获取签名
        const res = await request({
            url: '/mobile/wxwork/jsapi/signature',
            method: 'get',
            params: { url }
        })

        if (res.code !== 200 || !res.data) {
            console.error('获取签名失败:', res)
            return false
        }

        sdkConfig = res.data

        // 调用 wx.config 初始化
        return new Promise((resolve) => {
            wx.config({
                beta: true, // 必须这么写，否则 wx.invoke 调用报错
                debug: false,
                appId: sdkConfig.corpId,
                timestamp: sdkConfig.timestamp,
                nonceStr: sdkConfig.nonceStr,
                signature: sdkConfig.signature,
                jsApiList: ['shareAppMessage', 'onMenuShareAppMessage']
            })

            wx.ready(() => {
                console.log('企业微信 JS-SDK 初始化成功')
                sdkReady = true
                resolve(true)
            })

            wx.error((err) => {
                console.error('企业微信 JS-SDK 初始化失败:', err)
                resolve(false)
            })
        })
    } catch (e) {
        console.error('初始化 JS-SDK 异常:', e)
        return false
    }
}

/**
 * 分享给用户/群聊
 * @param {string} title 分享标题
 * @param {string} link 分享链接
 * @param {string} desc 分享描述（可选）
 * @param {string} imgUrl 分享图标（可选）
 */
export async function shareToUsers(title, link, desc = '', imgUrl = '') {
    // 开发环境模拟
    if (!isWxWorkEnv()) {
        console.log('模拟分享:', { title, link, desc })
        alert(`[模拟分享]\n标题: ${title}\n链接: ${link}`)
        return
    }

    // 确保 SDK 已初始化
    if (!sdkReady) {
        const success = await initWxWorkJsSdk()
        if (!success) {
            console.error('JS-SDK 未初始化，无法分享')
            return
        }
    }

    // 调用企业微信分享接口
    wx.invoke('shareAppMessage', {
        title: title,
        desc: desc || title,
        link: link,
        imgUrl: imgUrl || 'https://wwcdn.weixin.qq.com/node/wwnl/wwnl/style/images/independent/favicon/favicon_48h$5765600e.png'
    }, (res) => {
        if (res.err_msg === 'shareAppMessage:ok') {
            console.log('分享成功')
        } else {
            console.log('分享结果:', res)
        }
    })
}

/**
 * 企业微信 OAuth 登录
 * 检查 URL 中是否有 code 参数，如果有则调用后端换取用户信息
 * 如果没有 code 且需要登录，则跳转到 OAuth 授权页
 * @returns {Promise<object|null>} 用户信息或 null
 */
export async function loginWithWxWork() {
    // 1. 检查 localStorage 是否已有用户信息
    const storedUser = localStorage.getItem('forumUser')
    if (storedUser) {
        try {
            const user = JSON.parse(storedUser)
            // 检查是否是真实用户（非模拟）
            if (user.wxUserid && user.wxUserid !== 'test_user_001') {
                return user
            }
        } catch (e) {
            console.error('解析存储的用户信息失败', e)
        }
    }

    // 2. 非企业微信环境使用模拟用户
    if (!isWxWorkEnv()) {
        console.log('非企业微信环境，使用模拟用户')
        return null
    }

    // 3. 检查 URL 中是否有 code 参数
    const urlParams = new URLSearchParams(window.location.search)
    const code = urlParams.get('code')

    if (code) {
        console.log('检测到 OAuth code，正在获取用户信息...')
        try {
            const res = await request({
                url: '/mobile/wxwork/user/login',
                method: 'get',
                params: { code }
            })

            if (res.code === 200 && res.data) {
                // 存储用户信息
                localStorage.setItem('forumUser', JSON.stringify(res.data))

                // 清除 URL 中的 code 参数
                urlParams.delete('code')
                urlParams.delete('state')
                const newUrl = window.location.pathname +
                    (urlParams.toString() ? '?' + urlParams.toString() : '') +
                    window.location.hash
                window.history.replaceState({}, '', newUrl)

                console.log('用户登录成功:', res.data)
                return res.data
            } else {
                console.error('获取用户信息失败:', res)
            }
        } catch (e) {
            console.error('登录请求失败:', e)
        }
    }

    // 4. 没有 code，需要跳转到 OAuth 授权页
    console.log('未登录，跳转到 OAuth 授权页...')
    try {
        const redirectUri = window.location.href.split('?')[0] // 当前页面作为回调
        const res = await request({
            url: '/mobile/wxwork/oauth/url',
            method: 'get',
            params: { redirectUri }
        })

        if (res.code === 200 && res.data?.oauthUrl) {
            window.location.href = res.data.oauthUrl
            return null // 跳转后不会执行到这里
        }
    } catch (e) {
        console.error('获取 OAuth URL 失败:', e)
    }

    return null
}

/**
 * 获取当前用户信息
 * 优先从 localStorage 获取，如果没有则返回 null
 * @returns {object|null}
 */
export function getCurrentUser() {
    const storedUser = localStorage.getItem('forumUser')
    if (storedUser) {
        try {
            return JSON.parse(storedUser)
        } catch (e) {
            return null
        }
    }
    return null
}

export default {
    isWxWorkEnv,
    isWeChatEnv,
    getAccessDeniedMessage,
    getMockUser,
    initWxWorkJsSdk,
    shareToUsers,
    loginWithWxWork,
    getCurrentUser
}

