package kafka.streams.serialization;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerde {


    private JsonSerde() {
    }

    public static <T> Serde<T> create(Class<T> clazz) {
        return Serdes.serdeFrom(
                new JsonSerializer<T>(),
                new JsonDeserializer<T>(clazz));

    }
}
