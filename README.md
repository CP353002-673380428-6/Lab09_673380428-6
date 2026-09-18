> นี่คือเนื้อหาไฟล์ **`Readme.md`** ภาษาไทยฉบับสมบูรณ์ ที่จัดโครงสร้างอย่างเป็นระเบียบ ครอบคลุมทั้งรายละเอียดโจทย์, การประยุกต์ใช้ SOLID Principles, โครงสร้างตาราง (1:1 และ 1:N), Strategy Pattern และคู่มือการติดตั้ง สามารถคัดลอกไปวางทับในไฟล์ `Readme.md` ของโปรเจกต์ได้เลยครับ:

# 🛍️ Lab 8: Table Relationships — Product Shop

**วิชา:** CP353002 Principles of Software Design
**ผู้จัดทำ:** รหัสนักศึกษา `673380428-6` | กลุ่มเรียน (Section) `4`
**เทคโนโลยีหลัก:** Java 21 / Spring Boot 3.3.0 / Spring Data JPA / PostgreSQL / Thymeleaf

---

## 📋 1. วัตถุประสงค์ (Objectives)

1. เข้าใจและประยุกต์ใช้ความสัมพันธ์ของฐานข้อมูลแบบ **One-to-One (1:1)** และ **One-to-Many (1:N)** ผ่าน Spring Data JPA
2. ออกแบบ Entity, Service, Controller และ Pattern ให้สอดคล้องตามหลักการ **SOLID Principles 100%**
3. ประยุกต์ใช้ **Strategy Pattern** ร่วมกับ **Dependency Injection** ของ Spring Boot ในการคำนวณส่วนลดโดยไม่ละเมิด Open/Closed Principle (OCP)
4. พัฒนาระบบ CRUD (Create, Read, Update, Delete) จัดการข้อมูลที่เชื่อมโยงกันข้ามตารางพร้อม Cascade Operations

---

## 🛠️ 2. เทคโนโลยีที่ใช้ (Tech Stack)

* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot 3.3.0
  * Spring Web (MVC)
  * Spring Data JPA (Hibernate 6.5)
  * Thymeleaf (Template Engine)
  * Spring Boot DevTools
* **Database:** PostgreSQL 16
* **Build Tool:** Apache Maven 3.9+
* **Version Control:** Git & GitHub

---

## 🧠 3. สถาปัตยกรรมและหลักการออกแบบ (SOLID Principles)

โปรเจกต์นี้ได้รับการออกแบบตามหลักการของ **SOLID Principles** ในทุกเลเยอร์:

| ตัวย่อ | หลักการ (Principle) | การประยุกต์ใช้ในโปรเจกต์นี้ |
|:---:|---|---|
| **S** | **Single Responsibility (SRP)** | • แยก Entity: `Product`, `ProductDetail` และ `Review` แยกกันคนละตารางตามหน้าที่ของข้อมูล<br>• แยก Layer: `Controller` รับส่ง HTTP เท่านั้น, `Service` จัดการ Business Logic, `Repository` จัดการ Database, `DiscountContext` รับผิดชอบเฉพาะการคำนวณส่วนลด |
| **O** | **Open/Closed (OCP)** | • **Strategy Pattern:** ใช้ Spring Component Map Injection ใน `DiscountContext` ทำให้สามารถเพิ่มโปรโมชันส่วนลดใหม่ (เช่น `VIPDiscountStrategy`) ได้ทันทีเพียงสร้างคลาสใหม่ **โดยไม่ต้องแก้ไขโค้ดเดิมแม้แต่บรรทัดเดียว**<br>• การเพิ่มระบบ `Review` ทำได้โดยไม่ต้องแก้ไขโครงสร้างตารางหลักของ `Product` |
| **L** | **Liskov Substitution (LSP)** | • คลาสกลยุทธ์ส่วนลดทุกตัว (`NoDiscount`, `MemberDiscount`, `SeasonalSale`) สามารถใช้แทน Interface `DiscountStrategy` ได้สมบูรณ์โดยไม่ทำให้ระบบทำงานผิดพลาด<br>• Repository ทุกตัวสืบทอดและทำงานแทน `JpaRepository` ได้อย่างสมบูรณ์ |
| **I** | **Interface Segregation (ISP)** | • Interface `DiscountStrategy` มีเฉพาะเมธอด `applyDiscount()` ไม่ยัดเยียดเมธอดที่ไม่จำเป็น<br>• แยก Repository ชัดเจนตาม Entity (`ProductRepository`, `ProductDetailRepository`, `ReviewRepository`) ไม่รวมเป็น Interface ใหญ่ตัวเดียว |
| **D** | **Dependency Inversion (DIP)** | • ทุก Layer พึ่งพา Abstraction (Interface): `Controller` ➔ `Service` ➔ `Repository` (Interface)<br>• ใช้ **Constructor Injection** ในการเชื่อมโยง Dependencies ทั้งหมด<br>• `DiscountContext` พึ่งพา `Map<String, DiscountStrategy>` ที่ถูกฉีดเข้ามาโดย Spring Framework แทนการใช้คำสั่ง `new` เรียก Concrete Class โดยตรง |

