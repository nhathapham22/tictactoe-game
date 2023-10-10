
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;

public class EchoServer extends AbstractServer {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 5555;

    private ChatIF chatConsole;

    //Constructors ****************************************************
    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port, ChatIF chatConsole) {
        super(port);

        this.chatConsole = chatConsole;
        chatConsole.display("Echo Server is initialized with port " + port);
    }

    //Instance methods ************************************************
    /**
     * This method handles any messages received from the admin console.
     *
     * @param msg The message received from the console.
     */
    public void handleCommandFromAdmin(String message) {
        if (message.indexOf("#setPort") == 0) {
            try {
                int port = parseInt(message.substring("#setPort".length()).trim());
                this.setPort(port);
                chatConsole.display("Port is set to " + port);
            } catch (Exception e) {
                chatConsole.display("Cannot get port number!");
                chatConsole.display(e.getMessage());
            }
        } else if (message.equals("#start")) {
            try {
                this.listen(); //Start listening for connections
                chatConsole.display("Echo Server is started and ready at port " + this.getPort());
            } catch (Exception ex) {
                chatConsole.display("ERROR - Could not listen for clients!");
                chatConsole.display(ex.getMessage());
            }
        } else if (message.equals("#stop")) {
            Envelope env = new Envelope();
            env.setId("forceLogout");
            env.setContents("Server is shutting down, bye!");

            sendToAllClients(env);

            try {
                this.close();
                chatConsole.display("Echo Server is stopped");
            } catch (IOException ex) {
                chatConsole.display("Cannot close Echo Server");
                chatConsole.display(ex.getMessage());
            }
        } else if (message.equals("#quit")) {
            try {
                this.close();
                System.exit(0);
            } catch (IOException ex) {
                chatConsole.display(ex.getMessage());
                System.exit(255);
            }
        } else if (message.indexOf("#ison") == 0) {
            String user = message.substring("#ison".length()).trim();
            checkUserIsOn(user);
        } else if (message.equals("#userstatus")) {
            listUsersInRooms();
        } else if (message.indexOf("#joinroom") == 0) {
            String room1and2 = message.substring("#joinroom".length()).trim();
            String room1 = room1and2.substring(0, room1and2.indexOf(" ")).trim();
            String room2 = room1and2.substring(room1and2.indexOf(" ")).trim();
            moveUsersRoomsToOtherRooms(room1, room2);
        } else {
            this.sendToAllClients("<ADMIN>" + message);
        }
    }

    public void moveUsersRoomsToOtherRooms(String room1, String room2) {
        Thread[] clientThreadList = getClientConnections();
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            String userId = target.getInfo("userId").toString();
            String room = target.getInfo("room").toString();
            if (room.equals(room1)) {
                target.setInfo("room", room2);

                if (!checkDuplicateUserInRoom(target)) {
                    chatConsole.display("Moving " + userId + " to room " + room2);
                } else {
                    target.setInfo("room", room1);
                }
            }
        }
        notifyUserListChanged();
    }

    public void listUsersInRooms() {
        Thread[] clientThreadList = getClientConnections();
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            String userId = target.getInfo("userId").toString();
            String room = target.getInfo("room").toString();
            chatConsole.display(userId + " - " + room);
        }
    }

    public void checkUserIsOn(String user) {
        Thread[] clientThreadList = getClientConnections();
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            String userId = target.getInfo("userId").toString();
            String room = target.getInfo("room").toString();
            if (user.equals(userId)) {
                chatConsole.display(user + " is on in room " + room);
                return;
            }
        }

        chatConsole.display(user + " is not logged in");
    }

    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof Envelope) {
            Envelope env = (Envelope) msg;
            handleCommandFromClient(env, client);
        } else {
            chatConsole.display("Message received: " + msg + " from " + client);

            String userId = client.getInfo("userId").toString();

            this.sendToAllClientsInRoom(userId + ": " + msg, client);
        }
    }

    public void handleCommandFromClient(Envelope env, ConnectionToClient client) {
        if (env.getId().equals("login")) {
            String userId = env.getContents().toString();
            String room = client.getInfo("room").toString();

            if (room.length() == 0) {
                room = "lobby";
            }

            if (userId.length() == 0) {
                userId = "guest";
            }

            String prevUserId = client.getInfo("userId").toString();

            client.setInfo("userId", userId);
            client.setInfo("room", room);

            if (!checkDuplicateUserInRoom(client)) {
                notifyUserListChanged();
            } else {
                client.setInfo("userId", prevUserId);
            }
        }

        if (env.getId().equals("join")) {
            String roomName = env.getContents().toString();
            String prevRoomName = client.getInfo("room").toString();

            client.setInfo("room", roomName);

            if (!checkDuplicateUserInRoom(client)) {
                notifyUserListChanged();
            } else {
                client.setInfo("room", prevRoomName);
            }
        }

        if (env.getId().equals("pm")) {
            String target = env.getArg();
            String message = env.getContents().toString();
            sendToAClient(message, target, client);
        }

        if (env.getId().equals("yell")) {
            String message = env.getContents().toString();
            String userId = client.getInfo("userId").toString();
            sendToAllClients(userId + " yells: " + message);
        }

        if (env.getId().equals("who")) {
            sendRoomListToClient(client);
        }

        if (env.getId().indexOf("ttt") == 0) {
            handleTicTacToeCommand(client, env);
        }
    }

    public void handleTicTacToeCommand(ConnectionToClient client, Envelope env) {
        Thread[] clientThreadList = getClientConnections();
        TicTacToe ticTacToeContent = (TicTacToe) env.getContents();
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            if (target.getInfo("userId").toString().equals(env.getArg())) {
                try {
                    // This time we need to check game status of receiver
                    TicTacToe ongoingContent = (TicTacToe) target.getInfo("ttt");

                    // Do not invite other user which is playing (the receiver)!
                    if (ongoingContent != null && ongoingContent.getGameState() == 3) {
                        if (!client.getInfo("userId").toString().equals(ongoingContent.getPlayer1())
                                && !client.getInfo("userId").toString().equals(ongoingContent.getPlayer2())) {
                            client.sendToClient("<ADMIN>Player " + env.getArg() + " is playing!");
                            break;
                        }
                    }

                    // This time we need to check game status of sender
                    ongoingContent = (TicTacToe) client.getInfo("ttt");

                    // Don't invite again if you are playing!
                    if (ongoingContent != null
                            && ongoingContent.getGameState() == 3
                            && ticTacToeContent.getGameState() == 1) {
                        client.sendToClient("<ADMIN>You are playing!");

                        // And tell client to keep track of ongoing game
                        env.setContents(ongoingContent);
                        client.sendToClient(env);
                        break;
                    }

                    target.sendToClient(env);

                    // Except invite, message must be broadcast to both users
                    if (ticTacToeContent.getGameState() != 1) {
                        client.sendToClient(env);
                    }

                    target.setInfo("ttt", ticTacToeContent);
                    client.setInfo("ttt", ticTacToeContent);
                } catch (IOException ex) {
                    chatConsole.display("Cannot send tictactoe command to client");
                    chatConsole.display(ex.getMessage());
                }
                break;
            }
        }
    }

    public void sendRoomListToClient(ConnectionToClient client) {
        Envelope env = new Envelope();
        ArrayList<String> userList = new ArrayList<String>();
        String room = client.getInfo("room").toString();

        env.setId("who");
        env.setArg(room);

        Thread[] clientThreadList = getClientConnections();
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            String targetRoom = target.getInfo("room").toString();

            if (targetRoom.equals(room) && !target.equals(client)) {
                String encodedString = target.getInfo("userId").toString().replaceAll("&", "&amp;");
                userList.add(encodedString);
            }
        }

        env.setContents(userList);

        try {
            client.sendToClient(env);
        } catch (Exception e) {
            chatConsole.display("Failed to send userList to client");
            chatConsole.display(e.getMessage());
        }
    }

    public void sendToAllClientsInRoom(Object msg, ConnectionToClient client) {
        Thread[] clientThreadList = getClientConnections();
        String room = client.getInfo("room").toString();

        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            if (target.getInfo("room").equals(room)) {
                try {
                    target.sendToClient(msg);
                } catch (Exception ex) {
                    chatConsole.display("failed to send to client");
                    chatConsole.display(ex.getMessage());
                }
            }
        }
    }

    public void sendToAClient(Object msg, String pmTarget, ConnectionToClient client) {
        Thread[] clientThreadList = getClientConnections();

        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            if (target.getInfo("userId").equals(pmTarget)) {
                try {
                    target.sendToClient(msg);
                } catch (Exception ex) {
                    chatConsole.display("failed to send to private message");
                    chatConsole.display(ex.getMessage());
                }
            }
        }
    }

    public boolean checkDuplicateUserInRoom(ConnectionToClient client) {
        Thread[] clientThreadList = getClientConnections();
        for (Thread t : clientThreadList) {
            ConnectionToClient otherClient = (ConnectionToClient) t;

            if (client.equals(otherClient)) {
                continue;
            }

            if (client.getInfo("userId").toString().equals("guest")) {
                continue;
            }

            if (client.getInfo("room").toString().equals(otherClient.getInfo("room").toString())
                    && client.getInfo("userId").toString().equals(otherClient.getInfo("userId").toString())) {
                try {
                    Envelope env = new Envelope();
                    env.setId("forceLogout");
                    env.setContents("Someone in room have same name");
                    client.sendToClient(env);
                } catch (IOException ex) {
                    chatConsole.display("Failed to send forceLogout");
                    chatConsole.display(ex.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    public void notifyUserListChanged() {
        Envelope env = new Envelope();
        env.setId("userListChanged");
        sendToAllClients(env);
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * starts listening for connections.
     */
    protected void serverStarted() {
        chatConsole.display("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * stops listening for connections.
     */
    protected void serverStopped() {
        chatConsole.display("Server has stopped listening for connections.");
    }

//    //Class methods ***************************************************
//    /**
//     * This method is responsible for the creation of the server instance (there
//     * is no UI in this phase).
//     *
//     * @param args[0] The port number to listen on. Defaults to 5555 if no
//     * argument is entered.
//     */
//    public static void main(String[] args) {
//        int port = 0; //Port to listen on
//
//        try {
//            port = Integer.parseInt(args[1]);
//        } catch (ArrayIndexOutOfBoundsException oob) {
//            port = DEFAULT_PORT; //Set port to 5555
//        }
//
//        EchoServer sv = new EchoServer(port);
//
//        try {
//            sv.listen(); //Start listening for connections
//        } catch (Exception ex) {
//            chatConsole.display("ERROR - Could not listen for clients!");
//        }
//
//    }
    protected void clientConnected(ConnectionToClient client) {
        chatConsole.display("<Client Connected:" + client + ">");
        client.setInfo("room", "lobby");
        client.setInfo("userId", "guest");
        notifyUserListChanged();
    }

    @Override
    protected synchronized void clientException(ConnectionToClient client, Throwable exception) {
        chatConsole.display("Client shutdown");
        notifyUserListChanged();
    }
}
//End of EchoServer class
