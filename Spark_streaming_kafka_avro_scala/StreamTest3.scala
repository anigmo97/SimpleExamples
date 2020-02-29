package com.ingest.spark.streamingTest

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset,Row,SparkSession}
import org.apache.spark.sql.streaming.{ OutputMode, StreamingQuery}
import org.apache.spark.sql.avro._
import org.apache.spark.api.java.JavaSparkContext

import java.nio.file.{Files, Paths}

//logs
import org.slf4j.{Logger, LoggerFactory}


/**
 * Stream. Aggregated topic to hdfs.
 *
 * @author aigualadam
 *
 */

@SerialVersionUID(100L)
class StreamTest3 extends ApplicationListener[ContextRefreshedEvent] /*with Serializable*/{
    val  kafkaServers:String  = "kafka-1:19092,kafka-2:29092,kafka-3:39092"
    val topic: String = "avro_topic"
    val  hadoopURI: String = "hdfs://localhost:8020"
    
  	var sparkConf:SparkConf = new SparkConf()
                        .setAppName("Aggregated Spark Streaming")
                        .setMaster("local")
            			.set("spark.executor.memory", "1g")
            			.set("spark.sql.parquet.compression.codec", "uncompressed")
            			.set("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
            			.set("spark.hadoop.fs.defaultFS", hadoopURI)
            			.set("spark.hadoop.fs.hdfs.impl", classOf[org.apache.hadoop.hdfs.DistributedFileSystem].getName())
            			.set("spark.hadoop.fs.hdfs.server", classOf[org.apache.hadoop.hdfs.server.namenode.NameNode].getName())
            			.set("spark.hadoop.conf", classOf[org.apache.hadoop.hdfs.HdfsConfiguration].getName());
    
    var sparkSession:SparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
	var javaSparkContext:JavaSparkContext = new JavaSparkContext(sparkSession.sparkContext);
  	def log : Logger = LoggerFactory.getLogger(classOf[StreamTest])
  	
  	
    @Override
	def onApplicationEvent(event:ContextRefreshedEvent) {
        
        try {

            log.info("----- reading schema")
            val jsonFormatSchema = new String(Files.readAllBytes(Paths.get("./src/main/resources/schema.avsc")))

            val ds:Dataset[Row] = sparkSession
                .readStream
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaServers)
                .option("subscribe", topic)
                .load()

            val output:Dataset[Row] = ds
                .select(from_avro(ds.col("value"), jsonFormatSchema) as "record")
                .select("record.*")
                
            output.printSchema()

            var query: StreamingQuery = output.writeStream.format("console")
                .option("truncate", "false").outputMode(OutputMode.Append()).start();


            query.awaitTermination();

        } catch {
            case e: Exception => log.error("onApplicationEvent error: ", e)
        }
        
	}
} 