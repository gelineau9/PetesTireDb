# PetesTireDb

A command-line database management system built for Pete's Tire and Oil in Webster, MA. The system was designed to replace a hand-written inventory process — reducing the time and cost associated with manually tracking tire stock. The system was designed in my sophomore year at WSU and is due for sweeping changes when current high-priority projects are stable.

---

## Features

- **Role-based access control** — five permission tiers controlling which operations each user can perform
- **New tire inventory** — insert, delete, and search new tire stock with upsert logic (auto-increments quantity on duplicate entries)
- **Used tire inventory** — track used tires by ID, size, acquisition date, and months of use
- **Excel import/export** — load initial inventory from `.xlsx` files and export current stock using Apache POI
- **User management** — admins can create and delete MySQL users with appropriate privilege grants
- **PreparedStatement queries** — all DML operations use parameterized queries to prevent SQL injection on data inputs

---

## Access Levels

| Level | Role     | Permissions                                              |
|-------|----------|----------------------------------------------------------|
| 0     | Guest    | Login only (temporary/demo access)                       |
| 1     | Read     | Search new tires, search used tires                      |
| 2     | Staff    | Level 1 + insert and delete new/used tires               |
| 3     | Manager  | Level 2 + import inventory from Excel, export to Excel   |
| 4     | Admin    | Level 3 + create users, delete users, grant privileges   |

---

## Tech Stack

| Layer              | Technology                         |
|--------------------|------------------------------------|
| Language           | Java                               |
| Database           | MySQL                              |
| DB Connectivity    | JDBC (`java.sql`, `DriverManager`) |
| Excel I/O          | Apache POI (XSSFWorkbook)          |
| Interface          | CLI (console / `Scanner`)          |
| Build System       | Manual (see Setup)                 |

---

## Database Schema

### `newtire`
| Column     | Type    | Description                             |
|------------|---------|-----------------------------------------|
| `brand`    | VARCHAR | Tire manufacturer (e.g. Michelin, BFG)  |
| `Rnum`     | INT     | R-number / rim diameter (e.g. 15, 17)   |
| `size`     | VARCHAR | Tire size string (e.g. 205/55)          |
| `label`    | VARCHAR | Additional identifier / tag             |
| `quantity` | INT     | Current stock count                     |
| `extratag` | VARCHAR | Optional extra info                     |

### `usedtire`
| Column        | Type    | Description                        |
|---------------|---------|------------------------------------|
| `tireID`      | INT     | Primary key                        |
| `Rnum`        | INT     | R-number / rim diameter            |
| `size`        | VARCHAR | Tire size string                   |
| `dateAquired` | DATE    | Date tire was acquired             |
| `monthsUsed`  | INT     | Number of months the tire was used |

### `users`
| Column       | Type    | Description                              |
|--------------|---------|------------------------------------------|
| `userName`   | VARCHAR | Primary key — MySQL username             |
| `pass`       | VARCHAR | User password                            |
| `accessLevel`| INT     | Permission tier (0–4, see Access Levels) |

> Run `schema.sql` to initialize all three tables and a demo login. See [Setup](#setup) below.

---

## Project Structure

```
PetesTireDb/
├── javaFiles/
│   └── PetesTireDb.java      # Full application source
├── excelSheets/
│   └── PetesTire.xlsx        # Initial inventory from last hand-written record
├── schema.sql                # DDL to initialize the database
├── .gitignore
└── README.md
```

---

## Setup

### Prerequisites

- Java 8+
- MySQL 8+
- [Apache POI](https://poi.apache.org/) JARs (`poi`, `poi-ooxml`, and dependencies)
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) JAR

### 1. Initialize the database

```sql
-- In MySQL, create your database first:
CREATE DATABASE petestiredb;
USE petestiredb;

-- Then run the schema:
SOURCE schema.sql;
```

### 2. Configure the connection

Open `javaFiles/PetesTireDb.java` and update the connection string in the `getConnection` method:

```java
String url = "jdbc:mysql://localhost:3306/petestiredb";
```

Replace `3306` and `petestiredb` with your actual port and database name if different.

### 3. Compile

```bash
javac -cp ".:/path/to/mysql-connector.jar:/path/to/poi.jar:/path/to/poi-ooxml.jar" javaFiles/PetesTireDb.java
```

### 4. Run

```bash
java -cp ".:/path/to/mysql-connector.jar:/path/to/poi.jar:/path/to/poi-ooxml.jar" PetesTireDb
```

### 5. Log in

Use the demo credentials created by `schema.sql` to get started (access level 0 — guest). An admin user will need to be created directly in MySQL to unlock higher access levels on first use.

---

## Usage

After logging in, the program displays a menu of available operations based on your access level. Enter the number corresponding to the desired function and follow the prompts to supply the required inputs.

**Example session:**

```
Enter username: demo
Enter password: demoPass

Available functions:
  1. Search new tire
  2. Search used tire

Enter function number: 1
Enter brand: Michelin
Enter R-number: 17
Enter size: 225/45
Enter label: XL

Results:
  Brand: Michelin | R17 | 225/45 | XL | Qty: 4
```

---

## Planned Improvements

This project is being actively revisited as the client's needs grow beyond single-record querying. The following improvements are planned for the next iteration:

### Interface
- **GUI** — replace the CLI with a user-facing desktop or web interface as the system scales to more staff
- **Wildcard / partial search** — support `LIKE` queries so staff can search by partial brand name or size range
- **Pagination** — add `LIMIT`/`OFFSET` to prevent full-table dumps on large inventories

### Architecture
- **Build system** — migrate to Maven or Gradle to manage Apache POI and JDBC dependencies declaratively
- **External configuration** — move DB host, port, and name to a `config.properties` file so the source code does not need to be edited per environment
- **Connection pooling** — replace per-operation `DriverManager.getConnection()` calls with a connection pool (e.g. HikariCP) for better performance

### Data & Operations
- **Update / edit records** — add an operation to edit existing tire entries without requiring a delete and re-insert
- **Transaction support** — wrap multi-step operations like `loadExcel` (truncate + bulk insert) in a transaction to prevent partial failures from leaving the table empty

### Security
- **Password hashing** — hash stored passwords (SHA-256 or bcrypt) rather than storing plaintext
- **DDL input validation** — whitelist-validate user-supplied identifiers used in `CREATE USER`, `GRANT`, and `DROP USER` statements

### Quality
- **Logging** — replace `System.out.println` with a structured logging framework (e.g. SLF4J + Logback) for audit trails
- **Unit tests** — add JUnit tests for core query logic and input handling

---

## Background

Pete's Tire and Oil had been tracking inventory by hand in ledgers and spreadsheets. This system was built to digitize that process, seeding the database from the most recent hand-written Excel record (`excelSheets/PetesTire.xlsx`) and providing a structured, permission-controlled interface for day-to-day inventory management.
