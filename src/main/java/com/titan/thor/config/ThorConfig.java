package com.titan.thor.config;

import com.titan.thor.Thor;
import com.titan.thor.database.Wanda;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Log
@Configuration
public class ThorConfig {

    @Bean
    public Thor thor(Wanda wanda) {
        return new Thor(wanda);
    }

    @Bean
    public CommandLineRunner thorRunner(TaskExecutor executor, Wanda wanda) {
        log.info("Starting Thor -- Testing :)");
        return args -> executor.execute(thor(wanda));
    }

}


