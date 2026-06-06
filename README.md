# ♟️ Real-Time Multiplayer Chess

A real-time multiplayer chess application that enables two players to play chess online with live move synchronization, legal move validation, and an interactive user experience.

## 🚀 Features

* Real-time multiplayer gameplay
* Live move synchronization using WebSockets
* Interactive chessboard UI
* Legal move validation
* Turn-based gameplay
* Responsive design
* Modern user interface
* Scalable backend architecture

## 🛠️ Tech Stack

### Frontend

* React.js
* JavaScript
* Tailwind CSS
* Chessboard.js / React Chessboard

### Backend

* Java
* Spring Boot
* Spring WebSocket
* STOMP Protocol

### Database

* PostgreSQL / MySQL (if used)

### Other Tools

* Maven
* Git & GitHub

## 🏗️ System Architecture

```text
Player A
    │
    ▼
React Frontend
    │
WebSocket (STOMP)
    │
Spring Boot Backend
    │
Chess Game Engine
    │
WebSocket Broadcast
    │
React Frontend
    ▲
    │
Player B
```

## ⚡ Getting Started

### Clone Repository

```bash
git clone https://github.com/Tejascodez/real-time-chess-multiplayer.git
cd real-time-chess-multiplayer
```

### Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start on:

```text
http://localhost:8080
```

### Frontend Setup

```bash
cd frontend
npm install
npm start
```

Frontend will start on:

```text
http://localhost:3000
```

## 🎮 How It Works

1. Players join the same game room.
2. WebSocket connection is established with the Spring Boot server.
3. Moves are validated and processed by the backend.
4. Updated board state is broadcast instantly to both players.
5. Players continue until checkmate, stalemate, or draw.

## 📚 Key Concepts Implemented

* WebSocket Communication
* Real-Time Event Handling
* Multiplayer Game Synchronization
* Spring Boot Backend Development
* React State Management
* Chess Rules Enforcement
* Client-Server Architecture

## 🔥 Challenges Solved

* Synchronizing board state across multiple clients
* Managing player turns
* Preventing illegal moves
* Maintaining low-latency communication
* Handling game session lifecycle

## 🚀 Future Improvements

* User Authentication & Authorization
* Matchmaking System
* ELO Rating System
* Spectator Mode
* In-Game Chat
* Match History
* Tournament Support


## 💼 Why This Project?

This project demonstrates:

* Full Stack Development
* Java Spring Boot Development
* WebSocket Programming
* Real-Time Systems Design
* Frontend-Backend Integration
* Problem Solving and System Design

## 👨‍💻 Author

**Tejas Patil**

Java Full Stack Developer passionate about building scalable applications, real-time systems, and modern web experiences.

GitHub: https://github.com/Tejascodez
