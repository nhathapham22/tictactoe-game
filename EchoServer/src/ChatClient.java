
import java.io.*;
import java.util.ArrayList;

/**
 * This class overrides some of the methods defined in the abstract superclass
 * in order to give more functionality to the client.
 */
public class ChatClient extends AbstractClient {
    //Instance variables **********************************************

    /**
     * The interface type variable. It allows the implementation of the display
     * method in the client.
     */
    ChatIF clientUI;
    TicTacToe ticTacToe;
    String userName = "guest";

    //Constructors ****************************************************
    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @param clientUI The interface type variable.
     */
    public ChatClient(String host, int port, ChatIF clientUI)
            throws IOException {
        super(host, port); //Call the superclass constructor
        this.clientUI = clientUI;
        openConnection();
    }

    //Instance methods ************************************************
    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg) {
        if (msg instanceof Envelope) {
            Envelope env = (Envelope) msg;
            handleCommandFromServer(env);
        } else {
            clientUI.display(msg.toString());
        }
    }

    public void handleCommandFromServer(Envelope env) {
        if (env.getId().equals("who")) {
            displayUserList(env);
        }

        if (env.getId().equals("userListChanged")) {
            handleClientCommand("#who");
        }

        if (env.getId().equals("forceLogout")) {
            String reason = env.getContents().toString();
            clientUI.display("<FORCELOGOUT>");
            clientUI.display(reason);

            try {
                closeConnection();
            } catch (IOException ex) {
                clientUI.display(ex.getMessage());
            }
        }

        if (env.getId().equals("ttt")) {
            handleServerTicTacToeCommand(env);
        }
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(String message) {
        if (message.length() == 0) {
            return;
        }
        if (message.charAt(0) == '#') {
            handleClientCommand(message);
        } else {
            try {
                sendToServer(message);
            } catch (IOException e) {
                clientUI.display("Could not send message to server.  Terminating client.......");
                quit();
            }
        }
    }

    /**
     * This method terminates the client.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
        }
        System.exit(0);
    }

    public void connectionClosed() {
        clientUI.display("Connection closed");

    }

    public void displayUserList(Envelope env) {
        ArrayList<String> userList = (ArrayList<String>) env.getContents();

        String usersString = "<USERLIST>";
        for (int i = 0; i < userList.size(); i++) {
            usersString += userList.get(i);
            if (i != userList.size() - 1) {
                usersString += "&";
            }
        }

        clientUI.display(usersString);
    }

    public void handleClientCommand(Envelope envelope) {
        try {
            this.sendToServer(envelope);
        } catch (IOException ex) {
            clientUI.display("Can not send to server command + \"" + envelope.getId() + "\"");
            clientUI.display(ex.getMessage());
        }
    }

    public void handleClientCommand(String message) {

        if (message.equals("#quit")) {
            clientUI.display("Shutting Down Client");
            quit();

        }

        if (message.equals("#logoff")) {
            clientUI.display("Disconnecting from server");
            try {
                closeConnection();
            } catch (IOException e) {
            }

        }

        if (message.indexOf("#setHost") > 0) {

            if (isConnected()) {
                clientUI.display("Cannot change host while connected");
            } else {
                setHost(message.substring(8, message.length()));
            }

        }

        if (message.indexOf("#setPort") > 0) {

            if (isConnected()) {
                clientUI.display("Cannot change port while connected");
            } else {
                setPort(Integer.parseInt(message.substring(8, message.length())));
            }

        }

        if (message.indexOf("#login") == 0) {

            if (isConnected()) {
                clientUI.display("already connected");
            } else {
                try {

                    openConnection();
                    userName = message.substring(6, message.length()).trim();
                    if (userName.length() == 0) {
                        userName = "guest";
                    }

                    Envelope env = new Envelope("login", "", userName);
                    this.sendToServer(env);
                } catch (IOException e) {
                    clientUI.display("failed to connect to server.");
                }
            }
        }

        if (message.indexOf("#join") == 0) {
            try {
                String roomName = message.substring(5, message.length()).trim();
                Envelope env = new Envelope("join", "", roomName);
                this.sendToServer(env);
            } catch (IOException e) {
                clientUI.display("failed to join a room");
            }
        }

        if (message.indexOf("#pm") == 0) {
            try {
                String targetAndMessage = message.substring(3, message.length()).trim();

                String target = targetAndMessage.substring(0, targetAndMessage.indexOf(" ")).trim();
                String pm = targetAndMessage.substring(targetAndMessage.indexOf(" "), targetAndMessage.length()).trim();

                Envelope env = new Envelope("pm", target, pm);
                this.sendToServer(env);
            } catch (IOException e) {
                clientUI.display("could not private message user");
            }
        }

        if (message.indexOf("#yell") == 0) {

            try {
                String yellMessage = message.substring(5, message.length()).trim();
                String upper = yellMessage.toUpperCase();
                Envelope env = new Envelope("yell", "", upper);
                this.sendToServer(env);

            } catch (IOException e) {
                clientUI.display("failed to yell");
            }
        }

        if (message.indexOf("#who") == 0) {

            try {
                Envelope env = new Envelope("who", "", "");
                this.sendToServer(env);

            } catch (IOException e) {
                clientUI.display("failed to acquire user list");
            }
        }

        if (message.indexOf("#tttInvite") == 0) {
            String targetUser = message.substring("#tttInvite".length()).trim();
            sendTicTacToeInvite(targetUser);
        }

        if (message.equals("#tttAccept")) {
            sendTicTacToeAccept();
        }

        if (message.equals("#tttDecline")) {
            sendTicTacToeDecline();
        }

        // Just a little fun with old console client
        if (message.indexOf("#tttMove") == 0) {
            try {
                int move = Integer.parseInt(message.substring("#tttMove".length()).trim());
                sendTicTacToeMove(move - 1);
            } catch (NumberFormatException e) {
                clientUI.display("Please enter a number!");
                clientUI.display(e.getMessage());
            }

        }
    }

    public void sendTicTacToeInvite(String targetUser) {
        Envelope env = new Envelope();

        if (targetUser.equals("guest")) {
            clientUI.display("Cannot invite guest to play!");
            return;
        }

        if (userName.equals("guest")) {
            clientUI.display("You must be login to play!");
            return;
        }

        ticTacToe = new TicTacToe();
        ticTacToe.setGameState(1);

        ticTacToe.setPlayer1(userName);
        env.setId("ttt");
        env.setArg(targetUser);
        env.setContents(ticTacToe);

        sendTicTacToeEnvelop(env);
    }

    public void sendTicTacToeAccept() {
        if (ticTacToe == null || ticTacToe.getGameState() != 1) {
            return;
        }

        Envelope env = new Envelope();
        String targetUser = ticTacToe.getPlayer1();

        if (targetUser.equals(userName)) {
            clientUI.display("Can not accept game yourself!");
            return;
        }

        ticTacToe.setPlayer2(userName);
        ticTacToe.setGameState(3);
        ticTacToe.setActivePlayer(1);
        ticTacToe.setBoard(new char[3][3]);

        env.setId("ttt");
        env.setArg(targetUser);
        env.setContents(ticTacToe);
        sendTicTacToeEnvelop(env);
    }

    public void sendTicTacToeDecline() {
        if (ticTacToe == null || ticTacToe.getGameState() == 2 || ticTacToe.getGameState() == 4) {
            return;
        }

        Envelope env = new Envelope();
        String targetUser;
        if (userName.equals(ticTacToe.getPlayer1())) {
            targetUser = ticTacToe.getPlayer2();
        } else {
            targetUser = ticTacToe.getPlayer1();
        }
        ticTacToe.setGameState(2);

        env.setId("ttt");
        env.setArg(targetUser);
        env.setContents(ticTacToe);
        sendTicTacToeEnvelop(env);
    }

    public void sendTicTacToeMove(int move) {
        if (ticTacToe == null || ticTacToe.getGameState() != 3 || !isActivePlayer()) {
            return;
        }

        Envelope env = new Envelope();

        // Prevent move out of bounds
        if (move >= 9 || move < 0) {
            clientUI.display("Try another move! (1-9)");
            return;
        }

        // Prevent override value
        char board[][] = ticTacToe.getBoard();
        if ((int) board[move / 3][move % 3] != 0) {
            clientUI.display("You can not take that position! Try another.");
            return;
        }

        ticTacToe.updateBoard(move);

        if (ticTacToe.getActivePlayer() == 1) {
            ticTacToe.setActivePlayer(2);
            env.setArg(ticTacToe.getPlayer2());
        } else {
            ticTacToe.setActivePlayer(1);
            env.setArg(ticTacToe.getPlayer1());
        }

        env.setId("ttt");
        env.setContents(ticTacToe);
        sendTicTacToeEnvelop(env);
    }

    public void sendTicTacToeEnvelop(Envelope env) {
        try {
            sendToServer(env);
        } catch (IOException ex) {
            clientUI.display("Cannot send tictactoe envelope!");
            clientUI.display(ex.getMessage());
        }
    }

    public void handleServerTicTacToeCommand(Envelope env) {
        ticTacToe = (TicTacToe) env.getContents();

        if (ticTacToe.getGameState() == 1) {
            clientUI.display("You have been invited to play tic tac toe with "
                    + ticTacToe.getPlayer1()
                    + ", type #tttAccept to accept, #tttDecline to decline"
            );
        }

        if (ticTacToe.getGameState() == 2) {
            clientUI.display("Your game was declined");
            clientUI.display("<TICTACTOE>declined");
        }

        if (ticTacToe.getGameState() == 3) {
            clientUI.display("<TICTACTOE>playing");

            if (isActivePlayer()) {
                clientUI.display("Your turn to play TicTacToe");
                clientUI.display("<TICTACTOE>active");
            } else {
                clientUI.display("Wait for other player...");
                clientUI.display("<TICTACTOE>inactive");
            }

            clientUI.display("<TICTACTOE>board" + convertBoardToString(ticTacToe.getBoard()));
        }

        if (ticTacToe.getGameState() == 4) {
            // After set move, active switch
            // But if the player won, it should be the player before switch active
            // So player inactive should be winner
            if (!isActivePlayer()) {
                clientUI.display("You have won");
            } else {
                clientUI.display("You have lost");
            }

            clientUI.display("<TICTACTOE>board" + convertBoardToString(ticTacToe.getBoard()));
            clientUI.display("<TICTACTOE>inactive");
        }
    }

    public String convertBoardToString(char[][] board) {
        String boardString = "";
        for (char[] row : ticTacToe.getBoard()) {
            for (char col : row) {
                if ((int) col == 0) {
                    boardString += (TictactoeUI.DEFAULT_SYMBOL + " ");
                } else {
                    boardString += (col + " ");
                }
            }
        }
        return boardString;
    }

    public boolean isActivePlayer() {
        if (ticTacToe.getActivePlayer() == 1) {
            return ticTacToe.getPlayer1().equals(userName);
        } else if (ticTacToe.getActivePlayer() == 2) {
            return ticTacToe.getPlayer2().equals(userName);
        } else {
            return false;
        }
    }
}
//End of ChatClient class
