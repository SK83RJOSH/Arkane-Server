package com.sk83rsplace.arkane.server.game;

import com.sk83rsplace.arkane.server.ThreadedServer;
import com.sk83rsplace.arkane.server.client.IClient;

public class GameThread extends Thread {
	private static int tick = 0;

	public GameThread() {
		this.start();
	}
	
	public void run() {
		super.run();
		
		while(true) {
			if(tick%30 == 0)
				for(IClient c : ThreadedServer.clients)
					c.update("Ping Pong");
			
			tick++;
			tick = tick%60;
			try {
				sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
