package com.example;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.junit.Test;
import static org.junit.Assert.*;

public class MysqlCdcJobTest {

    @Test
    public void testPipeline() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 테스트용 소스 생성
        TestSource source = new TestSource();

        // 테스트용 싱크 생성
        TestSink sink = new TestSink();

        // 파이프라인 구성
        env.addSource(source)
                .addSink(sink);

        env.execute("Test Pipeline");

        // 테스트 검증
        assertTrue(sink.getReceivedRecords() > 0);
    }

    private static class TestSource implements SourceFunction<String> {
        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            // 테스트 데이터 생성
            ctx.collect("{\"id\":1,\"name\":\"Test User\",\"email\":\"test@example.com\"}");
        }

        @Override
        public void cancel() {
        }
    }

    private static class TestSink implements SinkFunction<String> {
        private int receivedRecords = 0;

        @Override
        public void invoke(String value, Context context) {
            receivedRecords++;
        }

        public int getReceivedRecords() {
            return receivedRecords;
        }
    }
}