
import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author dangh
 */
public class ServerConsole implements ChatIF {

    final public static int DEFAULT_PORT = 5555;
    EchoServer server;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        try {
            port = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            port = DEFAULT_PORT;
        }

        ServerConsole console = new ServerConsole(port);
        console.readAdminInput();
    }

    public ServerConsole(int port) {
        server = new EchoServer(port, this);
    }

    private void readAdminInput() {
        try {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (true) {
                message = fromConsole.readLine();
                server.handleCommandFromAdmin(message);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console!");
            display(ex.getMessage());
        }
    }

    /**
     * This method overrides the method in the ChatIF interface. It displays a
     * message onto the screen.
     *
     * @param message The string to be displayed.
     */
    public void display(String message) {
        System.out.println("> " + message);
    }
}
