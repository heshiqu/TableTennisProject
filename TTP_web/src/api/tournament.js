import request from '@/utils/request'

// 比赛管理API
export function getTournaments(params) {
  return request({
    url: '/api/tournaments',
    method: 'get',
    params
  })
}

export function getTournamentDetail(id) {
  return request({
    url: `/api/tournaments/${id}`,
    method: 'get'
  })
}

export function registerTournament(tournamentId, group) {
  return request({
    url: `/api/tournaments/${tournamentId}/register`,
    method: 'post',
    data: { group }
  })
}

export function getTournamentSchedule(tournamentId) {
  return request({
    url: `/api/tournaments/${tournamentId}/schedule`,
    method: 'get'
  })
}

export function getMyTournaments() {
  return request({
    url: '/api/tournaments/my',
    method: 'get'
  })
}

// 月赛相关
export function getMonthlyTournament() {
  return request({
    url: '/api/tournaments/monthly',
    method: 'get'
  })
}

export function registerMonthlyTournament(group) {
  return request({
    url: '/api/tournaments/monthly/register',
    method: 'post',
    data: { group }
  })
}

// 获取比赛分组
export function getTournamentGroups() {
  return request({
    url: '/api/tournaments/groups',
    method: 'get'
  })
}

// 获取比赛列表（管理员/校区管理员）
export function getTournamentList(params) {
  return request({
    url: '/api/tournaments/list',
    method: 'get',
    params
  })
}

// 发布比赛
export function publishTournament(id) {
  return request({
    url: `/api/tournaments/${id}/publish`,
    method: 'post'
  })
}

// 截止报名
export function closeRegistration(id) {
  return request({
    url: `/api/tournaments/${id}/close-registration`,
    method: 'post'
  })
}

// 开始比赛
export function startTournament(id) {
  return request({
    url: `/api/tournaments/${id}/start`,
    method: 'post'
  })
}

// 结束比赛
export function endTournament(id) {
  return request({
    url: `/api/tournaments/${id}/end`,
    method: 'post'
  })
}

// 删除比赛
export function deleteTournament(id) {
  return request({
    url: `/api/tournaments/${id}`,
    method: 'delete'
  })
}