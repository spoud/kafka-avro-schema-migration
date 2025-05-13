package io.spoud.intratopic;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

/**
 * Backwards compatible deserializer which can read old (v1) and current (v2) schemas.
 * Maps old schemas to the new format transparently.
 *
 * @param <V1> Class of the v1 records
 * @param <V2> Class of the v2 records
 */
public class MultiSchemaDeserializer<V1 extends SpecificRecord, V2 extends SpecificRecord> implements Deserializer<V2> {

    private final KafkaAvroDeserializer avroDeserializer = new KafkaAvroDeserializer();
    private final Function<V1, V2> mapV1ToV2;
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSchemaDeserializer.class);

    /**
     * @param mapV1ToV2 mapping function to convert v1 records to v2 records
     */
    public MultiSchemaDeserializer(Function<V1, V2> mapV1ToV2) {
        this.mapV1ToV2 = mapV1ToV2;
    }


    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map configs, boolean isKey) {
        // deserialize to generic records in underlying deserializer
        configs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "false");
        this.avroDeserializer.configure(configs, isKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V2 deserialize(String topic, byte[] data) {
        try {
            GenericRecord o = (GenericRecord) this.avroDeserializer.deserialize(topic, data);
            if (o.getSchema().getProp("version").equals("2")) {
                return (V2) new SpecificDatumReader<V2>().getSpecificData().deepCopy(o.getSchema(), o);
            } else {
                V1 v1 = (V1) new SpecificDatumReader<V1>().getSpecificData().deepCopy(o.getSchema(), o);
                return mapV1ToV2.apply(v1);
            }
        } catch (ClassCastException e) {
            throw new SerializationException("Unsupported schema");
        }
    }


}
