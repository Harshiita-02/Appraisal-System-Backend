package com.appraise.appraisal.System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @EnableAsync activates Spring's background thread pool so that
 * EmailService.sendNotificationEmail() (which is @Async) runs in a
 * separate thread. Without this annotation, @Async methods run
 * synchronously in the same thread — meaning a slow SMTP server
 * would stall the HTTP response until the email finishes sending.
 */
@EnableAsync
@SpringBootApplication
public class AppraisalSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(AppraisalSystemApplication.class, args);
	}
}