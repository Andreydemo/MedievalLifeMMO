package org.demosoft.medieval.life.gameserver.network;

import com.l2jserver.mmocore.MMOConnection;
import com.l2jserver.mmocore.MMOClient;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

/**
 * Created by Andrii on 4/10/2017.
 */
public class GameClient extends MMOClient<MMOConnection<GameClient>> {

    /**
     * CONNECTED - client has just connected AUTHORIZED - client has authed but doesnt has character attached to it yet IN_GAME - client has selected a char and is in game
     *
     * @author KenM
     */
    public static enum GameClientState {
        DEBUG,
        CONNECTED,
        AUTHORIZED,
        IN_GAME
    }

    public GameCrypt crypt = new GameCrypt();
    public int packetsSentStartTick = 0;
    public byte packetsSentInSec = 0;
    @Getter
    @Setter
    public GameClientState state = GameClientState.DEBUG;

    public GameClient(MMOConnection<GameClient> con) {
        super(con);
    }

    @Override
    public boolean decrypt(ByteBuffer buf, int size) {
        crypt.decrypt(buf.array(), buf.position(), size);
        return true;
    }

    @Override
    public boolean encrypt(ByteBuffer byteBuffer, int i) {
        return false;
    }

    @Override
    protected void onDisconnection() {

    }

    @Override
    protected void onForcedDisconnection() {

    }

    public byte[] enableCrypt() {
        byte[] key = BlowFishKeygen.getRandomKey();
        crypt.setKey(key);
        return key;
    }

    /**
     * Close client connection with {@link ServerClose} packet
     */
    public void closeNow() {
        /*close(ServerClose.STATIC_PACKET);
        synchronized (this)
        {
            if (_cleanupTask != null)
            {
                cancelCleanup();
            }
            _cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0); // instant
        }*/
    }

}