---

## 🔗 4. ความสัมพันธ์ของตาราง (Table Relationships)

```
                       ┌──────────────────────┐
                       │    ProductDetail     │
                       │──────────────────────│
                       │ PK  id               │
                       │     description      │
                       │     warranty         │
                       │     weight           │
                       │     dimensions       │
                       │     manufactured_... │
                       └──────────▲───────────┘
                                  │ (1:1)
                       ┌──────────┴───────────┐
                       │       Product        │
                       │──────────────────────│
                       │ PK  id               │
                       │     name             │
                       │     category         │
                       │     brand            │
                       │     stock            │
                       │     price            │
                       │     discount_type    │
                       │ FK  detail_id        │──┐ (Owner 1:1)
                       └──────────┬───────────┘  │
                                  │ (1:N)        │
                                  ▼              │
                       ┌──────────────────────┐  │
                       │        Review        │  │
                       │──────────────────────│  │
                       │ PK  id               │  │
                       │     reviewer         │  │
                       │     rating           │  │
                       │     comment          │  │
                       │     review_date      │  │
                       │ FK  product_id       │◀─┘ (Many side เก็บ FK)
                       └──────────────────────┘
```

### 1) One-to-One (1:1): `Product` ↔ `ProductDetail`
* **แนวคิด:** ข้อมูลเชิงลึกของสินค้า (เช่น การรับประกัน, ขนาด, น้ำหนัก) ไม่จำเป็นต้องถูก Query ตลอดเวลา จึงแยกออกมาเพื่อความเป็น SRP
* **การตั้งค่า:** `Product` เป็นฝั่งเจ้าของความสัมพันธ์ (Owner Side) ถือ Foreign Key `detail_id` และกำหนด `cascade = CascadeType.ALL, orphanRemoval = true` เพื่อให้เมื่อลบสินค้า ข้อมูล Detail จะถูกลบตามทันที

### 2) One-to-Many (1:N): `Product` (1) ↔ `Review` (N)
* **แนวคิด:** สินค้า 1 รายการสามารถมีรีวิวได้หลายรายการ ไม่จำกัดจำนวน
* **การตั้งค่า:** ฝั่ง Many (`Review`) เป็นผู้เก็บ Foreign Key ชื่อ `product_id` ผ่าน `@ManyToOne` และฝั่ง `Product` เชื่อมโยงผ่าน `@OneToMany(mappedBy = "product")`

---

## 🎯 5. Strategy Pattern (คำนวณส่วนลด)

ระบบคำนวณส่วนลดได้รับการออกแบบให้เป็น Spring Beans เพื่อความยืดหยุ่นและถูกต้องตาม OCP 100%:

