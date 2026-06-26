import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRouteStore = defineStore('route', () => {
  const dynamicRoutes = ref([])

  function setRoutes(routes) {
    dynamicRoutes.value = routes
  }

  function clearRoutes() {
    dynamicRoutes.value = []
  }

  return { dynamicRoutes, setRoutes, clearRoutes }
})
