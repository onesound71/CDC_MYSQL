package com.example;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;

public class MysqlCdcJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // MySQL CDC 소스 설정
        MySqlSource<String> source = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .databaseList("source_db")
                .tableList("source_db.customers")
                .username("root")
                .password("root")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        // JDBC 싱크 설정
        SinkFunction<String> sink = JdbcSink.sink(
                "INSERT INTO customers (id, name, email, created_at) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), email = VALUES(email), created_at = VALUES(created_at)",
                (statement, json) -> {
                    // JSON 파싱 및 매핑 로직
                    // 실제 구현에서는 JSON 파싱 라이브러리 사용 필요
                },
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3307/sink_db")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("root")
                        .build()
        );

        // 파이프라인 구성
        env.addSource(source)
           .addSink(sink);

        env.execute("MySQL CDC to MySQL");
    }
} 