* **`DiscountStrategy` (Interface):** นิยามเมธอด `applyDiscount(double originalPrice)`
* **Concrete Strategies:**
  * `NoDiscountStrategy` (`@Component("NONE")`): ไม่ลดราคา
  * `MemberDiscountStrategy` (`@Component("MEMBER")`): ส่วนลดสมาชิก 10%
  * `SeasonalSaleStrategy` (`@Component("SEASONAL")`): ส่วนลดเทศกาล 20%
* **`DiscountContext` (Context Class):** รับ `Map<String, DiscountStrategy>` ผ่าน Constructor Injection และดึง Strategy มาใช้งานตาม Key โดยไม่ต้องพึ่งพา `switch-case` แบบ Hardcoded

---

## 📂 6. โครงสร้างโปรเจกต์ (Project Structure)

```
Lab08_673380428-6/
├── pom.xml
├── README.md
├── src/
│   └── main/
│       ├── java/com/example/demo/
│       │   ├── DemoApplication.java
│       │   ├── controller/
│       │   │   └── ProductController.java
│       │   ├── model/
│       │   │   ├── Product.java
│       │   │   ├── ProductDetail.java
│       │   │   └── Review.java
│       │   ├── repository/
│       │   │   ├── ProductRepository.java
│       │   │   ├── ProductDetailRepository.java
│       │   │   └── ReviewRepository.java
│       │   ├── service/
│       │   │   └── ProductService.java
│       │   └── strategy/
│       │       ├── DiscountStrategy.java
│       │       ├── DiscountContext.java
│       │       ├── NoDiscountStrategy.java
│       │       ├── MemberDiscountStrategy.java
│       │       └── SeasonalSaleStrategy.java
│       └── resources/
│           ├── application.properties
│           ├── static/css/
│           │   └── style.css
│           └── templates/products/
│               ├── list.html
│               ├── add.html
│               ├── edit.html
│               └── delete.html
```

---

## 🚀 7. การติดตั้งและเริ่มต้นใช้งาน (Getting Started)

### ขั้นตอนที่ 1: เตรียมฐานข้อมูล PostgreSQL
เปิดโปรแกรม **SQL Shell (psql)** หรือรันใน Terminal:
```sql
CREATE DATABASE lab8shop;
```

### ขั้นตอนที่ 2: ตั้งค่า `application.properties`
ตรวจสอบไฟล์ `src/main/resources/application.properties` ปรับแต่งรหัสผ่านฐานข้อมูลของคุณ:
```properties
spring.application.name=lab8-product-shop

spring.datasource.url=jdbc:postgresql://localhost:5432/lab8shop
spring.datasource.username=postgres
spring.datasource.password=12345678
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

### ขั้นตอนที่ 3: คอมไพล์และสั่งรันระบบ
เปิด Terminal ในโฟลเดอร์โปรเจกต์ แล้วรันคำสั่ง:
```bash
mvn clean spring-boot:run
```

---

## 🌐 8. ตารางเส้นทางระบบ (URL Mappings)

| HTTP Method | URL | หน้าที่การทำงาน |
|:---:|---|---|
| **GET** | `/` หรือ `/products` | หน้ารายการสินค้าทั้งหมด พร้อมข้อมูลสรุป 1:1, 1:N และราคาสุทธิ |
| **GET** | `/products/add` | หน้าฟอร์มเพิ่มสินค้าใหม่ (รวมกรอก ProductDetail และ Review แรก) |
| **POST** | `/products/save` | บันทึกข้อมูลสินค้าใหม่ลงฐานข้อมูล (Cascade ข้อมูลไปยังตารางลูก) |
| **GET** | `/products/edit/{id}` | แสดงฟอร์มแก้ไขข้อมูลสินค้าและ ProductDetail |
| **POST** | `/products/update/{id}`| อัปเดตข้อมูลสินค้า |
| **GET** | `/products/delete/{id}`| แสดงหน้ายืนยันการลบสินค้า |
| **POST** | `/products/delete/{id}`| สั่งลบสินค้า (ลบ ProductDetail อัตโนมัติด้วย CascadeType.ALL) |