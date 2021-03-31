package com.titan.thor.config;

import com.titan.thor.Thor;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Log
@Configuration
public class ThorConfig {

    @Bean
    public CommandLineRunner bifrostRunner(TaskExecutor executor) {
        log.info("Starting Thor");
        return args -> executor.execute(new Thor());
    }

}


