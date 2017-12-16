package com.example.reactive;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReactiveApplicationTests {

	@Test
	public void contextLoads() {
		Flux<String> aFlux = Flux.just("mario","data");
	
		StepVerifier.create(aFlux)
					.expectNext("mario")
					.expectNext("data")
					.expectComplete()
					.verify();
	}

	@Test
	public void expectNextAsync() {
		Flux<String> aFlux = Flux.just("mario", "bear")
								.publishOn(Schedulers.parallel());
		
								StepVerifier.create(aFlux)
								.expectNext("mario")
								.expectNext("bear")
								.expectComplete()
								.verify();
	}

	@Test
	public void expectNexts() {
		Flux<String> flux = Flux.just("foo", "bar");

		StepVerifier.create(flux)
		.expectNext("foo", "bar")
		.expectComplete()
		.verify();
	}

	@Test
	public void expectNextMatches() {
		Flux<String> flux = Flux.just("mario", "bear");

		StepVerifier.create(flux)
		.expectNextMatches("mario"::equals)
		.expectNextMatches("bear"::equals)
		.expectComplete()
		.verify();
	}

	@Test
	public void expectInvalidNextMatches() {
		Flux<String> flux = Flux.just("mario", "bar");

		Assertions.assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> StepVerifier.create(flux)
				.expectNextMatches("mario"::equals)
				.expectNextMatches("bear"::equals)
				.expectComplete()
				.verify())
			.withMessage("expectation \"expectNextMatches\" failed (predicate failed on value: bar)");
		
	}

	// aggressive consumer == standard topical pub/sub ( 1 : * )
	@Test
	public void testBlockingMono() {
		Tuple2<Long, Long> nowandLater = 
				Mono.zip(
					Mono.just(System.currentTimeMillis()),
					Flux.just(1L).delayElements(Duration.ofSeconds(1)).map(i -> System.currentTimeMillis())
						.next()
				).block();

		Assertions.assertThat(nowandLater.getT2()).isNotNull();
		Assertions.assertThat(nowandLater.getT1()).isNotNull();
	}

	@Test
	public void consumeNextWith() throws Exception {
		Flux<String> flux = Flux.just("bar");

		Assertions.assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> StepVerifier.create(flux)
											.consumeNextWith(s -> {
												if(!"foo".equals(s)) {
													throw new AssertionError(s);
												}
											})
											.expectComplete()
											.verify()
			)
			.withMessage("bar");											
	}

	@Test
	public void consumeNextWith2() throws Exception {
		Flux<String> flux = Flux.just("bar");
		Assertions.assertThatExceptionOfType(AssertionError.class)
		.isThrownBy(() -> StepVerifier.create(flux)
										.consumeNextWith(s -> {
											if (!"foo".equals(s)) {
												throw new AssertionError("e:"+s);
											}
										})
										.expectComplete()
										.verify())
		.withMessage("e:bar");
	}

	@Test
	public void assertNext() throws Exception {
		Flux<String> flux = Flux.just("foo");

		Assertions.assertThatExceptionOfType(AssertionError.class)
		.isThrownBy(() -> StepVerifier.create(flux)
		.assertNext(s -> Assertions.assertThat(s).endsWith("ooz"))
		.expectComplete()
		.verify())
		.withMessage("\nExpecting:\n <\"foo\">\nto end with:\n <\"ooz\">\n");
	}

	@Test
	public void missingNext() {
		Flux<String> flux = Flux.just("foo", "bar");
		
		Assertions.assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> StepVerifier.create(flux)
			.expectNext("foo")
			.expectComplete()
			.verify())
			.withMessage("expectation \"expectComplete\" failed (expected: onComplete(); actual: onNext(bar))");
		}

		@Test
		public void missingNextAsync() {
			Flux<String> flux = Flux.just("foo", "bar")
									.publishOn(Schedulers.parallel());

			Assertions.assertThatExceptionOfType(AssertionError.class)
							.isThrownBy(() -> StepVerifier.create(flux)
									.expectNext("foo")	
									.expectComplete()
									.verify()
							)
							.withMessage("expectation \"expectComplete\" failed (expected: onComplete(); actual: onNext(bar))");
		}

		@Test
		public void expectNextCount() {
			Flux<String> flux = Flux.just("foo", "bar");

			StepVerifier.create(flux, 0)
			.thenRequest(1)
			.expectNextCount(1)
			.thenRequest(1)
			.expectNextCount(1)
			.expectComplete()
			.verify();
		}
}
