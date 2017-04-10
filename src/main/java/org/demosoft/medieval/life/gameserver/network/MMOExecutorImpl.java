package org.demosoft.medieval.life.gameserver.network;

import com.l2jserver.mmocore.IMMOExecutor;
import com.l2jserver.mmocore.ReceivablePacket;
import org.demosoft.medieval.life.gameserver.ThreadPoolManager;

import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

/**
 * Created by Andrii on 4/10/2017.
 */
public class MMOExecutorImpl implements IMMOExecutor<GameClient> {
    private static final Logger log = Logger.getLogger(MMOExecutorImpl.class.getName());

    @Override
    public void execute(ReceivablePacket<GameClient> receivablePacket) {
        try {
            if (receivablePacket.getClient().getState() == GameClient.GameClientState.IN_GAME) {
                ThreadPoolManager.getInstance().executePacket(receivablePacket);
            } else {
                ThreadPoolManager.getInstance().executeIOPacket(receivablePacket);
            }
        } catch (RejectedExecutionException e) {
            // if the server is shutdown we ignore
            if (!ThreadPoolManager.getInstance().isShutdown()) {
                log.severe("Failed executing: " + receivablePacket.getClass().getSimpleName() + " for Client: " + receivablePacket.getClient().toString());
            }
        }
    }
}
