package com.ingest.spark.streamingTest

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset,Row,functions,SparkSession}
import org.apache.spark.sql.types.{DataType,StructField,StructType,Metadata,DataTypes}
import org.apache.spark.sql.streaming.{OutputMode,StreamingQuery,Trigger}
import org.apache.spark.api.java.JavaSparkContext;

//import scala.collection.mutable.HashMap
import java.util.{Map,HashMap}

//logs
import org.slf4j.{Logger, LoggerFactory}


import org.apache.spark.sql.delta.sources.DeltaDataSource;
/**
 * Stream. Aggregated topic to hdfs.
 *
 * @author aigualadam
 *
 */
@SerialVersionUID(100L)
class StreamTest extends ApplicationListener[ContextRefreshedEvent] /*with Serializable*/{
    val  kafkaServers:String  = "kafka-1:19092,kafka-2:29092,kafka-3:39092"
  	val  topicConsumer:String = "preprocessed"
  	val  topicProducer:String = "aggregate"
  	val  hiveWarehouse:String = "/user/hive/warehouse"
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
        
        try{	
			var messagesAggregate:Dataset[Row]= sparkSession
                                                  .readStream
                                                  .format("rate") // <-- use RateStreamSource
                                                  .option("rowsPerSecond", 1)
                                                  .load()
			
			log.info("----- write delta lake");
			
    		var queryDelta:StreamingQuery = messagesAggregate
    		    .writeStream
    		    .format("delta") //NOT OK
        		.format("org.apache.spark.sql.delta.sources.DeltaDataSource")// NOT OK
        		//.format("parquet") //OK
        		.outputMode("append")
        		.option("checkpointLocation", "/tmp/checkpoint2")
        		.option("path", hiveWarehouse)
        		.trigger(Trigger.ProcessingTime(100000))
        		.start()
    		
    		queryDelta.awaitTermination()
            
        }catch {
            case e:Exception => log.error("onApplicationEvent error: ",e)
            //case _: Throwable => log.error("onApplicationEvent error:")
        }
        
	}
} 