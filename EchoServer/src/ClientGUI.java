/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Huy
 */
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
// ClientGUI class extends JFrame and implements ChatIF and WindowListener
public class ClientGUI extends JFrame implements ChatIF, WindowListener {
    
    // Define constants for the default host and port number
    final public static String DEFAULT_HOST = "localhost";
    final public static int DEFAULT_PORT = 5555;
    
    // Define GUI components
    private final JButton closeB = new JButton("Close");
    private final JButton openB = new JButton("Open");
    private final JButton sendB = new JButton("Send");
    private final JButton quitB = new JButton("Quit");
    private final JButton loginB = new JButton("Login");
    private final JButton roomB = new JButton("Change Room");
    private final JButton ticTacToeB = new JButton("Tic Tac Toe");
    private final JComboBox userListComboBox = new JComboBox();

    private final JTextField portTxF = new JTextField("");
    private final JTextField hostTxF = new JTextField("");
    private final JTextField messageTxF = new JTextField("");
    private final JTextField loginTxF = new JTextField("");
    private final JTextField roomTxF = new JTextField("");
    private final JLabel userListLB = new JLabel("User list: ", JLabel.RIGHT);
    private final JLabel portLB = new JLabel("Port: ", JLabel.RIGHT);
    private final JLabel hostLB = new JLabel("Host: ", JLabel.RIGHT);
    private final JLabel messageLB = new JLabel("Message: ", JLabel.RIGHT);
    private final JTextArea messageList = new JTextArea();
    
    //declare Tic Tac Toe game UI and a chat client
    private TictactoeUI ticTacToeUI;
    private ChatClient chatClient;
    //declare server host and port number
    static String host;
    static int port;
    //main method that initializes the ClientGUI object and runs the application
    public static void main(String[] args) {
        try { //get the server host and port number from the command line arguments
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            host = DEFAULT_HOST;
            port = DEFAULT_PORT;
        }
        // Create a new instance of the ClientGUI class
        new ClientGUI();
    }

