//package com.LT.restDummy.influx;
//
////import com.LT.restDummy.delay.model.DelayValue;
//
//import com.LT.restDummy.servises.ServiceValue;
//import lombok.NonNull;
//import org.influxdb.InfluxDB;
//import org.influxdb.dto.BatchPoints;
//import org.influxdb.dto.Point;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * Пишет в инфлюкс
// */
//public class InfluxWriter {
//    @id9681581 (@Autowired)
//    private InfluxDB influxDB;
//    @id9681581 (@Autowired)
//    private InfluxConnect influxConnect;
//    BatchPoints batchPoints;
//
//    public static class InfluxWriterHolder {
//        static final InfluxWriter HOLDER_INSTANCE = new InfluxWriter();
//    }
//
//    public static InfluxWriter getInstance() {
//        return InfluxWriter.InfluxWriterHolder.HOLDER_INSTANCE;
//    }
//
//    public InfluxWriter initialize(String db, String retentionPolicy) {
//        batchPoints = BatchPoints
//                .database(db)
//                .retentionPolicy(retentionPolicy)
//                .build();
//        return this;
//    }
//
//    public void addPoint(@NonNull String operationName) {
//        long timestamp = System.currentTimeMillis();
//        Point point = Point.measurement(influxConnect.getSubsystem())
//                .time(timestamp, TimeUnit.MILLISECONDS)
//                .tag("Application", influxConnect.getSubsystem())
//                .tag("Block", influxConnect.getBlock())
//                .tag("Chanel", influxConnect.getChanel())
//                .tag("operationName", operationName)
//                .tag("operationNameIncome", operationName)
//                .addField("timeDelay", String.valueOf(ServiceValue.getInstance().getDelayByService(operationName))).build();
//        influxDB.write(influxConnect.getDb(), influxConnect.getRetentionPolicy(), point);
//    }
//}