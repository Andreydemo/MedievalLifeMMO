package org.demosoft.medieval.life.gameserver.clientpackets;

import com.l2jserver.mmocore.ReceivablePacket;
import org.demosoft.medieval.life.Config;
import org.demosoft.medieval.life.gameserver.network.GameClient;

import java.util.logging.Logger;

/**
 * Created by Andrii on 4/10/2017.
 */
public abstract class GameClientPacket extends ReceivablePacket<GameClient> {

    private static final Logger log = Logger.getLogger(GameClientPacket.class.getName());

    @Override
    protected boolean read() {
        // System.out.println(this.getType());
        try {
            readImpl();
            return true;
        } catch (Throwable t) {
            log.severe("Client: " + getClient().toString() + " - Failed reading: " + getType() + " - L2J Server Version: " + Config.SERVER_VERSION + " - DP Revision: " + Config.DATAPACK_VERSION);
            t.printStackTrace();
        }
        return false;
    }

    protected abstract void readImpl();

    @Override
    public void run() {
       /* try {
            // flood protection
            if ((GameTimeController.getGameTicks() - getClient().packetsSentStartTick) > 10) {
                getClient().packetsSentStartTick = GameTimeController.getGameTicks();
                getClient().packetsSentInSec = 0;
            } else {
                getClient().packetsSentInSec++;
                if (getClient().packetsSentInSec > 12) {
                    if (getClient().packetsSentInSec < 100) {
                        sendPacket(new ActionFailed());
                    }
                    return;
                }
            }

            runImpl();
            if ((this instanceof MoveBackwardToLocation) || (this instanceof AttackRequest) || (this instanceof RequestMagicSkillUse))
            // could include pickup and talk too, but less is better
            {
                // Removes onspawn protection - player has faster computer than
                // average
                if (getClient().getActiveChar() != null) {
                    getClient().getActiveChar().onActionRequest();
                }
            }
        } catch (Throwable t) {
            log.severe("Client: " + getClient().toString() + " - Failed running: " + getType() + " - L2J Server Version: " + Config.SERVER_VERSION + " - DP Revision: " + Config.DATAPACK_VERSION);
            t.printStackTrace();
        }*/
    }

    protected abstract void runImpl();

    /*protected final void sendPacket(L2GameServerPacket gsp) {
        getClient().sendPacket(gsp);
    }*/

    /**
     * @return A String with this packet name for debuging purposes
     */
    public abstract String getType();
}
