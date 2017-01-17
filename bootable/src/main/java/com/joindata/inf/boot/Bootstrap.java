package com.joindata.inf.boot;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Set;

import javax.servlet.annotation.WebFilter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.joindata.inf.boot.annotation.JoindataApp;
import com.joindata.inf.boot.annotation.JoindataWebApp;
import com.joindata.inf.boot.bootconfig.WebMvcConfig;
import com.joindata.inf.boot.mechanism.Jetty2Log4j2Bridge;
import com.joindata.inf.boot.mechanism.JoindataAnnotationBeanNameGenerator;
import com.joindata.inf.boot.webserver.JettyServerFactory;
import com.joindata.inf.common.basic.annotation.FilterConfig;
import com.joindata.inf.common.basic.annotation.JoindataComponent;
import com.joindata.inf.common.basic.annotation.WebAppFilterItem;
import com.joindata.inf.common.basic.annotation.WebConfig;
import com.joindata.inf.common.basic.errors.SystemError;
import com.joindata.inf.common.basic.exceptions.SystemException;
import com.joindata.inf.common.basic.stereotype.AbstractConfigHub;
import com.joindata.inf.common.basic.support.BootInfoHolder;
import com.joindata.inf.common.util.basic.ArrayUtil;
import com.joindata.inf.common.util.basic.ClassUtil;
import com.joindata.inf.common.util.basic.CollectionUtil;
import com.joindata.inf.common.util.basic.StringUtil;
import com.joindata.inf.common.util.basic.SystemUtil;
import com.joindata.inf.common.util.log.Logger;

/**
 * 启动器提供者
 * 
 * @author <a href="mailto:songxiang@joindata.com">宋翔</a>
 * @date 2016年12月2日 下午1:35:48
 */
public class Bootstrap
{
    private static final Logger log = Logger.get();

    private static Integer port = null;

    /**
     * 启动应用，<strong>启动后容器将继续运行</strong><br />
     * <i>会在堆栈中自动寻找调用的启动类，放心地调用即可</i>
     * 
     * @param args 启动参数，实际上并没有什么软用，不要传
     */
    public static final ApplicationContext boot(String... args)
    {
        try
        {
            log.info(StreamUtils.copyToString(ClassUtil.getRootResourceAsStream("logo.txt"), Charset.forName("UTF-8")));
        }
        catch(IOException e)
        {
            System.err.println(e);
        }

        Class<?> bootClz = ClassUtil.getCaller();

        ApplicationContext context = null;

        try
        {
            // 如果是 Web 应用，启动 Web
            if(bootClz.getAnnotation(JoindataWebApp.class) != null)
            {
                log.info("启动 Web 应用...");

                JoindataWebApp joindataWebApp = bootClz.getAnnotation(JoindataWebApp.class);
                configureBootInfo(bootClz, joindataWebApp.id(), joindataWebApp.version());
                checkEnv();

                context = bootWeb(bootClz, bootClz.getAnnotation(JoindataWebApp.class).port());

                log.info("应用已启动, PID: {}{}", SystemUtil.getProcessId(), ". Web 端口号: " + Bootstrap.port);

            }
            // 启动应用
            else if(bootClz.getAnnotation(JoindataApp.class) != null)
            {
                log.info("启动应用...");

                JoindataApp joindataApp = bootClz.getAnnotation(JoindataApp.class);
                configureBootInfo(bootClz, joindataApp.id(), joindataApp.version());
                checkEnv();

                context = boot(bootClz);

                log.info("应用已启动, PID: {}", SystemUtil.getProcessId());
            }
            // 没有标注，就报错
            else
            {
                log.fatal("启动失败, 启动类中没有 @JoindataApp 或 @JoindataWebApp 注解，不知道你要启动什么样的应用 O__O \"…");
                System.exit(0);
            }
        }
        catch(SystemException e)
        {
            log.fatal("启动失败, 发生意外错误: {}", e.getMessage());
            System.exit(0);
        }
        catch(Exception e)
        {
            log.fatal("启动失败, 发生意外错误: {}", e.getMessage(), e);
            System.exit(0);
        }

        return context;
    }

