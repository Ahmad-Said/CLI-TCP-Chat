/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.TCPChat.client;

import static java.lang.System.exit;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import javafx.util.Pair;
import said.ahmad.ul.TCPChat.Command;
import said.ahmad.ul.TCPChat.ServerReply;

public class Client {
	public static String host = "localhost";
	public static int PORT = 1234;

	ServerConnection server;

	public Client() {
		Pair<ServerReply, String> messageFromServer;

		String messageToServer = null;

		try {
			System.out.println("Connecting To " + host + ":" + PORT);
			Socket link = new Socket(host, PORT);
			server = new ServerConnection(link);
			System.out.println("Connected!");
		} catch (IOException ex) {
			System.out.println("Host not reachable!");
			exit(0);
		}
		try {
			Scanner inputClient = new Scanner(System.in);

			System.out.print("Please Enter your name: ");
			String clientName = inputClient.nextLine();
			String registerCommand = Command.ENREGISTRER + ":" + clientName;
			server.sendCommand(registerCommand);

			messageFromServer = server.readReply();
			System.out.println(messageFromServer.getValue());
			System.out.println("-----------------------------------------------");
			ServerReply messageParsedReply = messageFromServer.getKey();

			if (messageParsedReply.equals(ServerReply.ENREGISTREMENT_OK)) {
				// registration successful
				server.setClientName(clientName);
			} else {
				// registration failed
				System.out.println("Failed to register");
				server.close();
				exit(0);
			}
			// Message reception will be inside a thread running function
			server.startThreadLogging();
			do {
				messageToServer = inputClient.nextLine();
				server.sendCommand(messageToServer);
			} while (!Command.END.equalsIgnoringCase(messageToServer));

			// This is the END clause
			inputClient.close();
			System.out.println("Connection is closing...");
			server.close();
			System.out.println("Connection succesfully closed...");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}
