package reactive.essentials.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriber() {
        Flux<String> fluxName = Flux.just("Thiele Heemann", "Joaquina Maria", "Soraia Evans").log();

        StepVerifier.create(fluxName)
                .expectNext("Thiele Heemann", "Joaquina Maria", "Soraia Evans")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers() {
        Flux<Integer> fluxName = Flux.range(1, 5).log();
        fluxName.subscribe(value -> log.info("Number {}", value));

        log.info("-------------------------------");

        StepVerifier.create(fluxName)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList() {
        Flux<Integer> fluxName = Flux.fromIterable(List.of(1, 2, 3, 4)).log();
        fluxName.subscribe(value -> log.info("Number {}", value));

        log.info("-------------------------------");

        StepVerifier.create(fluxName)
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersError() {
        Flux<Integer> fluxName = Flux.range(1, 5)
                .log()
                .map(value -> {
                    if (value == 4) {
                        throw new IndexOutOfBoundsException("Index Error");
                    }
                    return value;
                });
        fluxName.subscribe(value -> log.info("Number {}", value),
                Throwable::printStackTrace,
                () -> log.info("Done."),
                subscription -> subscription.request(3));

        log.info("-------------------------------");

        StepVerifier.create(fluxName)
                .expectNext(1, 2, 3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackpressure() {
        Flux<Integer> fluxName = Flux.range(1, 10)
                .log();


        fluxName.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        log.info("-------------------------------");

        StepVerifier.create(fluxName)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersNotSoUglyBackpressure() {
        Flux<Integer> fluxName = Flux.range(1, 10)
                .log();


        fluxName.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("-------------------------------");

        StepVerifier.create(fluxName)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberIntervalOne() throws Exception {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

        interval.subscribe(i -> log.info("Number {}", i));
        Thread.sleep(3000);
    }

    //Interval is never executed on the main thread.

    @Test
    public void fluxSubscriberIntervalTwo() throws Exception {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();

    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1))
                .take(10)
                .log();
    }
}
