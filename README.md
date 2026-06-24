# 🚀 UniTasker Enterprise

![Java](https://img.shields.io/badge/Java-17-orange.svg?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue.svg?style=for-the-badge&logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg?style=for-the-badge&logo=docker)
![Railway](https://img.shields.io/badge/Railway-Deployed-darkviolet.svg?style=for-the-badge&logo=railway)

An enterprise-grade, real-time task management platform designed to streamline productivity. Built with a robust Spring Boot backend, this project features secure JWT authentication, asynchronous OTP email verification, live WebSocket notifications, and is fully containerized with a multi-stage Docker build for seamless cloud deployment.

---

## ✨ Key Features

* **🔐 Advanced Security (Spring Security & JWT):** Stateless authentication architecture with BCrypt password hashing and role-based access control (Admin/User).
* **✉️ Asynchronous OTP Verification:** Integrated `JavaMailSender` for automated, non-blocking email verification during user registration.
* **⚡ Real-Time Collaboration:** Implemented Spring WebSockets (STOMP) to push instant live notifications to clients when tasks are created or updated.
* **🗄️ Relational Data Persistence:** Designed a scalable database schema using Hibernate/JPA with MySQL, ensuring strict data integrity and efficient querying.
* **☁️ Cloud-Native Deployment:** Engineered a Multi-Stage `Dockerfile` to optimize the build process, currently deployed and hosted 24/7 on the Railway cloud platform.

---

## 🛠️ Tech Stack

**Backend System:**
* Java 17
* Spring Boot (Web, Data JPA, Security, Mail, WebSocket)
* Maven (Dependency Management)

**Database & DevOps:**
* MySQL (Cloud Provisioned)
* Docker & Docker Compose
* Railway (CI/CD Pipeline & Hosting)

**Frontend Integration:**
* Vanilla JavaScript (Fetch API, WebSocket Listeners)
* HTML5 / CSS3 (Responsive UI/UX)

---


## 🚀 How to Run Locally

If you want to run this project on your local machine, follow these steps:

### Prerequisites
* Java 17 or higher
* Maven
* Docker Desktop (Optional, for containerized run)

### Method 1: Using Docker (Recommended)
Simply clone the repository and run the pre-configured compose file:
```bash
git clone [https://github.com/muhammetenesacar76-web/unitasker-enterprise.git](https://github.com/muhammetenesacar76-web/unitasker-enterprise.git)
cd unitasker-enterprise
docker-compose up --build
