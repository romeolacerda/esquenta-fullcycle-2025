package kafka.streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.log4j.BasicConfigurator;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kafka.streams.UserBalanceApplication.UserBalance;
import kafka.streams.message.PaymentCreatedMessage;
import kafka.streams.serialization.JsonSerde;

public class UserBalanceApplication {
    private static final String INPUT_TOPIC = "payments";
    private static final String OUTPUT_TOPIC = "user_balances";

    // Inner class to represent user balance
    public static class UserBalance {
        public String userId;
        public double balance;

        public UserBalance() {
            this.balance = 0.0;
        }

        public UserBalance(String userId, double balance) {
            this.userId = userId;
            this.balance = balance;
        }

        public UserBalance addPayment(double amount) {
            this.balance += amount;
            return this;
        }
    }

    // Inner class for output message
    public static class BalanceUpdatedMessage {
        public String user_id;
        public double balance;

        public BalanceUpdatedMessage(String user_id, double balance) {
            this.user_id = user_id;
            this.balance = balance;
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();

        final Gson gson = new GsonBuilder().create();

        //Serde - serialization | deserialization

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "user-balance-tracker");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> paymentStream = builder.stream(INPUT_TOPIC);

        // Create serdes for UserBalance
        Serde<UserBalance> userBalanceSerde = JsonSerde.create(UserBalance.class);

        //select sum(amount) from payments group by user_id

        // Process payments and track user balance
        KTable<String, UserBalance> userBalances = paymentStream
                .mapValues((key, value) -> gson.fromJson(value, PaymentCreatedMessage.class))
                .filter((key, paymentCreated) -> paymentCreated != null)
                .peek((key, paymentCreated) -> {
                    System.out.println("Processing payment - User ID: " + paymentCreated.user_id
                            + ", Amount: " + paymentCreated.amount);
                })
                .selectKey((key, paymentCreated) -> paymentCreated.user_id)
                .groupByKey(
                                                Grouped.with(Serdes.String(),
                                                                JsonSerde.create(PaymentCreatedMessage.class)))
                .aggregate(
                        () -> new UserBalance(),
                        (key, paymentCreated, userBalance) -> {
                            userBalance.userId = key;
                            return userBalance.addPayment(paymentCreated.amount);
                        },
                        Materialized.with(Serdes.String(), userBalanceSerde)
                );

        // Output user balances to topic
        userBalances.toStream()
                .peek((userId, balance) -> {
                    System.out.println("Updated balance - User ID: " + userId
                            + ", Balance: " + balance.balance);
                })
                .map((userId, balance) -> {
                    BalanceUpdatedMessage balanceMessage = new BalanceUpdatedMessage(userId, balance.balance);
                    return new KeyValue<>(userId, gson.toJson(balanceMessage));
                })
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}