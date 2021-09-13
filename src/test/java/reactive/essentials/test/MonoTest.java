package reactive.essentials.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class MonoTest {

    /**
     * Reactive Streams
     * 1. Asynchronous
     * 2. Non-blocking
     * 3. Backpreassure
     */

    @Test
    public void monoSubscriber() {
        String name = "Thiele Heemann";
        Mono<String> monoName = Mono.just(name).log();

        monoName.subscribe();
        log.info("---------------------------------");

        StepVerifier.create(monoName)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Thiele Heemann";
        Mono<String> monoName = Mono.just(name).log();

        monoName.subscribe(value -> log.info("Name: {}", value));
        log.info("---------------------------------");

        StepVerifier.create(monoName)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Thiele Heemann";
        Mono<String> monoName = Mono.just(name)
                .map(value -> {
                    throw new RuntimeException("Testing mono with error");
                });

        monoName.subscribe(value -> log.info("Name: {}", value), value -> log.error("Something unexpected happened."));
        monoName.subscribe(value -> log.info("Name: {}", value), Throwable::printStackTrace);

        StepVerifier.create(monoName)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "Thiele Heemann";
        Mono<String> monoName = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        monoName.subscribe(value -> log.info("Name: {}", value),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));
        log.info("---------------------------------");

        StepVerifier.create(monoName)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "Thiele Heemann";
        Mono<String> monoName = Mono.just(name).log();

        monoName.subscribe(value -> log.info("Nome: {}", value),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                Subscription::cancel);
        log.info("---------------------------------");
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Thiele Heemann";
        Mono<String> monoName = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request received. Starting de process..."))
                .doOnNext(value -> log.info("Value is here. Executing doOnNext {}", value))
                .doOnSuccess(value -> log.info("doOnSuccess executed."));

        monoName.subscribe(value -> log.info("Name: {}", value),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Error Message."))
                .doOnError(e -> log.error("Error Message: {}", e.getMessage()))
                .doOnNext(value -> log.info("This is not gonna be executed."))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "Thiele Heemann";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Error Message."))
                .doOnError(e -> log.error("Error Message: {}", e.getMessage()))
                .onErrorResume(value -> {
                    log.info("Inside onErrorResume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "Thiele Heemann";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Error Message."))
                .onErrorReturn("Empty.")

                //will be ignored because onErrorReturn will be executed:
                .onErrorResume(value -> {
                    log.info("Inside onErrorResume");
                    return Mono.just(name);
                })
                .doOnError(e -> log.error("Error Message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("Empty.")
                .verifyComplete();
    }
}
