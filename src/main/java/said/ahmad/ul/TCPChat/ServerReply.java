package said.ahmad.ul.TCPChat;

public enum ServerReply {
	ENREGISTREMENT_OK, ERROR, COMMAND_RECIEVED, INFO;

	public static ServerReply valueOfIgnoringCase(String command) {
		return ServerReply.valueOf(command.toUpperCase());
	}
}
