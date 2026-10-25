package com.chungnam.bidstats;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@MapperScan("com.chungnam.bidstats.mapper")
public class BidStatsApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8"); SpringApplication.run(BidStatsApplication.class, args); }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder b) { return b.sources(BidStatsApplication.class); }
}
