import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const TokenKeyLS = 'admin_token_ls'

export function getToken() {
  const cookieToken = Cookies.get(TokenKey)
  if (cookieToken) return cookieToken
  
  const lsToken = localStorage.getItem(TokenKeyLS)
  if (lsToken) {
    Cookies.set(TokenKey, lsToken, { path: '/' })
    return lsToken
  }
  
  return null
}

export function setToken(token) {
  Cookies.set(TokenKey, token, { path: '/' })
  localStorage.setItem(TokenKeyLS, token)
  return token
}

export function removeToken() {
  Cookies.remove(TokenKey, { path: '/' })
  localStorage.removeItem(TokenKeyLS)
}
