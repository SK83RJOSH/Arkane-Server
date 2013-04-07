package com.sk83rsplace.arkane.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.sk83rsplace.arkane.server.ThreadedServer;

public class ClientConnection extends Thread implements IClient {
	private Socket socket;
	private String username = "Bob";
    PrintWriter out = null;
    BufferedReader in = null;
    private static final String ACTION_USERNAME = "Username";
    private static final String ACTION_DISCONNECT = "Disconnecting";
    private static final String ACTION_HEARTBEAT = "Ping";
    private static final String ACTION_HEARTBEAT_RECEIVED = "Pong";
    private static final String CLIENT_UPDATE = "Update";
    private static final String CLIENT_CONNECT = "Connect";
    private static final String SERVER_STAT = "Stats";
    private boolean running;
    private int heartbeats;
    private long ping;
    private long sent_time;
    
    public ClientConnection(Socket clientSocket) throws IOException {
        this.socket = clientSocket;
    	ping = 0;
    	sent_time = 0;
    	heartbeats = 0;
    	running = true;
    	        
        try {
	        out = new PrintWriter(socket.getOutputStream(), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException e ) {
    		e.printStackTrace();
    	}
                
        this.start();
    }
    
    public void run() {
    	super.run();

        String clientMessage = "";
        String lastMessage = "";
                
        while(running != false) {	
			try {
				clientMessage = in.readLine();
			} catch (IOException e) {
				clientMessage = "";
			}
						
			if(clientMessage != lastMessage && clientMessage != null) {
				String args[] = clientMessage.split(" ");
				
				switch(args[0]) {
					case ACTION_USERNAME:
						username = args[1];
						connect();
						break;
					case ACTION_DISCONNECT:
						running = false;
						quit(args[1]);
						break;
					case ACTION_HEARTBEAT_RECEIVED:
						heartbeats--;
						ping = System.currentTimeMillis() - sent_time;
						System.out.println(username + ", Ping: " + ping + ". Heartbeats missed: " + heartbeats);
						break;
					case ACTION_HEARTBEAT:
						out.println("Pong");
						break;
					case CLIENT_UPDATE:
						for(IClient c : ThreadedServer.clients)
							if(c != this)
								c.update(clientMessage);						
						break;
					case CLIENT_CONNECT:
				        out.println("Connect OK");
						break;
					case SERVER_STAT:
						ThreadedServer.clients.remove(this);
						out.println(ThreadedServer.getPlayerCount() + " " + ThreadedServer.getMaxPlayerCount());
						break;
				}
									
				lastMessage = clientMessage;
			}						
		}        
    }

	public String getPlayerName() {
		return username;
	}

	public String getPlayerAddress() {
		return socket.getInetAddress().getHostAddress();
	}
	
	public void quit(String reason) {		
		try {
			ThreadedServer.playerDisconnected(this, reason);
			running = false;
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void update(String update) {
		String args[] = update.split(" ");
		
		switch(args[0]) {
			case ACTION_HEARTBEAT:
				heartbeats++;
				
				if(heartbeats > 5) {
					try {
						socket.close();
						running = false;
						out.println("Kicked Timed out!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					ThreadedServer.playerDisconnected(this, "Timeout");		
				}
				
				sent_time = System.currentTimeMillis();
				break;
		}
		
		out.println(update);
	}
	
	private void connect() {
		ThreadedServer.playerConnected(this);
		
		for(IClient c : ThreadedServer.clients)
			if(c != this)
				out.println("Create " + c.getPlayerName() + " 0 0");
	}
}
