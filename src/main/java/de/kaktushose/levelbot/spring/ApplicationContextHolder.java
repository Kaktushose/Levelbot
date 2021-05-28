package de.kaktushose.levelbot.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
