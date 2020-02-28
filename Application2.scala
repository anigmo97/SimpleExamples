package com.ingest.spark.streamingTest

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.Bean

import com.ingest.spark.streamingTest.StreamTest

@SpringBootApplication(scanBasePackages = Array("com.ingest.spark.streaming"))
class Application2 extends App //extend App-> to run as scala show in ide

    
object Main {    
    def main(args: Array[String]): Unit = {
        var springApplication: SpringApplication = new SpringApplication(classOf[Application2])
        //SpringApplication.run(classOf[Application], args:_*) // works
        springApplication.addListeners(new StreamTest());
        springApplication.run(args: _*);
    }

}
