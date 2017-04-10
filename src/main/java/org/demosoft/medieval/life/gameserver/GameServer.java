package org.demosoft.medieval.life.gameserver;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Andrii on 4/10/2017.
 */
@Component
public class GameServer {

    static Logger log = Logger.getLogger(GameServer.class);

    @PostConstruct
    void init() {
        log.info("Hello I am a game server");
    }

}