    public ClientGUI() {
        super("Simple Chat GUI");
        setSize(300, 400);
        setLayout(new BorderLayout(5, 5));
        addWindowListener(this);

        JScrollPane messageListScroll = new JScrollPane(
                messageList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        add("Center", messageListScroll);

        JPanel bottom = new JPanel();
        add("South", bottom);
        bottom.setLayout(new GridLayout(9, 2));
        bottom.add(hostLB);
        bottom.add(hostTxF);
        bottom.add(portLB);
        bottom.add(portTxF);
        bottom.add(messageLB);
        bottom.add(messageTxF);
        bottom.add(userListLB);
        bottom.add(userListComboBox);
        bottom.add(openB);
        bottom.add(sendB);
        bottom.add(ticTacToeB); //added tictactoe button
        bottom.add(closeB);
        bottom.add(loginB);
        bottom.add(loginTxF);
        bottom.add(roomB);
        bottom.add(roomTxF);
        bottom.add(quitB);

        hostTxF.setText(host);
        portTxF.setText(port + "");

        getRootPane().setDefaultButton(sendB);
        setVisible(true);

        ticTacToeUI = new TictactoeUI();

        messageList.setLineWrap(true);
        messageList.setWrapStyleWord(true);
        messageList.setEditable(false);
        messageList.setBackground(Color.WHITE);
        setButtonsBaseOnConnectionStatus(false);

        TicTacToeAction ticTacToeAction = new TicTacToeAction();
        ticTacToeB.addActionListener(ticTacToeAction);

        SendButtonAction sendButtonAction = new SendButtonAction();
        sendB.addActionListener(sendButtonAction);

        LoginAction loginAction = new LoginAction();
        loginB.addActionListener(loginAction);

        RoomAction roomAction = new RoomAction();
        roomB.addActionListener(roomAction);

        OpenConnectionAction openButtonAction = new OpenConnectionAction();
        openB.addActionListener(openButtonAction);

        CloseConnectionAction closeConnectionAction = new CloseConnectionAction();
        closeB.addActionListener(closeConnectionAction);

        QuitAction quitAction = new QuitAction();
        quitB.addActionListener(quitAction);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (chatClient != null && chatClient.isConnected()) {
            try {
                setButtonsBaseOnConnectionStatus(false);
                chatClient.closeConnection();
            } catch (IOException ex) {
            }
        }
        System.exit(0);
    }

    @Override
    public void display(String message) {
        if (message.indexOf("<TICTACTOE>") == 0) {
            String command = message.substring("<TICTACTOE>".length());
            handleTicTacToeGUICommand(command);
        } else if (message.equals("<FORCELOGOUT>")) {
            setButtonsBaseOnConnectionStatus(false);
        } else if (message.indexOf("<USERLIST>") == 0) {
            String usersString = message.substring("<USERLIST>".length());
            String[] users = usersString.split("&");

            userListComboBox.removeAllItems();

            for (String user : users) {
                if (user.length() == 0) {
                    continue;
                }

                userListComboBox.addItem(user.replaceAll("&amp;", "&"));
            }
        } else {
            messageList.insert("> " + message + "\n", 0);
        }
    }

    private void handleTicTacToeGUICommand(String command) {
        if ("playing".equals(command)) {
            ticTacToeUI.setVisible(true);
        }

        if ("declined".equals(command)) {
            ticTacToeUI.setVisible(false);
        }

        if ("active".equals(command)) {
            ticTacToeUI.setActive(true);
        }

        if ("inactive".equals(command)) {
            ticTacToeUI.setActive(false);
        }

        if (command.indexOf("board") == 0) {
            String boardString = command.substring("board".length());
            String[] board = boardString.split(" ");
            for (int i = 0; i < 9; i++) {
                ticTacToeUI.setButtonSymbol(i, board[i]);
            }
        }
    }

    private void setButtonsBaseOnConnectionStatus(boolean isConnected) {
        hostTxF.setEditable(!isConnected);
        portTxF.setEditable(!isConnected);
        messageTxF.setEditable(isConnected);
        userListComboBox.setEnabled(isConnected);
        openB.setEnabled(!isConnected);
        sendB.setEnabled(isConnected);
        ticTacToeB.setEnabled(isConnected);
        closeB.setEnabled(isConnected);
        loginTxF.setEditable(!isConnected && chatClient != null);
        loginB.setEnabled(!isConnected && chatClient != null);
        roomB.setEnabled(isConnected);
        roomTxF.setEnabled(isConnected);

        // For tic tac toe, close the board (if showing) whenever connection is closed
        if (!isConnected) {
            // Just send decline for sure
            // If no game is present, logic handled in sendTicTacToeDecline
            if (chatClient != null) {
                chatClient.sendTicTacToeDecline();
            }
            ticTacToeUI.setVisible(false);
        }
    }

    class TicTacToeAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String targetUser = (String) userListComboBox.getSelectedItem();

            if ("guest".equals(targetUser)) {
                JOptionPane.showMessageDialog(new JFrame(), "You can not invite guest to play");
                return;
            }

            if ("guest".equals(chatClient.userName)) {
                JOptionPane.showMessageDialog(
                        new JFrame(),
                        "You must login with name first!\nTry close connection and login with name again"
                );
                return;
            }

            chatClient.sendTicTacToeInvite(targetUser);
        }
    }

    class SendButtonAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (chatClient != null && chatClient.isConnected()) {
                chatClient.handleMessageFromClientUI(messageTxF.getText().trim());
            } else {
                display("<SYSTEM>Does not connect to server");
            }
        }
    }

    class LoginAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            chatClient.handleMessageFromClientUI("#login " + loginTxF.getText().trim());
            setButtonsBaseOnConnectionStatus(true);
        }
    }

    class OpenConnectionAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (chatClient == null) {
                try {
                    host = hostTxF.getText().trim();
                    port = Integer.parseInt(portTxF.getText().trim());
                    chatClient = new ChatClient(host, port, ClientGUI.this);
                    ticTacToeUI.setChatClient(chatClient);
                    setButtonsBaseOnConnectionStatus(true);
                } catch (IOException ioe) {
                    display("Error: Can't setup connection!!!!"
                            + " Terminating client.");
                    display(ioe.getMessage());
                }
            } else if (!chatClient.isConnected()) {
                chatClient.handleMessageFromClientUI("#login");
                setButtonsBaseOnConnectionStatus(true);
            }
        }
    }

    class CloseConnectionAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (chatClient != null && chatClient.isConnected()) {
                try {
                    setButtonsBaseOnConnectionStatus(false);
                    chatClient.closeConnection();
                } catch (IOException ex) {
                    display(ex.getMessage());
                }
            }
        }
    }

    class QuitAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (chatClient != null && chatClient.isConnected()) {
                try {
                    setButtonsBaseOnConnectionStatus(false);
                    chatClient.closeConnection();
                } catch (IOException ex) {
                }
            }
            System.exit(0);
        }
    }

    class RoomAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String room = roomTxF.getText().trim();
            if (room.length() == 0) {
                room = "lobby";
            }

            // Close tic tac toe game if trying to change room
            // Just send decline for sure
            // If no game is present, logic handled in sendTicTacToeDecline
            chatClient.sendTicTacToeDecline();
            ticTacToeUI.setVisible(false);
            chatClient.handleMessageFromClientUI("#join " + room);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
