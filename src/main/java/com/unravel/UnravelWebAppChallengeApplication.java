package com.unravel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableScheduling
@EnableRedisHttpSession
@SpringBootApplication
public class UnravelWebAppChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnravelWebAppChallengeApplication.class, args);
    }

}
