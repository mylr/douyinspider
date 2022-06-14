package com.lr.douyinspider.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName:ThreadPoolConfig
 * @Description: 线程池配置
 * @Auther: LR
 * @Date: 2022/6/13 20:22
 */
@Configuration
public class ThreadPoolConfig {

    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    @Bean
    public ThreadPoolTaskExecutor downloadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //此方法返回可用处理器的虚拟机的最大数量; 不小于1
        //int core = Runtime.getRuntime().availableProcessors();
        int core = 1;
        //设置核心线程数
        executor.setCorePoolSize(core);
        //设置最大线程数
        executor.setMaxPoolSize(core * 2 + 1);
        //除核心线程外的线程存活时间
        executor.setKeepAliveSeconds(3);
        //如果传入值大于0，底层队列使用的是LinkedBlockingQueue,否则默认使用SynchronousQueue
        executor.setQueueCapacity(40);
        //线程名称前缀
        executor.setThreadNamePrefix("thread-execute");
        //设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
