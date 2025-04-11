- fazer build do projeto
./gradlew build

- rodar o projeto o principal
./gradlew run

- fazer build do projeto removendo os arquivos gerados 
./gradlew clean build


{"id":1,"user_id":1,"amount":10, "created_at": "2025-01-01T00:00:00"}

kafka-console-consumer --topic payment-monitoring-KSTREAM-AGGREGATE-STATE-STORE-0000000006-changelog --from-beginning --bootstrap-server localhost:9092 --property print.key=true --property key.separator=" "