    /**
     * 启动应用，并返回 Spring 的上下文
     * 
     * @param bootClz 启动类
     * @param args 没什么软用的参数，不要传
     * @return Spring 上下文
     */
    private static final ApplicationContext boot(Class<?> bootClz, String... args)
    {
        log.info("配置 Spring - 开始");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setBeanNameGenerator(new JoindataAnnotationBeanNameGenerator());

        // 注册容器关闭句柄
        log.info("注册容器关闭句柄...");
        context.registerShutdownHook();

        log.info("注册公共扫描包...");
        context.scan("com.joindata.inf.common.basic.support");

        // 注册启动类，这样就可以在启动类中使用其他 Spring 注解
        log.info("注册启动类: {}", bootClz.getName());
        context.register(bootClz);

        // 扫描启动类的包
        log.info("注册扫描包: {}", bootClz.getPackage().getName());
        context.scan(bootClz.getPackage().getName());

        // 扫描 ConfigHub 的包
        for(Class<?> configHubClz: BootInfoHolder.getConfigHubClasses())
        {
            log.info("注册组件类: {}", configHubClz.getName());
            context.register(configHubClz);
            log.info("注册扫描包: {}", configHubClz.getPackage().getName());
            context.scan(configHubClz.getPackage().getName());
        }

        log.info("配置 Spring - 完成");

        log.info("启动 Spring - 开始");
        context.refresh();
        context.start();
        log.info("启动 Spring - 完成");

        return context;
    }

    /**
     * 启动 Web 应用，并返回 Spring 上下文
     * 
     * @param bootClz 启动类
     * @param args 没什么软用的参数，不要传
     * @return Spring 上下文
     * @throws Exception
     */
    private static final ApplicationContext bootWeb(Class<?> bootClz, int port, String... args) throws Exception
    {
        log.info("配置 Spring - 开始");

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setBeanNameGenerator(new JoindataAnnotationBeanNameGenerator());

        log.info("创建 DispatcherServlet: {}", DispatcherServlet.class.getName());
        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

        // 启动 Jetty Jetty
        {
            log.info("配置 Jetty - 开始");

            log.info("重定向 Jetty 日志记录器至: {}", Jetty2Log4j2Bridge.class.getName());
            org.eclipse.jetty.util.log.Log.setLog(new Jetty2Log4j2Bridge("JettyLogger"));

            WebAppContext webAppContext = JettyServerFactory.makeAppContext(context.getClassLoader(), dispatcherServlet);

            // 添加 Filter
            for(Class<?> configHubClz: BootInfoHolder.getConfigHubClasses())
            {
                FilterConfig filterConfig = configHubClz.getAnnotation(FilterConfig.class);

                if(filterConfig != null)
                {

                    for(WebAppFilterItem filterItem: filterConfig.value())
                    {
                        WebFilter webFilter = filterItem.config();
                        if(webFilter == null)
                        {
                            throw new SystemException(SystemError.DEPEND_RESOURCE_CANNOT_READY, "没有设置 Filter 属性，这样不好");
                        }

                        FilterMapping mapping = new FilterMapping();
                        if(StringUtil.isBlank(webFilter.filterName()))
                        {
                            mapping.setFilterName(filterItem.filter().getSimpleName());
                        }
                        else
                        {
                            mapping.setFilterName(webFilter.filterName());
                        }
                        mapping.setPathSpecs(webFilter.urlPatterns());
                        if(!ArrayUtil.isEmpty(webFilter.servletNames()))
                        {
                            mapping.setServletNames(webFilter.servletNames());
                        }
                        mapping.setDispatcherTypes(CollectionUtil.newEnumSet(webFilter.dispatcherTypes()));

                        FilterHolder holder = null;

                        // 对 Spring 的代理 filter 特殊处理，因为这傻逼玩意必须有个 targetBeanName 草他大爷的！
                        if(filterItem.filter().equals(DelegatingFilterProxy.class))
                        {
                            holder = new FilterHolder(new DelegatingFilterProxy(webFilter.filterName()));
                        }
                        else
                        {
                            holder = new FilterHolder(filterItem.filter());
                        }

                        holder.setName(mapping.getFilterName());

                        for(String path: mapping.getPathSpecs())
                        {
                            webAppContext.addFilter(holder, path, CollectionUtil.newEnumSet(webFilter.dispatcherTypes()));
                        }

                        log.info("注册自定义过滤器: {}", mapping.getFilterName());
                    }
                }
            }

            Server server = JettyServerFactory.newServer(port, context, webAppContext);
            log.info("配置 Jetty - 完成");

            log.info("启动 Jetty Server - 开始");
            server.start();
            log.info("启动 Jetty Server - 完成 ");

            // 记录端口号
            Bootstrap.port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
        }

        // 注册容器关闭句柄
        log.info("注册容器关闭句柄...");
        context.registerShutdownHook();

        log.info("注册公共扫描包...");
        context.scan("com.joindata.inf.common.basic.support");

        // 注册启动类
        log.info("注册启动类: {}", bootClz.getName());
        context.register(bootClz);

        // 扫描启动类的包
        log.info("注册扫描包: {}", bootClz.getPackage().getName());
        context.scan(bootClz.getPackage().getName());

        for(Class<?> configHubClz: BootInfoHolder.getConfigHubClasses())
        {
            // 注册支持组件的 Web 配置
            if(configHubClz.getAnnotation(WebConfig.class) != null)
            {
                Class<?>[] webConfigClzes = configHubClz.getAnnotation(WebConfig.class).value();
                if(webConfigClzes == null)
                {
                    continue;
                }

                for(Class<?> webConfigClz: webConfigClzes)
                {
                    if(webConfigClz == null)
                    {
                        continue;
                    }

                    log.info("注册 Web 配置类: {}", webConfigClz.getName());
                    context.register(webConfigClz);
                }
            }

            // 扫描 ConfigHub 包中的组件
            log.info("注册组件类: {}", configHubClz.getName());
            context.register(configHubClz);

            log.info("注册扫描包: {}", configHubClz.getPackage().getName());
            context.scan(configHubClz.getPackage().getName());
        }

        // 注册公共 WebMvc 配置
        log.info("注册 Web 配置类: {}", WebMvcConfig.class.getName());
        context.register(WebMvcConfig.class);

        log.info("配置 Spring - 完成");

        log.info("启动 Spring - 开始");
        context.refresh();
        context.start();
        log.info("启动 Spring - 完成");

        return context;
    }

