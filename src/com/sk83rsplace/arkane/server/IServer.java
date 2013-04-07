package com.sk83rsplace.arkane.server;

import com.sk83rsplace.arkane.server.client.IClient;

public interface IServer {
	public String getServerName();
	public String getServerHost();
	public int getServerPort();
	public boolean getServerType();
	public int getMaxPlayerCount();
	public int getPlayerCount();
	public int calculatePing(IClient c);
	public String[] getClients();
}
