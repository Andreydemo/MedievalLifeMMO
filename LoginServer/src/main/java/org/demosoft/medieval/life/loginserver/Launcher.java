package org.demosoft.medieval.life.loginserver;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Created by Andrii on 4/10/2017.
 */
@Configuration
@ComponentScan("org.demosoft.medieval.life.loginserver")
public class Launcher {

    static Logger log = Logger.getLogger(Launcher.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Launcher.class);
        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(log::info);
    }
}
