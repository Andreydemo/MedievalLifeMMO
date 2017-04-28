package org.demosoft.medieval.life.gameserver;

import com.l2jserver.mmocore.SelectorConfig;
import com.l2jserver.mmocore.SelectorThread;
import org.apache.log4j.Logger;
import org.demosoft.medieval.life.gameserver.network.ClientFactoryImpl;
import org.demosoft.medieval.life.gameserver.network.GameClient;
import org.demosoft.medieval.life.gameserver.network.MMOExecutorImpl;
import org.demosoft.medieval.life.gameserver.network.PacketHandlerImpl;
import org.demosoft.medieval.life.util.IPv4Filter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Andrii on 4/10/2017.
 */
@Component
public class GameServer {

    static Logger log = Logger.getLogger(GameServer.class);
    private SelectorThread<GameClient> selectorThread;

    @PostConstruct
    void init() throws IOException {
        log.info("Hello I am a game server");
        log.info("I am starting");

        // TODO: Unhardcode this configuration options
        final SelectorConfig sc = new SelectorConfig();
        sc.MAX_READ_PER_PASS = 12; // Config.MMO_MAX_READ_PER_PASS;
        sc.MAX_SEND_PER_PASS = 12; // Config.MMO_MAX_SEND_PER_PASS;
        sc.SLEEP_TIME = 20; // Config.MMO_SELECTOR_SLEEP_TIME;
        sc.HELPER_BUFFER_COUNT = 20; // Config.MMO_HELPER_BUFFER_COUNT;
        sc.TCP_NODELAY = false; // Config.MMO_TCP_NODELAY;
        PacketHandlerImpl gph = new PacketHandlerImpl();
        MMOExecutorImpl executor = new MMOExecutorImpl();
        ClientFactoryImpl clientFactory = new ClientFactoryImpl();
        selectorThread = new SelectorThread<>(sc, executor, gph, clientFactory, new IPv4Filter());
        InetAddress bindAddress = null;
        if (!Config.GAMESERVER_HOSTNAME.equals("*")) {
            try {
                bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
            } catch (UnknownHostException e1) {
                log.warn("WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
            }
        }

        try {
            selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
            log.info("Port binded");
        } catch (IOException e) {
            log.warn("FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
            System.exit(1);
        }

        selectorThread.start();
        log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

    }

}
