# MySQL CDC to MySQL Demo

이 프로젝트는 Flink CDC를 사용하여 MySQL에서 MySQL로 실시간 데이터 동기화를 구현한 데모입니다.

## 요구사항

- Docker
- Java 11
- Gradle 8.0

## 시작하기

1. Docker 컨테이너 실행:
```bash
docker-compose up -d
```

2. 프로젝트 빌드:
```bash
./gradlew build
```

3. Flink CDC 작업 실행:
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

## 테스트 방법

1. 소스 MySQL에 데이터 추가:
```sql
INSERT INTO source_db.customers (id, name, email) VALUES (3, 'Test User', 'test@example.com');
```

2. 싱크 MySQL에서 데이터 확인:
```sql
SELECT * FROM sink_db.customers;
``` 