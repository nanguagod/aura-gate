# AuraGate Frontend

AuraGate is an enterprise-grade AI empowerment management platform. This frontend provides a modern, cyberpunk-themed interface for interacting with AI agents.

## Features

- **AI Super Agent**: A versatile AI assistant capable of handling various professional tasks and providing intelligent solutions
- Real-time streaming responses via SSE (Server-Sent Events)
- Responsive design with cyberpunk aesthetic

## Tech Stack

- Vue 3 (Composition API with `<script setup>`)
- Vue Router 4
- Axios for HTTP requests
- SSE for real-time communication
- Vite for build tooling

## Development

### Prerequisites

- Node.js >= 16.0.0
- npm >= 7.0.0

### Getting Started

```bash
npm install
npm run dev
```

### Build

```bash
npm run build
```

## API Endpoints

The frontend communicates with the backend via:

- `/api/ai/manus/chat` - AI Super Agent chat endpoint (SSE)

The backend service runs at `http://localhost:8123` by default.
