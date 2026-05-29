package com.bean.lostandfound;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@MapperScan("com.bean.lostandfound.mapper")
@EnableScheduling
public class LostandfoundApplication {

    public static void main(String[] args) {
        log.info("若");
        SpringApplication.run(LostandfoundApplication.class, args);
    }

}
