/**
 * 企业微信工具类
 */

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

// 初始化企业微信 JS-SDK（后续实现）
export async function initWxWorkJsSdk() {
    // TODO: 实现企业微信 JS-SDK 初始化
    console.log('企业微信 JS-SDK 初始化')
}

// 调用企业微信选人接口进行分享
export async function shareToUsers(title, url) {
    // TODO: 调用 wx.invoke('shareToExternalContact', ...) 或 wx.invoke('selectEnterpriseContact', ...)
    console.log('分享给用户:', title, url)
}

export default {
    isWxWorkEnv,
    isWeChatEnv,
    getAccessDeniedMessage,
    getMockUser,
    initWxWorkJsSdk,
    shareToUsers
}
