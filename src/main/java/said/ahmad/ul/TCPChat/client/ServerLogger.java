/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.TCPChat.client;

import java.io.IOException;
import java.net.SocketException;

import javafx.util.Pair;
import said.ahmad.ul.TCPChat.ServerReply;

class ServerLogger extends Thread {
	ServerConnection server;
	int timeOutToReconnect;

	public ServerLogger(ServerConnection server, int timeOutToReconnect) {
		this.server = server;
		this.timeOutToReconnect = timeOutToReconnect <= 0 ? 5 : timeOutToReconnect;
	}

	@Override
	public void run() {
		Pair<ServerReply, String> message_in;
		int sleepTimeOut = 0;
		boolean doReconnect = false;
		while (true) {
			try {
				if (sleepTimeOut != 0) {
					doReconnect = false;
					System.out.println("Reconneting in " + sleepTimeOut + " seconds");
					sleepTimeOut--;
					Thread.sleep(1000);
					if (sleepTimeOut == 0) {
						server.reconnectAsNew();
						if (server.getClientName() != null) {
							server.sendCommand(server.getRegisterCommand(server.getClientName()));
						}
					}
					continue;
				}

				message_in = server.readReply();
				System.out.println(message_in.getValue());
				System.out.println("-----------------------------------------------");

			} catch (SocketException e) {
				if (e.getMessage().equals("Socket closed")) {
					// ignore socket closed exception and stop logging
					break;
				} else {
					e.printStackTrace();
					doReconnect = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				doReconnect = true;
			} catch (InterruptedException e) {
				break;
			}
			if (doReconnect) {
				sleepTimeOut = timeOutToReconnect;
				System.err.println("Connection to server lost!");
			}
		}
	}

}
