> นี่คือเนื้อหาไฟล์ **`README.md`** ภาษาไทยฉบับสมบูรณ์สำหรับ **Lab 9: Spring Boot - Transaction** ครับ จัดรูปแบบ Markdown สวยงาม ครอบคลุมตั้งแต่สถาปัตยกรรม, การตั้งค่า, API, การทดลองเปรียบเทียบ Rollback ไปจนถึงเฉลยคำถามท้ายแล็บ สามารถคัดลอกไปวางในไฟล์ `README.md` ของโปรเจกต์ได้ทันทีครับ

# 🏦 Lab 9: Spring Boot — Transaction Management

**วิชา:** CP353002 Principles of Software Design
**ผู้จัดทำ:** นายสิทธิโชค (Sitthichok) | รหัสนักศึกษา: `673380428-6`
**เทคโนโลยีหลัก:** Java 21 / Spring Boot 3.3.x / Spring Data JPA / PostgreSQL / REST API

---

## 📋 1. วัตถุประสงค์ของ Lab

1. เข้าใจหลักการทำงานของ **Database Transaction** ตามคุณสมบัติ **ACID (โดยเฉพาะ Atomicity)**
2. สามารถประยุกต์ใช้ Annotation `@Transactional` ใน Spring Boot เพื่อควบคุมขอบเขตของ Transaction
3. เข้าใจและสามารถอธิบายความแตกต่างระหว่างกระบวนการ **COMMIT** และ **ROLLBACK** ได้อย่างชัดเจน
4. พัฒนา REST API ตามสถาปัตยกรรมแบบแยกชั้น (Layered Architecture: Model, Repository, Service, Controller)
5. สร้างความสัมพันธ์ของตารางแบบ **One-to-Many (1:N)** ในทิศทางเดียว (Unidirectional) เพื่อป้องกันปัญหา JSON Infinite Recursion

---

## 🛠️ 2. เทคโนโลยีที่ใช้ (Tech Stack)

* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot 3.3.x
  * **Spring Web:** พัฒนา RESTful Web Services
  * **Spring Data JPA:** จัดการข้อมูลด้วย ORM (Hibernate) และควบคุม Transaction
* **Database:** PostgreSQL 16
* **Build Tool:** Apache Maven (Maven Wrapper: `./mvnw`)
* **Testing Tools:** Postman / cURL / pgAdmin 4

---

## 🗄️ 3. โครงสร้างฐานข้อมูลและความสัมพันธ์ (Database Schema)

ระบบมีทั้งหมด 2 ตาราง โดยมีความสัมพันธ์แบบ **One-to-Many (1:N)**:

```text
       ┌────────────────────────┐
       │        account         │
       ├────────────────────────┤
       │ PK  id                 │
       │     account_number     │
       │     owner_name         │
       │     balance            │
       └───────────▲────────────┘
                   │ 1
                   │
                   │ *
       ┌───────────┴────────────┐
       │  deposit_transaction   │
       ├────────────────────────┤
       │ PK  id                 │
       │     amount             │
       │ FK  account_id         │ (Many-to-One ชี้ไปที่ Account)
       └────────────────────────┘
```

> **📌 ข้อควรระวังในการออกแบบ (Design Consideration):**
> ความสัมพันธ์ถูกประกาศไว้ที่ฝั่ง **`DepositTransaction` เท่านั้น** (`@ManyToOne`) โดยในคลาส `Account` **ไม่มีการเก็บ `List<DepositTransaction>`** เพื่อป้องกันปัญหา **JSON Infinite Recursion** (การวนลูปไม่รู้จบขณะแปลง Object เป็น JSON ตอนส่งผ่าน REST API)

---

## 🧠 4. กลไกการทำงานของ `@Transactional`

การฝากเงิน 1 ครั้ง ประกอบไปด้วย 2 ขั้นตอนสำคัญ:
1. **เพิ่มยอดเงินในบัญชี (Update Balance)**
2. **บันทึกประวัติการฝากเงิน (Insert DepositTransaction)**

```text
[เริ่ม Transaction]
        │
        ▼
   ค้นหา Account
        │
        ▼
   เพิ่ม balance ใน Account (save)
        │
        ▼
   บันทึกประวัติ DepositTransaction (save)
        │
        ├───────────────────────────────┐
        ▼ (สำเร็จทุกขั้นตอน)            ▼ (เกิด Exception กลางคัน)
    [ COMMIT ]                     [ ROLLBACK ]
บันทึกข้อมูลลงฐานข้อมูลถาวร     ย้อนคืนสถานะข้อมูลเดิมทั้งหมด
```

* **มี `@Transactional`:** ทั้งสองขั้นตอนจะนับเป็น **"งานก้อนเดียวกัน (Atomic Unit)"** หากเกิด RuntimeException ระบบจะทำการ **Rollback** ย้อนกลับข้อมูลให้ทั้งหมด ยอดเงินจะไม่เพิ่มและไม่มีประวัติค้าง
* **ไม่มี `@Transactional`:** คำสั่ง `save()` จะแยกกันบันทึกทันทีทีละคำสั่ง (Autocommit) หากเกิด Exception ตามหลัง ยอดเงินจะเพิ่มค้างไว้แต่ไม่มีประวัติ หรือเกิดภาวะข้อมูลไม่สอดคล้องกัน (**Inconsistent State**)

