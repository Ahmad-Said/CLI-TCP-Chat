package said.ahmad.ul.TCPChat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javafx.util.Pair;
import said.ahmad.ul.TCPChat.Command;
import said.ahmad.ul.TCPChat.ServerReply;

public class ServerConnection {

	private static int timeOutToReconnectIfLost = 5;

	private InetAddress host;
	private int port;
	private Socket link;

	private DataInputStream inFromServer;
	private DataOutputStream outToServer;

	private ServerLogger logger;

	private String clientName;

	public ServerConnection(Socket link) throws IOException {
		this.link = link;
		host = link.getInetAddress();
		port = link.getPort();
		initializeStream();
	}

	public void reconnectAsNew() throws IOException {
		if (link.isConnected()) {
			link.close();
		}
		link = new Socket(host, port);
		initializeStream();
	}

	private void initializeStream() throws IOException {
		inFromServer = new DataInputStream(link.getInputStream());
		outToServer = new DataOutputStream(link.getOutputStream());
	}

	/** Note this will clear all output stream to system output */
	public void startThreadLogging() {
		if (logger == null) {
			logger = new ServerLogger(this, timeOutToReconnectIfLost);
			logger.start();
		}
	}

	public void stopLogging() {
		if (logger != null) {
			logger.interrupt();
		}
	}

	/**
	 * @return the clientName
	 */
	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getRegisterCommand(String clientName) {
		return Command.ENREGISTRER + ":" + clientName;
	}

	public void sendCommand(String command) throws IOException {
		outToServer.writeUTF(command);
	}

	public Pair<ServerReply, String> readReply() throws IOException {
		String reply = inFromServer.readUTF();
		String message = inFromServer.readUTF();
		ServerReply serverReply;
		try {
			serverReply = ServerReply.valueOfIgnoringCase(reply);
		} catch (Exception e) {
			serverReply = ServerReply.ERROR;
		}
		return new Pair<ServerReply, String>(serverReply, message);
	}

	public void close() throws IOException {
		stopLogging();
		link.close();
	}
}
