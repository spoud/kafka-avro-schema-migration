# AVRO Schema evolution - handling incompatible versions in Kafka consumers

AVRO Schemas evolve over time, but sometimes it is not possible to achieve backwards or forward compatibility of new schemas.

We demonstrate approaches to deal with such breaking changes using exemplary Spring Boot implementations.


## Option 1: inter-topic migration

* V1-producer write to v1-topic
* V2-consumer reads from v2-topic
* migration-service maps messages from v1-format to v2-format and writes them to the v2-topic

Final phase:
* V1-producer is replaced with a V2-producer
* v1-topic is deleted
* migration-service is deleted

## Option 2: intra-topic migration with consumer logic

* new consumer version contains logic to read v1 and v2 records
* V1-producer is replaced with a V2-producer
* mapping logic is removed from consumer

## Option 3: intra-topic migration with confluent schema contracts

Requires Confluent Platform (enterprise version) or Confluent Cloud.

* new schema is registered, including CEL or jsonata migration rules
* consumer fetches new schema + rules from Schema Registry and applies them transparently

See https://docs.confluent.io/platform/current/schema-registry/fundamentals/data-contracts.html#custom-rules
