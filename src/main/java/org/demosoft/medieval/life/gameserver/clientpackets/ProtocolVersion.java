package org.demosoft.medieval.life.gameserver.clientpackets;


import org.demosoft.medieval.life.Config;

import java.util.logging.Logger;

/**
 * Created by Andrii on 4/10/2017.
 */
public class ProtocolVersion extends GameClientPacket {
    private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
    static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());

    private int _version;

    @Override
    protected void readImpl() {
        _version = readD();
    }

    @Override
    protected void runImpl() {
        // this packet is never encrypted
        if (_version == -2) {
            if (Config.DEBUG) {
                _log.info("Ping received");
            }
            // this is just a ping attempt from the new C2 client
            getClient().closeNow();
        } else if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION)) {
            _log.info("Client: " + getClient().toString() + " -> Protocol Revision: " + _version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is " + Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
            _log.warning("Wrong Protocol Version " + _version);
            getClient().closeNow();
        } else {
            if (Config.DEBUG) {
                _log.fine("Client Protocol Revision is ok: " + _version);
            }

           /* KeyPacket pk = new KeyPacket(getClient().enableCrypt());
            getClient().sendPacket(pk);*/
        }
    }


    /*
     * (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType() {
        return _C__00_PROTOCOLVERSION;
    }
}
