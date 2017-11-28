package com.joindata.inf.zipkin.agent;

import com.github.kristofa.brave.AbstractSpanCollector;
import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import com.joindata.inf.zipkin.SimpleMetricsHandler;
import com.joindata.inf.zipkin.collector.HttpSpanCollector;
import com.twitter.zipkin.gen.Span;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class TraceAgent {

    private AbstractSpanCollector collector;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1, r -> {
                Thread worker = new Thread(r);
                worker.setName("TRACE-AGENT-WORKER");
                worker.setDaemon(true);
                return worker;
            });

    public TraceAgent(AbstractSpanCollector collector) {
        this.collector = collector;
    }

//    public TraceAgent(String server) {
//
//        SpanCollectorMetricsHandler metrics = new SimpleMetricsHandler();
//
//        // set flush interval to 0 so that tests can drive flushing explicitly
//        HttpSpanCollector.Config config =
//                HttpSpanCollector.Config.builder()
//                        .compressionEnabled(true)
//                        .flushInterval(0)
//                        .build();
//
//        collector = HttpSpanCollector.create(server, config, metrics);
//    }

    public void send(final List<Span> spans) {
        if (spans != null && !spans.isEmpty()) {
            executor.submit(() -> {
                for (Span span : spans) {
                    collector.collect(span);
                }
                collector.flush();
            });
        }
    }
}
