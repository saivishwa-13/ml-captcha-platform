package com.mlcaptcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ML CAPTCHA Backend Application
 * Running in in-memory mode (no database required).
 * Supports Sequence Memory CAPTCHA only.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@EnableAsync
@EnableScheduling
public class MlCaptchaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MlCaptchaApplication.class, args);
    }
}
