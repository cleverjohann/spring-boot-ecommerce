package com.example.springbootecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono.
 * Optimiza el rendimiento separando tareas no críticas del hilo principal.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configuración del pool de hilos para tareas asíncronas.
     * Optimizado para operaciones como envío de emails y notificaciones.
     */
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configuracion del pool de hilos
        executor.setCorePoolSize(2); //Hilos maximos activos
        executor.setMaxPoolSize(5); //Maximo de hilos
        executor.setQueueCapacity(100); //Cola de tareas pendientes
        executor.setThreadNamePrefix("async-task");

        // Politica de rechazo: el hilo que llama ejecuta la tarea
        executor.setRejectedExecutionHandler(
                (runnable, threadPoolExecutor) -> {
                    log.warn("Task queue is full. Running task in caller thread.");
                    runnable.run();
                }
        );

        // Esperar a que terminen las tareas al cerrar la aplicacion
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Manejador de excepciones para tareas asíncronas.
     * Las excepciones no manejadas en métodos @Async llegan aquí.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error("Exception occurred in async method: {} with arguments: {}",
                    method.getName(), objects, throwable);

            // Aquí podrías enviar una notificación a un sistema de monitoreo
            // o guardar el error en una base de datos para análisis posterior
        };
    }

    /**
     * Executor específico para operaciones de email.
     * Pool separado para evitar que emails lentos bloqueen otras tareas.
     */
    @Bean(name = "emailExecutor")
    public Executor emailTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("email-task");

        executor.setRejectedExecutionHandler(
                (runnable, threadPoolExecutor) -> {
                    log.warn("Email queue is full. Email will be processed later.");
                }
        );

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * Executor para operaciones de notificaciones push.
     * Pool dedicado para notificaciones en tiempo real.
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("notification-task");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);

        executor.initialize();
        return executor;
    }

}
