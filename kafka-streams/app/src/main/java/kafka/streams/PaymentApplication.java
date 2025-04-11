package kafka.streams;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.log4j.BasicConfigurator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kafka.streams.message.FraudAlertMessage;
import kafka.streams.message.PaymentCreatedMessage;
import kafka.streams.serialization.JsonSerde;

public class PaymentApplication {
        private static final String INPUT_TOPIC = "payments";
        private static final String OUTPUT_TOPIC = "payment_alerts";

        public static void main(String[] args) {
                BasicConfigurator.configure();

                final Gson gson = new GsonBuilder().create();

                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "payment-monitoring");
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
                props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

                StreamsBuilder builder = new StreamsBuilder();
                KStream<String, String> paymentStream = builder.stream(INPUT_TOPIC);

                Serde<Long> longSerde = JsonSerde.create(Long.class);
                // Detecção de fraude: Muitas transações do mesmo usuário em curto período
                KTable<Windowed<String>, Long> fraudAlerts = paymentStream
                                .mapValues((key, value) -> gson.fromJson(value, PaymentCreatedMessage.class))
                                .filter((key, paymentCreated) -> paymentCreated != null)
                                .peek((key, paymentCreated) -> {
                                        System.out.println("User ID: " + paymentCreated.user_id);
                                        System.out.println("Amount: " + paymentCreated.amount);
                                })
                                // .groupBy((key, paymentCreated) -> paymentCreated.user_id)
                                .selectKey((key, paymentCreated) -> paymentCreated.user_id)
                                .groupByKey(
                                                Grouped.with(Serdes.String(),
                                                                JsonSerde.create(PaymentCreatedMessage.class)))

                                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5))) // 5 minutos
                                .count(Materialized.with(Serdes.String(), longSerde))
                                // .count()
                                .filter((key, count) -> {
                                        System.out.println("User ID: " + key.key());
                                        System.out.println("Count: " + count);
                                        return count > 1;
                                });

                // Enviar alertas para outro tópico
                fraudAlerts.toStream()
                                .map((key, count) -> {
                                        FraudAlertMessage alert = new FraudAlertMessage(key.key(),
                                                        "Fraud alert: " + count + " payments in 1 minute");
                                        return new KeyValue<>(key.key(), gson.toJson(alert));
                                })
                                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        }

}

//Domain Driven Design

//mysql admin de catalogs (series, videos)

//video title, description, thumbnail
//membros de elencos
//categorias
//generos


//Apache Kafka Connect 
//Kafka Streams - os eventos de criação de vídeo join 



//consumidor node (video bonitinho)

//elastic search (api do catalogo)