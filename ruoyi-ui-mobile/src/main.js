import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import {
    Button,
    List,
    Cell,
    CellGroup,
    Image as VanImage,
    Icon,
    NavBar,
    Field,
    Popup,
    Popover,
    Loading,
    PullRefresh,
    Toast,
    Dialog,
    ActionSheet,
    Search,
    Empty,
    Skeleton
} from 'vant'
import 'vant/lib/index.css'
import './style.css'

// 路由配置
const routes = [
    { path: '/', redirect: '/posts' },
    { path: '/posts', name: 'PostList', component: () => import('./views/PostList.vue') },
    { path: '/post/search', name: 'PostSearch', component: () => import('./views/PostSearch.vue') },
    { path: '/post/:id', name: 'PostDetail', component: () => import('./views/PostDetail.vue') },
    { path: '/post/create', name: 'PostCreate', component: () => import('./views/PostCreate.vue') },
    { path: '/post/edit/:id', name: 'PostEdit', component: () => import('./views/PostCreate.vue') },
    { path: '/followed', name: 'FollowedPosts', component: () => import('./views/FollowedPosts.vue') },
    { path: '/my-posts', name: 'MyPosts', component: () => import('./views/MyPosts.vue') }
]

const router = createRouter({
    history: createWebHistory('/tongshiba/'),
    routes,
    scrollBehavior(to, from, savedPosition) {
        // PostList 页面使用 keep-alive 缓存，不需要 vue-router 控制滚动
        // 因为它有自己的自定义滚动容器
        if (to.name === 'PostList') {
            return false // 不做任何滚动操作
        }
        // 如果有保存的位置（如浏览器返回），恢复该位置
        if (savedPosition) {
            return savedPosition
        }
        // 否则滚动到顶部
        return { top: 0 }
    }
})

const app = createApp(App)

// 注册 Vant 组件
app.use(Button)
app.use(List)
app.use(Cell)
app.use(CellGroup)
app.use(VanImage)
app.use(Icon)
app.use(NavBar)
app.use(Field)
app.use(Popup)
app.use(Loading)
app.use(PullRefresh)
app.use(Toast)
app.use(Dialog)
app.use(ActionSheet)
app.use(Popover)
app.use(Search)
app.use(Empty)
app.use(Skeleton)

app.use(router)
app.mount('#app')