    /**
     * 设置启动信息，以供其他组件使用
     */
    public static void configureBootInfo(Class<?> bootClz, String appId, String appVersion)
    {
        log.info("配置启动信息 - 开始");

        // 设置 AppID
        BootInfoHolder.setAppId(appId);
        log.info("应用 ID: {}", appId);

        // 设置应用版本号
        BootInfoHolder.setAppVersion(appVersion);
        log.info("应用版本号: {}", appVersion);

        // 告诉大家启动类是哪个
        BootInfoHolder.setBootClass(bootClz);
        log.info("启动类是: {}", bootClz.getName());

        Annotation[] annos = bootClz.getAnnotations();
        for(Annotation anno: annos)
        {
            JoindataComponent jc = anno.annotationType().getAnnotation(JoindataComponent.class);
            if(jc == null)
            {
                continue;
            }

            log.info("声明使用组件: @{} - {}", anno.annotationType().getSimpleName(), jc.name());

            BootInfoHolder.addConfigHub(jc.bind());
        }

        log.info("配置启动信息 - 完成");
    }

    /**
     * 检查环境是否干净整洁
     * 
     * @throws Exception 发生任何错误，抛出该异常
     */
    private static void checkEnv() throws Exception
    {
        log.info("检查环境 - 开始");

        // 检查依赖组件
        Set<String> needAnno = CollectionUtil.newHashSet();
        for(Class<? extends AbstractConfigHub> configHubClz: BootInfoHolder.getConfigHubClasses())
        {
            log.info("检查依赖组件: {}", configHubClz);
            for(Class<? extends Annotation> clz: getDependComponent(configHubClz))
            {
                if(BootInfoHolder.hasBootAnno(clz))
                {
                    log.info("         @{} - OK!", clz.getSimpleName());
                }
                else
                {
                    needAnno.add("@" + clz.getSimpleName());
                    log.fatal("         @{} - 不 OK!", clz.getSimpleName());
                }
            }
        }
        if(!CollectionUtil.isNullOrEmpty(needAnno))
        {
            throw new SystemException(SystemError.DEPEND_COMPONENT_NOT_READY, "请将所需组件注解声明在启动类上: " + needAnno);
        }

        // 执行组件的 check 方法
        for(AbstractConfigHub configHub: BootInfoHolder.getConfigHubs())
        {
            log.info("执行环境检查: {}.{}", configHub.getClass().getName(), "check()");
            configHub.executeCheck();
        }

        log.info("检查环境 - 完成");
    }

    /**
     * 获取依赖组件
     * 
     * @return 依赖组件集合
     */
    private static Set<Class<? extends Annotation>> getDependComponent(Class<? extends AbstractConfigHub> clz)
    {
        Set<Class<? extends Annotation>> set = CollectionUtil.newHashSet();
        for(Annotation anno: clz.getAnnotations())
        {
            JoindataComponent jc = anno.annotationType().getAnnotation(JoindataComponent.class);
            if(jc == null)
            {
                continue;
            }
            set.add(anno.annotationType());
        }

        return set;
    }

}
