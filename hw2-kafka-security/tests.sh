export BOOTSTRAP_SERVER=kafka1:19092,kafka2:29092,kafka3:39092
export TOPIC_NAME=otus-events

#docker exec kafka1 kafka-topics --create --topic $TOPIC_NAME --bootstrap-server $BOOTSTRAP_SERVER --command-config /etc/kafka/secrets/admin-sasl.properties

#docker exec kafka1 kafka-topics --describe --topic $TOPIC_NAME --bootstrap-server $BOOTSTRAP_SERVER --command-config /etc/kafka/secrets/alice-sasl.properties



#docker exec kafka1 kafka-topics --describe --topic $TOPIC_NAME --bootstrap-server $BOOTSTRAP_SERVER --command-config /etc/kafka/secrets/alice-sasl.properties

#Alice
#docker exec kafka2 kafka-console-producer --topic $TOPIC_NAME --bootstrap-server $BOOTSTRAP_SERVER --command-config /etc/kafka/secrets/alice-sasl.properties
#docker exec kafka2 kafka-console-consumer --from-beginning --max-messages 2 --topic $TOPIC_NAME --bootstrap-server $BOOTSTRAP_SERVER --consumer.config /etc/kafka/secrets/alice-sasl.properties


#docker exec kafka1 kafka-acls --add --allow-principal User:Alice --operation Read --topic $TOPIC_NAME --bootstrap-server $BOOTSTRAP_SERVER --command-config /etc/kafka/secrets/admin-sasl.properties

docker exec kafka1 kafka-acls --list --bootstrap-server $BOOTSTRAP_SERVER --command-config /etc/kafka/secrets/admin-sasl.properties