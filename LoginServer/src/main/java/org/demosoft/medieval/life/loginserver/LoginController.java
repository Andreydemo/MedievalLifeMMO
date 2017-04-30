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
package org.demosoft.medieval.life.loginserver;

import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.commons.logging.Log;
import org.demosoft.medieval.life.loginserver.crypt.ScrambledKeyPair;
import org.demosoft.medieval.life.util.Rnd;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
@Component
public class LoginController {
    protected static final Logger _log = Logger.getLogger(LoginController.class.getName());

    private static LoginController _instance;

    /**
     * Time before kicking the client if he didnt logged yet
     */
    private final static int LOGIN_TIMEOUT = 60 * 1000;

    /**
     * Clients that are on the LS but arent assocated with a account yet
     */
    protected FastSet<L2LoginClient> _clients = new FastSet<>();

    /**
     * Authed Clients on LoginServer
     */
    protected FastMap<String, L2LoginClient> _loginServerClients = new FastMap<String, L2LoginClient>().setShared(true);


    protected ScrambledKeyPair[] _keyPairs;

    protected byte[][] _blowfishKeys;
    private static final int BLOWFISH_KEYS = 20;

    @PostConstruct
    public static void load() throws GeneralSecurityException {
        if (_instance == null) {
            _instance = new LoginController();
        } else {
            throw new IllegalStateException("LoginController can only be loaded a single time.");
        }
    }

    public static LoginController getInstance() {
        return _instance;
    }

    private LoginController() throws GeneralSecurityException {
        _log.info("Loading LoginContoller...");


        _keyPairs = new ScrambledKeyPair[10];

        KeyPairGenerator keygen = null;

        keygen = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
        keygen.initialize(spec);

        // generate the initial set of keys
        for (int i = 0; i < 10; i++) {
            _keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
        }
        _log.info("Cached 10 KeyPairs for RSA communication");

        testCipher((RSAPrivateKey) _keyPairs[0]._pair.getPrivate());

        // Store keys for blowfish communication
        generateBlowFishKeys();
    }

    /**
     * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<BR>
     * In short it avoids the worst-case execution time on runtime by doing it on loading.
     *
     * @param key Any private RSA Key just for testing purposes.
     * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
     */
    private void testCipher(RSAPrivateKey key) throws GeneralSecurityException {
        // avoid worst-case execution, KenM
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
    }

    private void generateBlowFishKeys() {
        _blowfishKeys = new byte[BLOWFISH_KEYS][16];

        for (int i = 0; i < BLOWFISH_KEYS; i++) {
            for (int j = 0; j < _blowfishKeys[i].length; j++) {
                _blowfishKeys[i][j] = (byte) (Rnd.nextInt(255) + 1);
            }
        }
        _log.info("Stored " + _blowfishKeys.length + " keys for Blowfish communication");
    }

    /**
     * @return Returns a random key
     */
    public byte[] getBlowfishKey() {
        return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
    }

    public void addLoginClient(L2LoginClient client) {
        synchronized (_clients) {
            _clients.add(client);
        }
    }

    public void removeLoginClient(L2LoginClient client) {
        synchronized (_clients) {
            _clients.remove(client);
        }
    }

    public SessionKey assignSessionKeyToClient(String account, L2LoginClient client) {
        SessionKey key;

        key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
        _loginServerClients.put(account, client);
        return key;
    }

    public void removeAuthedLoginClient(String account) {
        _loginServerClients.remove(account);
    }

    public boolean isAccountInLoginServer(String account) {
        return _loginServerClients.containsKey(account);
    }

    public L2LoginClient getAuthedClient(String account) {
        return _loginServerClients.get(account);
    }

    public static enum AuthLoginResult {
        INVALID_PASSWORD,
        ACCOUNT_BANNED,
        ALREADY_ON_LS,
        ALREADY_ON_GS,
        AUTH_SUCCESS
    }


    public SessionKey getKeyForAccount(String account) {
        L2LoginClient client = _loginServerClients.get(account);
        if (client != null) {
            return client.getSessionKey();
        }
        return null;
    }


    /**
     * <p>
     * This method returns one of the cached {@link ScrambledKeyPair ScrambledKeyPairs} for communication with Login Clients.
     * </p>
     *
     * @return a scrambled keypair
     */
    public ScrambledKeyPair getScrambledRSAKeyPair() {
        return _keyPairs[Rnd.nextInt(10)];
    }

    /**
     * @param client
     * @param serverId
     * @return
     */
    public boolean isLoginPossible(L2LoginClient client, int serverId) {

        return true;
    }

    public AuthLoginResult tryAuthLogin(String account, String password, L2LoginClient client) {
        AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
        // check auth
        if (loginValid(account, password, client)) {
            // login was successful, verify presence on Gameservers
            ret = AuthLoginResult.ALREADY_ON_GS;
            // account isnt on any GS verify LS itself
            ret = AuthLoginResult.ALREADY_ON_LS;

            // dont allow 2 simultaneous login
            synchronized (_loginServerClients) {
                if (!_loginServerClients.containsKey(account)) {
                    _loginServerClients.put(account, client);
                    ret = AuthLoginResult.AUTH_SUCCESS;

                    // remove him from the non-authed list
                    removeLoginClient(client);
                }
            }
        } else {
            if (client.getAccessLevel() < 0) {
                ret = AuthLoginResult.ACCOUNT_BANNED;
            }
        }
        return ret;
    }

    public boolean loginValid(String user, String password, L2LoginClient client) {


        return true;
    }

    public GameServerTable.GameServerInfo getAccountOnGameServer(String account) {
        Collection<GameServerTable.GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
        for (GameServerTable.GameServerInfo gsi : serverList) {

            return gsi;
        }
        return null;
    }

}
