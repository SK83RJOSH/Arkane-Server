package com.sk83rsplace.arkane.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sk83rsplace.arkane.server.client.ClientConnection;
import com.sk83rsplace.arkane.server.client.IClient;
import com.sk83rsplace.arkane.server.game.GameThread;

public class ThreadedServer implements IServer {
	public static CopyOnWriteArrayList<IClient> clients = new CopyOnWriteArrayList<IClient>();
	private static int maxPlayers = 6;
	private static int serverPort = 3371;
	private static boolean publicServer = true;
	private static boolean running = true;
	private static ServerSocket socket = null;
	private static final String REASON_LEAVING = "Leaving";
	private static final String REASON_KICKED = "Kicked";
	private static final String REASON_TIMEOUT = "Timeout";

	public static void main(String[] args) {
		try {
			socket = new ServerSocket(serverPort, maxPlayers + 1, java.net.InetAddress.getLocalHost());
			System.out.println("Server started!");
		} catch (IOException e) {
			System.err.println("Couldn't bind to socket " + serverPort + ", perhaps a server is already running on that port?");
			shutdown();
			return;
		}
		 
		new GameThread();
		
		Socket connection = null;
				 
		while(running != false) {			
			try {
				connection = socket.accept();
				clients.add(new ClientConnection(connection));
			} catch (Exception e) {
				System.err.println("There was a problem accepting the most recent connection.");
				//shutdown();
			}			
		}
	}
	
	private static void shutdown() {
		try {
			socket.close();
			
			for(IClient c : clients)
				c.quit("Server shutting down.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Server shutting down.");
		System.exit(0);
	}

	public String getServerName() {
		return null;
	}

	public String getServerHost() {
		return null;
	}
	
	public int getServerPort() {
		return 0;
	}
	
	public boolean getServerType() {
		return publicServer;
	}

	public int getMaxPlayerCount() {
		return maxPlayers;
	}

	public int getPlayerCount() {
		return clients.size();
	}

	public int calculatePing(IClient c) {
		return 0;
	}

	public String[] getClients() {
		String[] currentClients = new String[getPlayerCount()];
		
		for(int client = 0; client < getPlayerCount(); client++)
			currentClients[client] = clients.get(client).getPlayerName();
		
		return currentClients;
	}
	
	public static void playerConnected(IClient c) {
		if(clients.size() <= maxPlayers) {
			System.out.println(c.getPlayerName() + " Connected!");	
		} else {
			c.update("Kicked Max Players reached!");
		}
		//c.update("Kicked Test"); //Kick the fucker.
	}
	
	public static void playerDisconnected(IClient c, String type) {
		switch(type) {
			case REASON_KICKED:
				System.out.println(c.getPlayerName() + " was kicked!");
				break;
			case REASON_LEAVING:
				System.out.println(c.getPlayerName() + " disconnected!");
				break;
			case REASON_TIMEOUT:
				System.out.println(c.getPlayerName() + " timed out!");
				break;
		}
		
		for(IClient cl : clients)
			if(cl != c)
				cl.update("Disconnect " + c.getPlayerName());
		
		clients.remove(c);
	}
}
