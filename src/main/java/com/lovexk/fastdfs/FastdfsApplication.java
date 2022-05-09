package com.lovexk.fastdfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FastdfsApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(FastdfsApplication.class, args);
        String[] beanDefinitionNames = run.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {

            System.out.println(beanDefinitionName);


        }
    }

}
