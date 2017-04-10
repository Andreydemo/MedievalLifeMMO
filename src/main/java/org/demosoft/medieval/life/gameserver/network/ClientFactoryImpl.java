package org.demosoft.medieval.life.gameserver.network;

import com.l2jserver.mmocore.IClientFactory;
import com.l2jserver.mmocore.MMOConnection;

/**
 * Created by Andrii on 4/10/2017.
 */
public class ClientFactoryImpl implements IClientFactory<GameClient> {
    @Override
    public GameClient create(MMOConnection<GameClient> mmoConnection) {
        return new GameClient(mmoConnection);
    }
}
