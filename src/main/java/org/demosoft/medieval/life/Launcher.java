package org.demosoft.medieval.life;

import org.apache.log4j.Logger;
import org.demosoft.medieval.life.gameserver.GameServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Created by Andrii on 4/10/2017.
 */
@Configuration
@ComponentScan("org.demosoft.medieval.life")
public class Launcher {

    static Logger log = Logger.getLogger(GameServer.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Launcher.class);
        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(log::info);
    }
}
