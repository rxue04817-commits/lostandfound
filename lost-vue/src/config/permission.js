/**
 * 角色等级定义
 * 0: 普通用户
 * 1: 普通管理员
 * 2: 超级管理员
 */
export const ROLES = {
  USER: 0,           // 普通用户
  ADMIN: 1,          // 普通管理员
  SUPER_ADMIN: 2     // 超级管理员
}

/**
 * 角色名称映射
 */
export const ROLE_NAMES = {
  0: '普通用户',
  1: '普通管理员',
  2: '超级管理员'
}

/**
 * 菜单配置
 * 每个菜单项包含：
 * - path: 路由路径
 * - title: 菜单标题
 * - icon: 图标名称（Element Plus图标）
 * - minRole: 最低角色等级要求
 * - children: 子菜单（可选）
 */
export const MENU_CONFIG = [
  // 普通用户功能
  {
    path: '/publish',
    title: '发布信息',
    icon: 'Plus',
    minRole: ROLES.USER
  },
  {
    path: '/my-posts',
    title: '查看我的发布',
    icon: 'Document',
    minRole: ROLES.USER
  },
  {
    path: '/all-posts',
    title: '信息浏览',
    icon: 'Document',
    minRole: ROLES.USER
  },
  {
    path: '/my-claims',
    title: '我的认领',
    icon: 'Message',
    minRole: ROLES.USER
  },
  {
    path: '/my-donations',
    title: '我的打赏',
    icon: 'DataAnalysis',
    minRole: ROLES.USER
  },
  {
    path: '/claim-manage',
    title: '认领管理',
    icon: 'Check',
    minRole: ROLES.USER,
    // 特殊条件：只有当用户有拾物时才显示（这个在组件中动态判断）
    //specialCondition: 'hasFoundItem'
  },
  {
    path: '/profile',
    title: '修改个人信息',
    icon: 'User',
    minRole: ROLES.USER
  },
  {
    path: '/statistics',
    title: '数据统计',
    icon: 'DataAnalysis',
    minRole: ROLES.USER  // 所有用户都可以访问，但看到的数据不同
  },
  
  // 管理员功能（role >= 1）
  {
    path: '/admin',
    title: '管理员审核',
    icon: 'DataAnalysis',
    minRole: ROLES.ADMIN
  },
  
  // 超级管理员功能（role >= 2）
  {
    path: '/admin/users', 
    title: '用户管理',
    icon: 'UserFilled',
    minRole: ROLES.SUPER_ADMIN
  },
  {
    path: '/admin/donation-statistics',
    title: '打赏统计',
    icon: 'DataAnalysis',
    minRole: ROLES.SUPER_ADMIN
  },
  {
    path: '/admin/donation-orders',
    title: '打赏订单',
    icon: 'Document',
    minRole: ROLES.SUPER_ADMIN
  }
]

/**
 * 根据用户角色过滤菜单
 * @param {number} userRole - 用户角色等级
 * @param {object} conditions - 特殊条件对象，如 { hasFoundItem: true }
 * @returns {array} 过滤后的菜单列表
 */
export function filterMenusByRole(userRole, conditions = {}) {
  return MENU_CONFIG.filter(menu => {
    // 检查角色等级
    if (userRole < menu.minRole) {
      return false
    }
    
    // 检查特殊条件
    if (menu.specialCondition) {
      return conditions[menu.specialCondition] === true
    }
    
    return true
  })
}

/**
 * 检查用户是否有访问指定路径的权限
 * @param {number} userRole - 用户角色等级
 * @param {string} path - 路由路径
 * @returns {boolean} 是否有权限
 */
export function checkPermission(userRole, path) {
  const menu = MENU_CONFIG.find(m => m.path === path)
  if (!menu) {
    // 如果路径不在菜单配置中，默认允许访问（如首页等）
    return true
  }
  return userRole >= menu.minRole
}
