/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.TCPChat.server;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import said.ahmad.ul.TCPChat.Command;
import said.ahmad.ul.TCPChat.CommandFormatException;
import said.ahmad.ul.TCPChat.ServerReply;
import said.ahmad.ul.TCPChat.ServerReplyException;

public class ClientHandler implements Runnable {
	Server server;
	ClientConnection client;

	public ClientHandler(Socket link, Server server) throws IOException {
		client = new ClientConnection(link);
		this.server = server;
		server.addClient(client);
	}

	public String getClientHelp() {
		return "Your name is " + client.getName() + "\n"
				+ Stream.of(Command.values()).filter(c -> !c.equals(Command.ENREGISTRER))
						.map(c -> Command.getInfoFormat(c)).collect(Collectors.joining("\n"));
	}

	@Override
	public void run() {
		String messageFromClient;
		String tokens[];
		Command command;
		try {
			do {
				messageFromClient = client.readCommand();
//				System.out.println(client.name + " send " + messageFromClient);
				tokens = messageFromClient.split(":", 2);
				try {
					command = Command.valueOf(tokens[0].toUpperCase());
				} catch (Exception e) {
					client.sendReply(ServerReply.ERROR, "Invalid Command! " + Command.getInfoFormat(Command.HELP));
					continue;
				}
				if (!client.isRegistered()) {
					if (!command.equals(Command.ENREGISTRER) || tokens.length != 2) {
						client.sendReply(ServerReply.ERROR,
								"Require Registration first" + Command.getInfoFormat(Command.ENREGISTRER));
						continue;
					}
					client.setName(tokens[1]);
					client.sendReply(ServerReply.ENREGISTREMENT_OK,
							"Hello " + client.getName() + ", Welcome to our tiny chat app.\n" + getClientHelp());
					client.setRegistered(true);
					System.out.println(client.getName() + " connected!");
					continue;
				}
				try {
					switch (command) {
					case HELP:
						client.sendReply(ServerReply.COMMAND_RECIEVED, getClientHelp());
						break;
					case ENREGISTRER:
						client.sendReply(ServerReply.COMMAND_RECIEVED,
								"You already registered\n" + "Your name is: " + client.getName());
						break;
					case END:
						closeconnection();
						break;
					case BROADCAST:
						if (tokens.length != 2) {
							throw new CommandFormatException(command);
						}
						client.sendReply(ServerReply.COMMAND_RECIEVED, null);
						broadcastMessage(tokens[1]);
						break;
					case MESSAGE:
						tokens = messageFromClient.split(":", 3);
						if (tokens.length != 3) {
							throw new CommandFormatException(command);
						}
						client.sendReply(ServerReply.COMMAND_RECIEVED, null);
						sendMessage(tokens[1], tokens[2]);
						break;
					case LIST:
						client.sendReply(ServerReply.COMMAND_RECIEVED, server.getClientsList());
						break;
					default:
						client.sendReply(ServerReply.ERROR, command + " Command is not yet supported\n");
						break;
					}
				} catch (CommandFormatException e) {
					client.sendReply(ServerReply.ERROR, e.getMessage());
				} catch (ServerReplyException e) {
					client.sendReply(ServerReply.ERROR, e.getMessage());
				} catch (IOException e) {
					if (client.isConnected()) {
						client.sendReply(ServerReply.ERROR, e.getMessage());
					}
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (true);
		} catch (IOException ex) {
			System.err.println("Connection to " + client.getName() + " get lost!");
			closeconnection();
		}
	}

	private void sendMessage(String otherClientName, String message) throws ServerReplyException, IOException {
		server.sendMessage(client.getName(), otherClientName, message);
	}

	private void broadcastMessage(String message) {
		server.broadCastMessage(client.getName(), message);
	}

	public void closeconnection() {
		server.removeClient(client);
		try {
			client.close();
		} catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
