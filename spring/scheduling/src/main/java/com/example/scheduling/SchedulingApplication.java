package com.example.scheduling;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

@EnableScheduling
@SpringBootApplication
public class SchedulingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingApplication.class, args);
    }
}

@Log
@Component
class ScheduledTasks {

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {

        // the version of logging doesnt have a format function?
        log.info(String.format("the time is now %s",
                DateFormat.getTimeInstance(DateFormat.DEFAULT,
                        Locale.getDefault()
                ).format(new Date())
        ));
    }
}