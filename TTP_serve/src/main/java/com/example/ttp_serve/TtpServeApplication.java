package com.example.ttp_serve;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.ttp_serve.entity")
@EnableJpaRepositories("com.example.ttp_serve.repository")
public class TtpServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TtpServeApplication.class, args);
    }

}
