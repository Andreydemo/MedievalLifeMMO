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


import org.demosoft.medieval.life.loginserver.Config;
import org.demosoft.medieval.life.loginserver.LoginController;
import org.demosoft.medieval.life.loginserver.SessionKey;
import org.demosoft.medieval.life.loginserver.serverpackets.LoginFail;
import org.demosoft.medieval.life.loginserver.serverpackets.PlayFail;
import org.demosoft.medieval.life.loginserver.serverpackets.PlayOk;

/**
 * Fromat is ddc d: first part of session id d: second part of session id c: server ID
 */
public class RequestServerLogin extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;
	
	/**
	 * @return
	 */
	public int getSessionKey1()
	{
		return _skey1;
	}
	
	/**
	 * @return
	 */
	public int getSessionKey2()
	{
		return _skey2;
	}
	
	/**
	 * @return
	 */
	public int getServerID()
	{
		return _serverId;
	}
	
	@Override
	public boolean readImpl()
	{
		if (_buf.remaining() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		SessionKey sk = getClient().getSessionKey();
		
		// If we didn't showed the license we can't check these values
		if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			if (LoginController.getInstance().isLoginPossible(getClient(), _serverId))
			{
				getClient().setJoinedGS(true);
				getClient().sendPacket(new PlayOk(sk));
			}
			else
			{
				getClient().close(PlayFail.PlayFailReason.REASON_TOO_MANY_PLAYERS);
			}
		}
		else
		{
			getClient().close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
