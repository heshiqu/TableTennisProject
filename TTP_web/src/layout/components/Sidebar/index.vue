<template>
  <div :class="{'has-logo':showLogo}">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :unique-opened="false"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path" />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.scss'

export default {
  components: { SidebarItem, Logo },
  computed: {
    ...mapGetters([
      'sidebar',
      'roles'
    ]),
    routes() {
      return this.filterRoutesByRole(this.$router.options.routes)
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    showLogo() {
      return this.$store.state.settings.sidebarLogo
    },
    variables() {
      return variables
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  },
  methods: {
    filterRoutesByRole(routes) {
      if (!Array.isArray(routes)) return []
      
      return routes.filter(route => {
        if (route.hidden) return false
        
        // 检查路由是否需要权限
        if (route.meta && route.meta.roles) {
          // 检查用户是否有路由要求的角色
          return route.meta.roles.some(role => this.roles.includes(role))
        }
        
        // 如果没有roles字段，则所有用户都可以访问
        return true
      }).map(route => {
        // 递归过滤子路由
        if (route.children) {
          return {
            ...route,
            children: this.filterRoutesByRole(route.children)
          }
        }
        return route
      })
    }
  }
}
</script>
