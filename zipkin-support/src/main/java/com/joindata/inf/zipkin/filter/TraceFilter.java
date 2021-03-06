package com.joindata.inf.zipkin.filter;

import com.google.common.base.Stopwatch;
import com.joindata.inf.common.basic.annotation.FilterComponent;
import com.joindata.inf.common.basic.cst.RequestLogCst;
import com.joindata.inf.common.basic.support.BootInfoHolder;
import com.joindata.inf.zipkin.TraceContext;
import com.joindata.inf.zipkin.agent.TraceAgent;
import com.joindata.inf.zipkin.cst.TraceConstants;
import com.joindata.inf.zipkin.util.Ids;
import com.joindata.inf.zipkin.util.ServerInfo;
import com.joindata.inf.zipkin.util.Times;
import com.joindata.inf.zipkin.util.TraceUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 过滤器，创建请求的唯一ID
 * Created by Rayee on 2017/10/23.
 */
@Component
@FilterComponent(path = "/*")
public class TraceFilter implements Filter {

    @Resource
    private TraceAgent agent;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //do trace
        Stopwatch stopwatch = Stopwatch.createStarted();
        //root span
        Span rootSpan = TraceUtils.startTrace(request.getLocalPort());
        filterChain.doFilter(servletRequest, servletResponse);
        TraceUtils.endTrace(request.getLocalPort(), rootSpan, stopwatch, agent);
    }

    private Span startTrace(HttpServletRequest request) {
        String apiName = BootInfoHolder.getAppId().concat(" : ").concat(request.getRequestURI());
        Span apiSpan = new Span();
        //调用链初始化信息
        long id = Ids.get();
        apiSpan.setId(id);
        apiSpan.setTrace_id(id);
        apiSpan.setName(apiName);
        long timestamp = Times.currentMicros();
        apiSpan.setTimestamp(timestamp);
        apiSpan.addToAnnotations(Annotation.create(timestamp, TraceConstants.ANNO_SR, Endpoint.create(apiName, ServerInfo.IP4, request.getLocalPort())));
        apiSpan.addToBinary_annotations(BinaryAnnotation.create("name", BootInfoHolder.getAppId(), null));
        //日志显示traceId信息
        MDC.clear();
        MDC.put(RequestLogCst.REQUEST_ID, Long.toHexString(id));
        return apiSpan;
    }

    private void endTrace(HttpServletRequest request, Span span, Stopwatch stopwatch) {
        span.addToAnnotations(Annotation.create(Times.currentMicros(), TraceConstants.ANNO_SS, Endpoint.create(span.getName(), ServerInfo.IP4, request.getLocalPort())));
        span.setDuration(stopwatch.stop().elapsed(TimeUnit.MICROSECONDS));
        agent.send(TraceContext.getSpans());
        TraceContext.clear();
        MDC.clear();
    }

    @Override
    public void destroy() {
        TraceContext.clear();
    }
}
































