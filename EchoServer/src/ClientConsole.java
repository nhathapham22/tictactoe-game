
import java.io.*;

/**
 * This class constructs the UI for a chat client. It implements the chat
 * interface in order to activate the display() method.
 */
public class ClientConsole implements ChatIF {
    //Class variables *************************************************

    /**
     * The default port to connect on.
     */
    final public static int DEFAULT_PORT = 5555;

    //Instance variables **********************************************
    /**
     * The instance of the client that created this ConsoleChat.
     */
    ChatClient client;

    /**
     * This method is responsible for the creation of the Client UI.
     *
     * @param args[0] The host to connect to.
     */
    public static void main(String[] args) {
        String host = "";
        int port = 0;  //The port number

        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            host = "localhost";
            port = DEFAULT_PORT;
        }
        ClientConsole chat = new ClientConsole(host, DEFAULT_PORT);
        chat.accept();  //Wait for console data
    }
    //Constructors ****************************************************

    /**
     * Constructs an instance of the ClientConsole UI.
     *
     * @param host The host to connect to.
     * @param port The port to connect on.
     */
    public ClientConsole(String host, int port) {
        try {
            client = new ChatClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection!!!!"
                    + " Terminating client.");
            System.exit(1);
        }
    }

    //Instance methods ************************************************
    /**
     * This method waits for input from the console. Once it is received, it
     * sends it to the client's message handler.
     */
    public void accept() {
        try {
            BufferedReader fromConsole
                    = new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (true) {
                message = fromConsole.readLine();
                client.handleMessageFromClientUI(message);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console!");
        }
    }

    /**
     * This method overrides the method in the ChatIF interface. It displays a
     * message onto the screen.
     *
     * @param message The string to be displayed.
     */
    public void display(String message) {
        // Let console play TicTacToe too
        if (message.indexOf("<TICTACTOE>board") == 0) {
            String boardString = message.substring("<TICTACTOE>board".length());
            String[] board = boardString.split(" ");
            for (int i = 0; i < 9; i++) {
                System.out.print(board[i] + " ");
                if (i % 3 == 2) { // After third column
                    System.out.print("\n");
                }
            }
            return;
        }

        if (message.indexOf("<TICTACTOE>active") == 0) {
            System.out.println("> Type #tttMove <yourmove 1-9>");
            return;
        }

        // Properly show user list
        if (message.indexOf("<USERLIST>") == 0) {
            String usersString = message.substring("<USERLIST>".length());
            String[] users = usersString.split("&");

            System.out.println("User in room:\n(You)");
            for (String user : users) {
                if (user.length() == 0) {
                    continue;
                }

                System.out.println(user);
            }
            return;
        }

        if (message.indexOf("<TICTACTOE>") == 0) { // Remove spamming...
            return;
        }

        System.out.println("> " + message);
    }

    //Class methods ***************************************************
}
//End of ConsoleChat class
