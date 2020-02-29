from time import sleep
from confluent_kafka.avro import AvroProducer, load, loads

def generate_records():
	avro_producer_settings = {
		'bootstrap.servers': "localhost:19092",
		'group.id': 'groupid',
		'schema.registry.url': "http://127.0.0.1:8081"
	}
	producer = AvroProducer(avro_producer_settings)
	key_schema = loads('"string"')
	value_schema = load("schema.avsc")
	i = 1
	while True:
		row = {"int_field": int(i), "string_field": str(i)}
		producer.produce(topic="avro_topic", key="key-{}".format(i), value=row, key_schema=key_schema, value_schema=value_schema)
		print(row)
		sleep(1)
		i+=1

if __name__ == "__main__":
	generate_records()

