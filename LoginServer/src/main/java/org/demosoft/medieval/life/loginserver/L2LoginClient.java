package org.demosoft.medieval.life.loginserver;

import com.l2jserver.mmocore.MMOConnection;
import com.l2jserver.mmocore.MMOClient;
import com.l2jserver.mmocore.SendablePacket;
import lombok.Getter;
import lombok.Setter;
import org.demosoft.medieval.life.loginserver.crypt.LoginCrypt;
import org.demosoft.medieval.life.loginserver.crypt.ScrambledKeyPair;
import org.demosoft.medieval.life.loginserver.serverpackets.L2LoginServerPacket;
import org.demosoft.medieval.life.loginserver.serverpackets.LoginFail;
import org.demosoft.medieval.life.loginserver.serverpackets.PlayFail;
import org.demosoft.medieval.life.util.Rnd;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Logger;

/**
 * Created by Andrii_Korkoshko on 4/25/2017.
 */
@Getter
@Setter
public class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>> {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(L2LoginClient.class);
    private final ScrambledKeyPair scrambledPair;
    private final byte[] blowfishKey;
    private final int sessionId;
    private final long connectionStartTime;
    private final LoginCrypt loginCrypt;
    private LoginClientState state;
    private boolean usesInternalIP;

    private boolean joinedGS;

    @Getter
    @Setter
    private String account;
    @Getter
    @Setter
    private int accessLevel;

    @Getter
    @Setter
    private SessionKey sessionKey;


    public L2LoginClient(MMOConnection<L2LoginClient> con) {
        super(con);
        state = LoginClientState.CONNECTED;
        String ip = getConnection().getInetAddress().getHostAddress();

        // TODO unhardcode this
        if (ip.startsWith("192.168") || ip.startsWith("10.0") || ip.equals("127.0.0.1")) {
            usesInternalIP = true;
        }

        scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
        blowfishKey = LoginController.getInstance().getBlowfishKey();
        sessionId = Rnd.nextInt();
        connectionStartTime = System.currentTimeMillis();
        loginCrypt = new LoginCrypt();
        loginCrypt.setKey(blowfishKey);
    }

    public static enum LoginClientState {
        CONNECTED,
        AUTHED_GG,
        AUTHED_LOGIN
    }

    @Override
    public boolean decrypt(ByteBuffer buf, int size)
    {
        boolean ret = true;
        /*try
        {
            ret = loginCrypt.decrypt(buf.array(), buf.position(), size);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            super.getConnection().close((SendablePacket<L2LoginClient>) null);
            return false;
        }*/

        if (!ret)
        {
            byte[] dump = new byte[size];
            System.arraycopy(buf.array(), buf.position(), dump, 0, size);
            log.warn("Wrong checksum from client: " + toString());
            super.getConnection().close((SendablePacket<L2LoginClient>) null);
        }

        return ret;
    }

    @Override
    public boolean encrypt(ByteBuffer buf, int size)
    {
        final int offset = buf.position();
        /*try
        {
            size = loginCrypt.encrypt(buf.array(), offset, size);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }*/

        buf.position(offset + size);
        return true;
    }

    @Override
    protected void onDisconnection() {

    }

    @Override
    protected void onForcedDisconnection() {

    }

    public byte[] getScrambledModulus() {
        return scrambledPair._scrambledModulus;
    }

    public void sendPacket(L2LoginServerPacket lsp) {
        getConnection().sendPacket(lsp);
    }

    public void close(LoginFail.LoginFailReason reason) {
        getConnection().close(new LoginFail(reason));
    }

    public void close(PlayFail.PlayFailReason reason) {
        getConnection().close(new PlayFail(reason));
    }

    public void close(L2LoginServerPacket lsp) {
        getConnection().close(lsp);
    }

    public RSAPrivateKey getRSAPrivateKey() {
        return (RSAPrivateKey) scrambledPair._pair.getPrivate();
    }

}
