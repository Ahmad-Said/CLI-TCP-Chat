/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.TCPChat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import said.ahmad.ul.TCPChat.ServerReply;
import said.ahmad.ul.TCPChat.ServerReplyException;

public class Server {
	private static final int PORT = 1234;
	private static ExecutorService clientsServerExecutors = Executors.newCachedThreadPool();

	private ServerSocket servSock;

	private final ArrayList<ClientConnection> clients = new ArrayList<>();

	/**
	 * Start a server and listens for any connection to be made to this socket and
	 * serve it. The server will keep running until interrupted by user.
	 */
	public Server() {
		try {
			servSock = new ServerSocket(PORT);
			System.out.println("Starting server at port " + servSock.getLocalPort());
		} catch (IOException ex) {
			System.out.println("Unable to connecto to this port: " + PORT);
			return;
		}
		startListening();
	}

	private void startListening() {
		while (true) {
			try {
				Socket link = servSock.accept();
				ClientHandler cl = new ClientHandler(link, this);
				clientsServerExecutors.execute(cl);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void removeClient(ClientConnection client) {
		clients.remove(client);
	}

	public void addClient(ClientConnection client) {
		clients.add(client);
	}

	public void broadCastMessage(String sender, String message) {
		clients.stream().forEach(c -> {
			try {
				c.sendReply(ServerReply.INFO, sender + " broadcasted: " + message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void sendMessage(String sender, String otherClientName, String message)
			throws ServerReplyException, IOException {
		Optional<ClientConnection> otherClient = clients.stream().filter(c -> c.getName().equals(otherClientName))
				.findAny();
		if (otherClient.isPresent()) {
			otherClient.get().sendReply(ServerReply.INFO, sender + " saying to you: " + message);
		} else {
			throw new ServerReplyException(otherClientName + "Name not found in the list: " + getClientsList());
		}
	}

	public String getClientsList() {
		return "There are " + clients.size() + " clients:\n\t- "
				+ clients.stream().map(c -> c.getName()).collect(Collectors.joining("\n\t- "));
	}

}
