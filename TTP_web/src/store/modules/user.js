import { login as apiLogin, logout as apiLogout, getCurrentUser, register as apiRegister, registerStudent as apiRegisterStudent, registerCoach as apiRegisterCoach, checkRole } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { resetRouter } from '@/router'

const getDefaultState = () => {
  return {
    token: getToken(),
    user: null,
    name: '',
    avatar: '',
    roles: [],
    permissions: []
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_USER: (state, user) => {
    state.user = user
    state.name = user?.realName || user?.username || ''
    state.avatar = user?.avatar || ''
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_PERMISSIONS: (state, permissions) => {
    state.permissions = permissions
  }
}

const actions = {
  // user login
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      apiLogin({ username: username.trim(), password: password }).then(response => {
        const { data } = response
        commit('SET_TOKEN', data.token)
        setToken(data.token)
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getCurrentUser().then(response => {
        const { data } = response

        if (!data) {
          return reject('获取用户信息失败，请重新登录')
        }

        commit('SET_USER', data)
        
        // 获取用户角色
        checkRole().then(roleResponse => {
          const roleData = roleResponse.data
          commit('SET_ROLES', [roleData])
          resolve(data)
        }).catch(() => {
          commit('SET_ROLES', ['USER'])
          resolve(data)
        })
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user register
  register({ commit }, registerInfo) {
    return new Promise((resolve, reject) => {
      apiRegister(registerInfo).then(response => {
        resolve(response)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // register student
  registerStudent({ commit }, registerInfo) {
    return new Promise((resolve, reject) => {
      apiRegisterStudent(registerInfo).then(response => {
        resolve(response)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // register coach
  registerCoach({ commit }, registerInfo) {
    return new Promise((resolve, reject) => {
      apiRegisterCoach(registerInfo).then(response => {
        resolve(response)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      apiLogout().then(() => {
        removeToken()
        resetRouter()
        commit('RESET_STATE')
        resolve()
      }).catch(error => {
        removeToken()
        resetRouter()
        commit('RESET_STATE')
        resolve()
      })
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken()
      commit('RESET_STATE')
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

