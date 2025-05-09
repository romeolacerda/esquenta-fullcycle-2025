package kafka.streams.serialization;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonSerializer<T> implements Serializer<T> {

    private Gson gson = new Gson();

    @SuppressWarnings("java:S1186")
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String topic, T t) {
        return gson.toJson(t).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("java:S1186")
    @Override
    public void close() {

    }
}
