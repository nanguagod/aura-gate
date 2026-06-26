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
 * @param {string} userId
 * @param {Function} onToken callback for each token char
 * @returns {WebSocket}
 */
export function connectAiWebSocket(userId, onToken) {
  let token = localStorage.getItem('token')
  const ws = new WebSocket(`ws://localhost:8080/ws/ai?id=${userId}&token=${token}`)

  ws.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      if (onToken) onToken('\n\n--- 对话结束 ---')
      return
    }
    if (onToken) onToken(data)
  }

  ws.onerror = (err) => {
    console.error('AI WebSocket 错误:', err)
  }

  ws.onclose = () => {
    console.log('AI WebSocket 已关闭')
  }

  return ws
}