---

## 📂 5. โครงสร้างโปรเจกต์ (Project Structure)

```text
src/main/java/com/example/lab9/
├── Lab9Application.java
├── model/
│   ├── Account.java                ← Entity บัญชีธนาคาร
│   └── DepositTransaction.java     ← Entity รายการฝากเงิน (@ManyToOne)
├── repository/
│   ├── AccountRepository.java      ← extends JpaRepository
│   └── DepositRepository.java      ← extends JpaRepository
├── service/
│   ├── AccountService.java         ← Business Logic จัดการบัญชี
│   └── DepositService.java         ← ควบคุม @Transactional สำหรับการฝากเงิน
└── controller/
    └── AccountController.java      ← REST API Endpoints (Constructor Injection)
```

---

## 🚀 6. การติดตั้งและเริ่มต้นใช้งาน (Getting Started)

### 1) สร้างฐานข้อมูลใน PostgreSQL
เปิด SQL Shell (`psql`) หรือ pgAdmin แล้วรันคำสั่ง:
```sql
CREATE DATABASE lab9;
```

### 2) ตั้งค่า `application.properties`
ตรวจสอบไฟล์ `src/main/resources/application.properties`:
```properties
spring.application.name=lab9

# PostgreSQL Configuration (แก้ password ให้ตรงกับเครื่อง)
spring.datasource.url=jdbc:postgresql://localhost:5432/lab9
spring.datasource.username=postgres
spring.datasource.password=12345678

# JPA Settings
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 3) สั่งรันโปรแกรม
```bash
./mvnw spring-boot:run
```

---

## 🌐 7. ตาราง REST API Endpoints

| HTTP Method | URL | คำอธิบาย | ข้อมูลที่ส่ง (Request Body) |
|:---:|---|---|---|
| **POST** | `/accounts` | สร้างบัญชีธนาคารใหม่ | `{"accountNumber":"1234567890", "ownerName":"Sitthichok", "balance":0}` |
| **GET** | `/accounts/{id}` | ค้นหาและดูข้อมูลบัญชี | *(ไม่ต้องระบุ)* |
| **POST** | `/accounts/{id}/deposit` | ฝากเงินเข้าบัญชี (`@Transactional`) | `{"amount": 1000}` |

---

## 🧪 8. ผลการทดลองและการเปรียบเทียบ (Experiment Results)

### การทดลองที่ 1: การทำงานตามปกติ (Normal Execution)
* ทำการฝากเงินจำนวน 1,000 บาท เข้า Account ID `1`
* **ผลลัพธ์:** ได้รับสถานะ `200 OK`
* **การตรวจสอบ:**
  * เรียก `GET /accounts/1` ➔ `balance` อัปเดตจาก `0.0` เป็น `1000.0`
  * ตรวจสอบฐานข้อมูล `SELECT * FROM deposit_transaction;` ➔ มีข้อมูล 1 แถวถูกต้อง

---

### การทดลองที่ 2: เปรียบเทียบผลลัพธ์ Rollback เมื่อเกิด Error
*(จำลองข้อผิดพลาดด้วยคำสั่ง `throw new RuntimeException("Test Rollback");` ที่บรรทัดสุดท้ายของเมธอด `deposit()`)*

| หัวข้อการทดสอบ | แบบที่ 1: มี `@Transactional` (ข้อ 12) | แบบที่ 2: ไม่มี `@Transactional` (ข้อ 13) |
|---|:---:|:---:|
| **สถานะตอบกลับ (HTTP Status)** | `500 Internal Server Error` | `500 Internal Server Error` |
| **ยอดเงินในบัญชี (`balance`)** | **`1000.0` (เท่าเดิม)** | **`2000.0` (ยอดเงินเพิ่มขึ้น)** |
| **จำนวนแถวใน `deposit_transaction`** | **1 แถว (เท่าเดิม)** | **2 แถว (มีรายการใหม่ถูกบันทึก)** |
| **พฤติกรรมของฐานข้อมูล** | **ROLLBACK สมบูรณ์**<br>ย้อนคืนสถานะข้อมูลเดิมทั้งหมด | **ไม่มีการ ROLLBACK (เกิด Partial Update)**<br>ข้อมูลถูกบันทึกค้างไว้แม้ระบบพัง |
| **ความสมบูรณ์ของข้อมูล** | ✅ สอดคล้องถูกต้อง (Consistent) | ❌ เสียหายและไม่สอดคล้องกัน (Inconsistent) |

---

## 👤 9. ข้อมูลผู้จัดทำ

* **ชื่อ-นามสกุล:** นายสิทธิโชค มุขนาค
* **รหัสนักศึกษา:** `673380428-6`
* **วิชา:** CP353002 หลักการออกแบบซอฟต์แวร์ (Principles of Software Design)
* **สถาบัน:** มหาวิทยาลัยขอนแก่น (Khon Kaen University)