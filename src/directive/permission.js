import { useUserStore } from '@/stores'

export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const roles = userStore.roles

    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = roles.some((role) => value.includes(role))

      if (!hasPermission) {
        el.style.display = 'none'
        el.remove()
      }
    } else {
      throw new Error('权限指令需要传入角色数组')
    }
  },
}
