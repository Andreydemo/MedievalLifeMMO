package org.demosoft.medieval.life.gameserver.network;

import com.l2jserver.mmocore.IPacketHandler;
import com.l2jserver.mmocore.ReceivablePacket;
import org.apache.log4j.Logger;
import org.demosoft.medieval.life.gameserver.clientpackets.ProtocolVersion;

import java.nio.ByteBuffer;

/**
 * Created by Andrii on 4/10/2017.
 */
public class PacketHandlerImpl implements IPacketHandler<GameClient> {
    static Logger log = Logger.getLogger(PacketHandlerImpl.class);

    @Override
    public ReceivablePacket<GameClient> handlePacket(ByteBuffer byteBuffer, GameClient gameClient) {
        byte b = byteBuffer.get();
        int opcode = b & 0xFF;
        ReceivablePacket<GameClient> msg = null;
        GameClient.GameClientState state = gameClient.getState();
        log.debug("oppcode:" + opcode);
        switch (state) {
            case CONNECTED:
                if (opcode == 0x00) {
                    msg = new ProtocolVersion();
                }
                break;
            case AUTHORIZED:
                break;
            case IN_GAME:
                break;
        }

        return msg;
    }
}
