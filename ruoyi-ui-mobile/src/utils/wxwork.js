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
        avatar: 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKq2CRmib1mG2r7icH5Xn0MIicFqLjuJED0hibNfLf2wictR4ZkKHXxoTfXZKibibHjwPheEic8cWX93rMmgQ/132'
    }
}

// JS-SDK 是否已初始化
let sdkReady = false
let sdkConfig = null
let lastSignedUrl = '' // 记录上次签名使用的 URL

/**
 * 初始化企业微信 JS-SDK
 * @param {boolean} forceReinit 是否强制重新初始化
 * @returns {Promise<boolean>}
 */
export async function initWxWorkJsSdk(forceReinit = false) {
    if (!isWxWorkEnv()) {
        console.log('非企业微信环境，跳过 JS-SDK 初始化')
        return false
    }

    // 获取当前页面 URL（不含 hash）
    const currentUrl = window.location.href.split('#')[0]

    // 如果 URL 没变且已初始化，直接返回
    if (sdkReady && lastSignedUrl === currentUrl && !forceReinit) {
        return true
    }

    // URL 变化或强制重新初始化，需要重新获取签名
    if (sdkReady && lastSignedUrl !== currentUrl) {
        console.log('[JS-SDK] URL 变化，重新获取签名')
        sdkReady = false
    }

    try {
        // 调用后端获取签名
        const res = await request({
            url: '/mobile/wxwork/jsapi/signature',
            method: 'get',
            params: { url: currentUrl }
        })

        if (res.code !== 200 || !res.data) {
            console.error('获取签名失败:', res)
            return false
        }

        sdkConfig = res.data
        lastSignedUrl = currentUrl // 记录本次签名的 URL

        // 调用 wx.config 初始化
        return new Promise((resolve) => {
            wx.config({
                beta: true, // 必须这么写，否则 wx.invoke 调用报错
                debug: false,
                appId: sdkConfig.corpId,
                timestamp: sdkConfig.timestamp,
                nonceStr: sdkConfig.nonceStr,
                signature: sdkConfig.signature,
                jsApiList: ['shareAppMessage', 'onMenuShareAppMessage', 'openUserProfile']
            })

            wx.ready(() => {
                console.log('企业微信 JS-SDK 初始化成功, URL:', currentUrl)
                sdkReady = true
                resolve(true)
            })

            wx.error((err) => {
                console.error('企业微信 JS-SDK 初始化失败:', err)
                sdkReady = false
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

    // 每次都检查 SDK 状态（内部会判断 URL 是否变化）
    const success = await initWxWorkJsSdk()
    if (!success) {
        console.error('JS-SDK 初始化失败，无法分享')
        return
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
 * 打开用户个人信息页
 * @param {string} userId 企业微信用户ID (wxUserid)
 */
export async function openUserProfile(userId) {
    // 开发环境模拟
    if (!isWxWorkEnv()) {
        console.log('模拟打开用户信息:', userId)
        alert(`[模拟] 打开用户信息页\n用户ID: ${userId}`)
        return
    }

    // 每次都检查 SDK 状态（内部会判断 URL 是否变化）
    const success = await initWxWorkJsSdk()
    if (!success) {
        console.error('JS-SDK 初始化失败，无法打开用户信息')
        return
    }

    // 调用企业微信接口打开个人信息页
    wx.invoke('openUserProfile', {
        type: 1, // 1 表示企业成员
        userid: userId
    }, (res) => {
        if (res.err_msg === 'openUserProfile:ok') {
            console.log('打开用户信息成功')
        } else {
            console.log('打开用户信息结果:', res)
        }
    })
}

/**
 * 企业微信登录
 * 检查 URL 中是否有 userid 参数（由上层服务传递）
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

    // 2. 检查 URL 中是否有 userid 参数（由上层服务传递）
    const urlParams = new URLSearchParams(window.location.search)
    const userid = urlParams.get('userid')
    const parentDept = urlParams.get('parentDept')  // 所属单位
    const dept = urlParams.get('dept')              // 所在部门

    console.log('[DEBUG] 当前 URL:', window.location.href)
    console.log('[DEBUG] URL 参数:', Object.fromEntries(urlParams))
    console.log('[DEBUG] userid:', userid, 'parentDept:', parentDept, 'dept:', dept)

    if (userid) {
        console.log('[DEBUG] 检测到 userid，正在调用后端 API...')
        try {
            const res = await request({
                url: '/mobile/wxwork/user/login',
                method: 'get',
                params: { userid }
            })

            console.log('[DEBUG] 后端返回结果:', res)

            if (res.code === 200 && res.data) {
                // 合并 URL 参数中的单位和部门信息
                const userData = {
                    ...res.data,
                    unit: parentDept || res.data.unit || '',
                    department: dept || res.data.department || ''
                }

                // 存储用户信息
                localStorage.setItem('forumUser', JSON.stringify(userData))

                // 清除 URL 中的参数
                urlParams.delete('userid')
                urlParams.delete('parentDept')
                urlParams.delete('dept')
                const newUrl = window.location.pathname +
                    (urlParams.toString() ? '?' + urlParams.toString() : '') +
                    window.location.hash

                window.history.replaceState({}, '', newUrl)

                console.log('用户登录成功:', userData)
                return userData
            } else {
                console.error('获取用户信息失败:', res)
            }
        } catch (e) {
            console.error('登录请求失败:', e)
        }
    }

    // 3. 非企业微信环境或没有 userid，使用模拟用户
    if (!isWxWorkEnv()) {
        console.log('非企业微信环境，使用模拟用户')
        return null
    }

    // 4. 企业微信环境但没有 userid，等待上层服务传递
    console.log('等待上层服务传递 userid...')
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
    openUserProfile,
    loginWithWxWork,
    getCurrentUser
}

