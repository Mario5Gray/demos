package com.example.redismessaging;

import lombok.extern.java.Log;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Log
public class RedisMessagingApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(RedisMessagingApplication.class, args);
	}

	@Bean
	ApplicationRunner appRun(StringRedisTemplate template, CountDownLatch latch) {
		return args -> {
			log.info("Sending message...");
			template.convertAndSend("chat", "root is the word.  No password. 2017 #YOLO");

			latch.await();

			System.exit(0);
		};
	}

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory factory,
											MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(factory);
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	Receiver receiver(CountDownLatch latch) {
		return new Receiver(latch);
	}

	@Bean
	CountDownLatch latch() {
		return new CountDownLatch(1);
	}

	@Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}


}

@Log
class Receiver {
	CountDownLatch latch;

	public Receiver(CountDownLatch latch) {
		this.latch = latch;
	}

	public void receiveMessage(String msg) {
		log.info("Received <" + msg + ">");
		latch.countDown();
	}
}