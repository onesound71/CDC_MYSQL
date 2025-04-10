# MySQL CDC to MySQL Demo

이 프로젝트는 Apache Flink의 오픈소스 CDC(Change Data Capture) 커넥터를 사용하여 MySQL에서 MySQL로 실시간 데이터 동기화를 구현한 데모입니다. 로컬 MySQL에서 Google Cloud MySQL, Amazon RDS 등 다양한 MySQL 호환 데이터베이스로의 동기화도 지원합니다.

## 프로젝트 정보

- **기반 기술**: Apache Flink (오픈소스)
- **CDC 커넥터**: Flink CDC Connector (오픈소스)
- **라이선스**: Apache License 2.0
- **GitHub 저장소**: [Flink CDC](https://github.com/ververica/flink-cdc-connectors)

## 지원되는 대상 데이터베이스

- 로컬 MySQL
- Google Cloud SQL for MySQL
- Amazon RDS for MySQL
- Azure Database for MySQL
- 기타 MySQL 호환 데이터베이스

## Google Cloud MySQL 동기화 설정

### 1. Google Cloud MySQL 연결 설정
```java
JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
    .withUrl("jdbc:mysql://[INSTANCE_IP]/[DATABASE_NAME]")
    .withDriverName("com.mysql.cj.jdbc.Driver")
    .withUsername("[USERNAME]")
    .withPassword("[PASSWORD]")
    .build()
```

### 2. Google Cloud MySQL 접속 정보
- **인스턴스 IP**: Google Cloud SQL 인스턴스의 IP 주소
- **데이터베이스**: 동기화할 데이터베이스 이름
- **사용자**: Cloud SQL 사용자 계정
- **비밀번호**: Cloud SQL 사용자 비밀번호

### 3. Google Cloud MySQL 접근 설정
1. Cloud SQL 인스턴스의 공개 IP 활성화
2. 방화벽 규칙 설정 (3306 포트 허용)
3. SSL 연결 설정 (권장)

### 4. 성능 고려사항
- 네트워크 대기 시간 최적화
- 배치 크기 조정
- Cloud SQL 인스턴스 크기 선택
- 지역 간 데이터 전송 비용 고려

## 요구사항

- Docker
- Java 11
- Gradle 8.0

## Docker Compose 환경 설명

### 1. 서비스 구성
```yaml
services:
  mysql-source:
    image: mysql:8.0
    container_name: mysql-source
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: source_db
    ports:
      - "3306:3306"
    volumes:
      - ./init/source:/docker-entrypoint-initdb.d
    command: --server-id=1 --log-bin=mysql-bin --binlog-format=ROW

  mysql-sink:
    image: mysql:8.0
    container_name: mysql-sink
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: sink_db
    ports:
      - "3307:3306"
    volumes:
      - ./init/sink:/docker-entrypoint-initdb.d
```

### 2. 환경 상세 설명

#### 2.1 MySQL Source 서버
- **포트**: 3306 (기본 MySQL 포트)
- **데이터베이스**: source_db
- **초기화 스크립트**: `init/source/init.sql`
- **CDC 설정**:
  - `server-id=1`: MySQL 복제를 위한 고유 식별자
  - `log-bin=mysql-bin`: 바이너리 로깅 활성화
  - `binlog-format=ROW`: 행 기반 복제 설정

#### 2.2 MySQL Sink 서버
- **포트**: 3307 (소스와 구분을 위한 포트)
- **데이터베이스**: sink_db
- **초기화 스크립트**: `init/sink/init.sql`

### 3. 볼륨 마운트
- 소스 DB 초기화 스크립트: `./init/source:/docker-entrypoint-initdb.d`
- 싱크 DB 초기화 스크립트: `./init/sink:/docker-entrypoint-initdb.d`

### 4. 네트워크 구성
- 소스 DB: `localhost:3306`
- 싱크 DB: `localhost:3307`
- 컨테이너 간 통신: Docker 내부 네트워크 사용

## 테스트 데이터베이스 구성

### 1. 소스 데이터베이스 (source_db)
```sql
-- init/source/init.sql
CREATE TABLE customers (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 테스트 데이터
INSERT INTO customers (id, name, email) VALUES
(1, 'John Doe', 'john@example.com'),
(2, 'Jane Smith', 'jane@example.com');
```

### 2. 싱크 데이터베이스 (sink_db)
```sql
-- init/sink/init.sql
CREATE TABLE customers (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. 테스트 시나리오

#### 3.1 기본 동기화 테스트
1. 소스 DB에 데이터 추가:
```sql
INSERT INTO source_db.customers (id, name, email) 
VALUES (3, 'Test User', 'test@example.com');
```

2. 싱크 DB에서 확인:
```sql
SELECT * FROM sink_db.customers;
```

#### 3.2 업데이트 테스트
1. 소스 DB에서 데이터 수정:
```sql
UPDATE source_db.customers 
SET email = 'updated@example.com' 
WHERE id = 1;
```

2. 싱크 DB에서 변경 확인:
```sql
SELECT * FROM sink_db.customers WHERE id = 1;
```

#### 3.3 삭제 테스트
1. 소스 DB에서 데이터 삭제:
```sql
DELETE FROM source_db.customers WHERE id = 2;
```

2. 싱크 DB에서 삭제 확인:
```sql
SELECT * FROM sink_db.customers WHERE id = 2;
```

### 4. 테스트 데이터 모니터링

#### 4.1 바이너리 로그 확인
```bash
# 소스 DB 컨테이너 내부에서
mysqlbinlog /var/lib/mysql/mysql-bin.000001
```

#### 4.2 CDC 상태 확인
```sql
-- 소스 DB에서
SHOW MASTER STATUS;
SHOW SLAVE STATUS;
```

## 시작하기

1. Docker 컨테이너 실행:
```bash
docker-compose up -d
```

2. 컨테이너 상태 확인:
```bash
docker-compose ps
```

3. 로그 확인:
```bash
docker-compose logs -f
```

4. 프로젝트 빌드:
```bash
./gradlew build
```

5. Flink CDC 작업 실행:
```bash
./gradlew run
```

## 테스트 실행

단위 테스트를 실행하려면:
```bash
./gradlew test
```

## 구조

- `docker-compose.yml`: MySQL 소스와 싱크 인스턴스 설정
- `init/source/init.sql`: 소스 MySQL 초기화 스크립트
- `init/sink/init.sql`: 싱크 MySQL 초기화 스크립트
- `src/main/java/com/example/MysqlCdcJob.java`: Flink CDC 작업 구현
- `src/test/java/com/example/MysqlCdcJobTest.java`: 단위 테스트

## 문제 해결

1. **컨테이너 시작 실패**
   - 로그 확인: `docker-compose logs`
   - 포트 충돌 확인
   - 볼륨 마운트 권한 확인

2. **CDC 동기화 문제**
   - 바이너리 로그 설정 확인
   - 네트워크 연결 확인
   - 권한 설정 확인

3. **성능 이슈**
   - 메모리 설정 확인
   - 네트워크 대역폭 확인
   - 배치 사이즈 조정 