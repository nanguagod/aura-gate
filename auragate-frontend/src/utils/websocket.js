import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient = null

/**
 * Connect to WebSocket (STOMP over SockJS)
 * @param {Function} onMessage callback for incoming messages
 * @param {string} userId user ID for queue subscription
 * @returns {Promise}
 */
export function connectWebSocket(onMessage, userId) {
  return new Promise((resolve, reject) => {
    stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        // Subscribe to user-specific queue
        stompClient.subscribe(`/user/${userId}/queue/messages`, (msg) => {
          if (onMessage) onMessage(JSON.parse(msg.body))
        })
        resolve(stompClient)
      },
      onStompError: (frame) => {
        reject(new Error(frame.headers.message))
      },
    })
    stompClient.activate()
  })
}

/**
 * Disconnect WebSocket
 */
export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

/**
 * Send a message via STOMP
 * @param {string} destination
 * @param {object} body
 */
export function sendMessage(destination, body) {
  if (stompClient?.connected) {
    stompClient.publish({
      destination,
      body: JSON.stringify(body),
    })
  }
}

/**
 * Connect to raw AI WebSocket (/ws/ai)
 * 使用相对 URL（通过 Vite proxy），无需 hardcode localhost
 * @param {Function} onToken 每个 token 片段的回调
 * @param {Function} onDone 完成回调（可选）
 * @param {Function} onError 错误回调（可选）
 * @returns {WebSocket}
 */
export function connectAiWebSocket(onToken, onDone, onError) {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const token = localStorage.getItem('token')
  const wsUrl = `${protocol}//${window.location.host}/ws/ai?token=${token || ''}`

  const ws = new WebSocket(wsUrl)

  ws.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      if (onDone) onDone()
      return
    }
    if (onToken) onToken(data)
  }

  ws.onerror = (err) => {
    console.error('AI WebSocket 错误:', err)
    if (onError) onError(err)
  }

  ws.onclose = (event) => {
    if (event.code !== 1000 && onError) {
      onError(new Error(`WebSocket 异常关闭: code=${event.code}`))
    }
  }

  return ws
}
