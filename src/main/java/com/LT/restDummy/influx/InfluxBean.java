//package com.LT.restDummy.config;
//
//import com.LT.restDummy.file.ServiceFileHandler;
//import com.LT.restDummy.influx.InfluxConnect;
//import com.LT.restDummy.influx.InfluxWriter;
//import lombok.extern.slf4j.Slf4j;
//import org.influxdb.InfluxDB;
//import org.influxdb.InfluxDBFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * Класс создает бины для инфлюкса
// */
//@Configuration
//@PropertySource("classpath:/influxDB.properties")
//@Slf4j
//public class InfluxBean {
//
//    @Bean("InfluxDB")
//    public InfluxDB influxdbFactory("${influxdb.connect.url}" String url,
////                                    "${influxdb.username}" String username,
////                                    "${influxdb.password}" String password,
//                                    "${influxdb.batch.byCount}" int actions,
//                                    "${influxdb.batch.byTimeInMs}" int flush) {
//        InfluxDBFactory factory;
//        factory = InfluxDBFactory.INSTANCE;
////        InfluxDB influxDB = factory.connect(url, username, password);
//        InfluxDB influxDB = factory.connect(url);
//        influxDB.enableBatch(actions, flush, TimeUnit.MILLISECONDS);
//        return influxDB;
//    }
//
//    @Bean("InfluxWriter")
//    public InfluxWriter influxWriter("${influxdb.database}" String db,
//                                     "${influxdb.retentionpolicy}" String retentionpolicy) {
//        return InfluxWriter.getInstance().initialize(db, retentionpolicy);
//    }
//
//    @Bean("InfluxConnect")
//    public InfluxConnect InfluxConnect("${influxdb.database}" String db,
//                                       "${influxdb.retentionpolicy}" String retentionpolicy,
//                                       "${influxdb.block}" String block) {
//
//        InfluxConnect influxConnect = new InfluxConnect();
//        influxConnect.setDb(db);
//        influxConnect.setRetentionPolicy(retentionpolicy);
//        influxConnect.setSubsystem(ServiceFileHandler.getInfluxProperty().getOrDefault("subsystem", "CREATE_SUBSYSTEM").toString());
//        influxConnect.setBlock(block);
//        influxConnect.setChanel(ServiceFileHandler.getInfluxProperty().getOrDefault("influxdb.chanel", "SBOL").toString());
//        return influxConnect;
//    }
//}