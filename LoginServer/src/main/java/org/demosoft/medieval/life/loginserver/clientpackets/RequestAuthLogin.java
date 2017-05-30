/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package org.demosoft.medieval.life.loginserver.clientpackets;


import org.apache.log4j.Logger;
import org.demosoft.medieval.life.loginserver.Config;
import org.demosoft.medieval.life.loginserver.GameServerTable;
import org.demosoft.medieval.life.loginserver.L2LoginClient;
import org.demosoft.medieval.life.loginserver.LoginController;
import org.demosoft.medieval.life.loginserver.serverpackets.AccountKicked;
import org.demosoft.medieval.life.loginserver.serverpackets.LoginFail;
import org.demosoft.medieval.life.loginserver.serverpackets.LoginOk;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

/**
 * Format: x 0 (a leading null) x: the rsa encrypted block with the login an password
 */
public class RequestAuthLogin extends L2LoginClientPacket {
    private final byte[] _raw = new byte[128];

    protected static final Logger log = Logger.getLogger(LoginController.class.getName());

    private String _user;
    private String _password;
    private int _ncotp;

    /**
     * @return
     */
    public String getPassword() {
        return _password;
    }

    /**
     * @return
     */
    public String getUser() {
        return _user;
    }

    public int getOneTimePassword() {
        return _ncotp;
    }

    @Override
    public boolean readImpl() {
        if (_buf.remaining() >= 128) {
            readB(_raw);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        byte[] decrypted = null;
       /* try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(Cipher.DECRYPT_MODE, getClient().getRSAPrivateKey());
            decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return;
        }*/
            decrypted= _raw;

        _user = new String(decrypted, 91, 14).trim();
        _user = _user.toLowerCase();
        _password = new String(decrypted, 105, 16).trim();
        _ncotp = decrypted[0x7c];
        _ncotp |= decrypted[0x7d] << 8;
        _ncotp |= decrypted[0x7e] << 16;
        _ncotp |= decrypted[0x7f] << 24;

        LoginController lc = LoginController.getInstance();
        L2LoginClient client = getClient();
        LoginController.AuthLoginResult result = lc.tryAuthLogin(_user, _password, getClient());

        switch (result) {
            case AUTH_SUCCESS:
                client.setAccount(_user);
                client.setState(L2LoginClient.LoginClientState.AUTHED_LOGIN);
                client.setSessionKey(lc.assignSessionKeyToClient(_user, client));
                if (Config.SHOW_LICENCE) {
                    client.sendPacket(new LoginOk(getClient().getSessionKey()));
                } else {
                    log.error("OCHKO");
                }
                break;
            case INVALID_PASSWORD:
                client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
                break;
            case ACCOUNT_BANNED:
                client.close(new AccountKicked(AccountKicked.AccountKickedReason.REASON_PERMANENTLY_BANNED));
                break;
            case ALREADY_ON_LS:
                L2LoginClient oldClient;
                if ((oldClient = lc.getAuthedClient(_user)) != null) {
                    // kick the other client
                    oldClient.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
                    lc.removeAuthedLoginClient(_user);
                }
                break;
            case ALREADY_ON_GS:
                GameServerTable.GameServerInfo gsi;
                if ((gsi = lc.getAccountOnGameServer(_user)) != null) {
                    client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);

                    // kick from there
                    if (gsi.isAuthed()) {
                       // gsi.getGameServerThread().kickPlayer(_user);
                    }
                }
                break;
        }
    }
}
