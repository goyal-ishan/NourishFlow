# ♻️ NourishFlow

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)

## 🎯 Project Objective
**NourishFlow** is an event-driven system designed to continuously monitor supermarket inventories, identify high-risk perishable goods (e.g.,Baked Foods, Milk,Paneer,Vegetables,Raw Chicken,etc), and stream this data in real-time to match surplus food with available cold-storage facilities before it spoils.

## 👋 The Problem I'm Trying to Solve
It’s crazy how much perfectly good food ends up in landfills every single day just because of bad logistics. Supermarkets often have surplus food that requires refrigeration (like milk or paneer), but they lack a real-time way to find nearby cold-storage facilities or food banks before it spoils.

I'm building this project to fix that gap. **NorishFlow** acts as a live monitoring system. It watches a supermarket's inventory, instantly spots items that need a fridge, and streams that data out so the food can be matched with a storage facility before it goes bad.

## ✨ Key Features
* **Event-Driven Architecture:** Uses Apache Kafka to handle high-throughput inventory data streams.
* **Real-Time Processing:** Instantly identifies items requiring refrigeration.
* **Full-Stack Containerization:** Frontend, backend, and infrastructure all orchestrated via Docker Compose.
* **Interactive Dashboard:** React-based UI for visualizing the cold-chain matching process.

## 📸 Screenshots


| Dashboard View 1 | Dashboard View 2 |
| :---: | :---: |
| ![Dashboard1](./screenshots/Live-matched_1.png) | ![Dashboard2](./screenshots/Live-matches2.png) |
| **Impact Analytics** | **Architecture Flow** |
| ![Analytics](./screenshots/Impact%20Analytics.png) | ![Flow](./screenshots/System-architecture.png) |

## 🏗️ System Architecture 

This project leverages a modern, distributed microservices architecture. 

1. **Inventory Simulator (Producer):** A Spring Boot service that continuously generates randomized supermarket inventory data.
2. **Event Broker (Kafka):** Acts as the central nervous system, capturing high-throughput inventory streams.
3. **Data Persistence (PostgreSQL):** Relational database primed for storing matched cold-chain records and analytical data.
4. **Containerization:** The entire infrastructure (Zookeeper, Kafka Broker, Postgres Database) is locally orchestrated via Docker Compose.
5. **Client Interface:** A React frontend for real-time dashboard visualization.

```
[ Supermarket Simulator ] 
       │ (Spring Boot)
       │ JSON Payload (Item, Weight, Refrigeration Needs)
       ▼
[ Apache Kafka ] ── Topic: food-inventory-stream
       │
       ▼
[ Future Consumers / Match Engine ] ---> [ PostgreSQL DB ]
```
🚀 Execution Guide (How to Start)

1. Spin up the Infrastructure (Docker)

```
-docker compose up --build
```
2. Start the Spring Boot Backend

```
cd backend
./mvnw spring-boot:run
```
3. Start the React Dashboard
Finally, launch the frontend to visualize the live data matching:
```
cd frontend
npm install
npm start
```
