package me.lolico.example.netty.event;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

public class DefaultEventBus implements EventBus {

    private final DirectProcessor<Event> bus;
    private final FluxSink<Event> sink;
    private final Scheduler scheduler;

    public DefaultEventBus(Scheduler scheduler) {
        this.bus = DirectProcessor.create();
        this.sink = bus.sink();
        this.scheduler = scheduler;
    }

    @Override
    public Flux<Event> get() {
        return bus.onBackpressureDrop().publishOn(scheduler);
    }

    @Override
    public void publish(Event event) {
        sink.next(event);
    }
}
