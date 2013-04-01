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
    private static boolean running = true;
    private static int heartbeats = 0;
    
    public ClientConnection(Socket clientSocket) throws IOException {
        this.socket = clientSocket;
    	
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
        
        out.println("Connect OK");
        
        while(running != false) {	
			try {
				clientMessage = in.readLine();
			} catch (IOException e) {
				clientMessage = "";
			}
						
			if(clientMessage != lastMessage) {
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
						heartbeats = 0;
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
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void update(String update) {
		String args[] = update.split(" ");
		
		switch(args[0]) {
			case ACTION_HEARTBEAT:
//				heartbeats++;
//				
//				if(heartbeats > 5)
//					ThreadedServer.playerDisconnected(this, "Timeout");		
//					try {
//						socket.close();
//					} catch (IOException e) {
//						out.println("Kicked Timed out!");
//						return;
//					}
				break;
		}
		
		out.println(update);
	}
	
	private void connect() {
		ThreadedServer.playerConnected(this);
	}
}
