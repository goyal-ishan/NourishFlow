# ♻️ Real-Time Cold-Storage Matching Network

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)

## 🎯 Project Objective
**The Real-Time Cold-Storage Matching Network** is an event-driven system designed to continuously monitor supermarket inventories, identify high-risk perishable goods (e.g., Raw Chicken, Milk), and stream this data in real-time to match surplus food with available cold-storage facilities before it spoils.

## 👋 The Problem I'm Trying to Solve
It’s crazy how much perfectly good food ends up in landfills every single day just because of bad logistics. Supermarkets often have surplus food that requires refrigeration (like milk or raw meat), but they lack a real-time way to find nearby cold-storage facilities or food banks before it spoils.

I'm building this project to fix that gap. **The Real-Time Cold-Storage Matching Network** acts as a live monitoring system. It watches a supermarket's inventory, instantly spots items that need a fridge, and streams that data out so the food can be matched with a storage facility before it goes bad.

## 🏗️ System Architecture (Current State)

This project leverages a modern, distributed microservices architecture. 

1. **Inventory Simulator (Producer):** A Spring Boot service that continuously generates randomized supermarket inventory data.
2. **Event Broker (Kafka):** Acts as the central nervous system, capturing high-throughput inventory streams.
3. **Data Persistence (PostgreSQL):** Relational database primed for storing matched cold-chain records and analytical data.
4. **Containerization:** The entire infrastructure (Zookeeper, Kafka Broker, Postgres Database) is locally orchestrated via Docker Compose.
5. **Client Interface:** A React frontend for real-time dashboard visualization.
## 🚀 Getting Started

Follow these steps to run the complete infrastructure and application locally.

### Prerequisites
* **Docker Desktop** installed and running on your machine.
* **Java 17+** installed.
* **Node.js & npm** installed.

### 1. Start Docker Desktop
Before running any commands, ensure the Docker Desktop application is open and running in the background. This is required to spin up our Kafka and PostgreSQL containers.

### 2. Boot Up the Infrastructure (Docker)
This project uses 3 main Docker files (`docker-compose.yml` in the root, plus a `Dockerfile` in both the backend and frontend folders). Open your terminal in the **root** folder of the project and run this command to build and start the containers:
```bash
docker compose up --build

```text
[ Supermarket Simulator ] 
       │ (Spring Boot)
       │ JSON Payload (Item, Weight, Refrigeration Needs)
       ▼
[ Apache Kafka ] ── Topic: food-inventory-stream
       │
       ▼
[ Future Consumers / Match Engine ] ---> [ PostgreSQL DB ]
