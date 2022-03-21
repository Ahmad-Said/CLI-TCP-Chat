package said.ahmad.ul.TCPChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.sun.istack.internal.Nullable;

import said.ahmad.ul.TCPChat.ServerReply;

public class ClientConnection {

	private String name;
	private boolean registered = false;

	private Socket link;
	private DataInputStream inFromClient;
	private DataOutputStream outToClient;

	public ClientConnection(Socket link) throws IOException {
		this.link = link;
		inFromClient = new DataInputStream(link.getInputStream());
		outToClient = new DataOutputStream(link.getOutputStream());
	}

	public String readCommand() throws IOException {
		return inFromClient.readUTF();
	}

	public void sendReply(ServerReply reply, @Nullable String message) throws IOException {
		outToClient.writeUTF(reply.toString());
		if (message != null) {
			outToClient.writeUTF(message);
		} else {
			outToClient.writeUTF("");
		}
	}

	public boolean isConnected() {
		return link.isConnected();
	}

	public void close() throws IOException {
		link.close();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the registered
	 */
	public boolean isRegistered() {
		return registered;
	}

	/**
	 * @param registered the registered to set
	 */
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}
}
