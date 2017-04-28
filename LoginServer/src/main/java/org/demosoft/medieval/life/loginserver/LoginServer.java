package org.demosoft.medieval.life.loginserver;

import com.l2jserver.mmocore.SelectorConfig;
import com.l2jserver.mmocore.SelectorThread;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

@Component
public class LoginServer {

    static Logger log = Logger.getLogger(LoginServer.class);
    private SelectorThread<L2LoginClient> selectorThread;

    @PostConstruct
    void init() throws IOException {

        InetAddress bindAddress = getBindAddress();
        // TODO: Unhardcode this configuration options
        final SelectorConfig sc = new SelectorConfig();
        sc.MAX_READ_PER_PASS = 12; // Config.MMO_MAX_READ_PER_PASS;
        sc.MAX_SEND_PER_PASS = 12; // Config.MMO_MAX_SEND_PER_PASS;
        sc.SLEEP_TIME = 20; // Config.MMO_SELECTOR_SLEEP_TIME;
        sc.HELPER_BUFFER_COUNT = 20; // Config.MMO_HELPER_BUFFER_COUNT;
        sc.TCP_NODELAY = false; // Config.MMO_TCP_NODELAY;

        final L2LoginPacketHandler lph = new L2LoginPacketHandler();
        final SelectorHelper sh = new SelectorHelper();
        try
        {
            selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
        }
        catch (IOException e)
        {
            log.info( "FATAL: Failed to open Selector. Reason: " + e.getMessage(), e);
            System.exit(1);
        }

        try {
            selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
        } catch (IOException e) {
            log.info("FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
            System.exit(1);
        }
        selectorThread.start();
        log.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);
    }

    InetAddress getBindAddress() {
        InetAddress bindAddress = null;
        if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
        {
            try
            {
                bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
            }
            catch (UnknownHostException e1)
            {
                log.info("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage());
                if (Config.DEVELOPER)
                {
                    e1.printStackTrace();
                }
            }
        }
        return bindAddress;
    }
}
