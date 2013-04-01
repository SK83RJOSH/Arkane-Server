package com.sk83rsplace.arkane.server.client;

public interface IClient {
	public String getPlayerName();
	public String getPlayerAddress();
	public void quit(String reason);
	public void update(String update);
}
