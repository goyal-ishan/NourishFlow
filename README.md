# ♻️ NourishFlow

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)

## 🎯 Project Objective
**NourishFlow** is an event driven system would constantly stream real time information about inventory in supermarkets, look out for items prone to be high-risk (e.g Baked Foods, Milk, Paneer, Vegetables, Raw Chicken etc.) and match these items against cold-storage availability to prevent food waste due to spoilage.

## 👋 The Problem I'm Trying to Solve
Daily on a lot of good eatable food products are thrown away simply because of a logistic problem which is completely insane!!! Food products like Milk, Paneer need refrigeration but there's no way they can verify if there's a local cold storage or food bank to keep these products before they are expired.

So, my project tries to address this problem as essentially this is the real time monitor (NorishFlow) of the supermarkets, where they scan the inventory and send alerts if the products need to be refrigerated live to a storage unit. Which in turn would pick it up before it goes bad.

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
