package org.demosoft.medieval.life.gameserver;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Andrii on 4/10/2017.
 */
@Component
public class GameServer {
    @PostConstruct
    void init(){
        System.out.println("Hello I am a game server");
    }

}
