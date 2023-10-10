
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nhath
 */
public class TictactoeUI extends JFrame implements WindowListener {
    
    static final String DEFAULT_SYMBOL = "-";
    
    //declaration
    ChatClient chatClient;
    ArrayList<JButton> buttons;
    
    
    // This class represents the user interface for a Tic Tac Toe game
    public TictactoeUI() {
        super("Tic Tac Toe"); //Set the title of the window
        setSize(400, 400); // Set the size of the window
        setLayout(new GridLayout(3, 3)); // set the layout to a 3x3 grid
        
        // Add a window listener to the frame
        addWindowListener(this);
        
        
        // Create an ArrayList to store the buttons
        buttons = new ArrayList<>();
        // create buttons and add them to the frame
        for (int i = 0; i < 9; i++) {
            JButton button = new JButton(DEFAULT_SYMBOL); // Create a button with default symbol
            button.setFont(new Font("Arial", Font.PLAIN, 30)); // Set the font size of the button
            buttons.add(button); // Add the button to the ArrayList
            add(button); // Add the button to the frame
            
            // Attach an ActionListener to the button using a custom class called ClickOnCell
            ClickOnCell clickOnCell = new ClickOnCell(button, i);
            button.addActionListener(clickOnCell);
        }
    }
    // Sets the chat client for the Tic Tac Toe game
    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    // Enables or disables all buttons in the game
    public void setActive(boolean isActive) {
        for (JButton button : buttons) {
            button.setEnabled(isActive);
        }
    }
    // Sets the symbol for a specific button in the game
    public void setButtonSymbol(int cell, String symbol) {
        buttons.get(cell).setText(symbol);
    }
    // Handles button clicks in the game
    class ClickOnCell implements ActionListener {
        
        int cell;
        JButton button;
        // Constructor that accepts a button and its cell number
        public ClickOnCell(JButton button, int cell) {
            this.cell = cell;
            this.button = button;
        }
        // Handles the button click event
        @Override
        public void actionPerformed(ActionEvent e) {
            // Prevent clicking on button that has symbol
            if (!button.getText().equals(DEFAULT_SYMBOL)) {
                return;
            }
            // Sends a Tic Tac Toe move to the chat client
            chatClient.sendTicTacToeMove(cell);
        }
    }
    // Responds to the window closing event
    @Override
    public void windowClosing(WindowEvent e) {
        // Decline game if window closing
        chatClient.sendTicTacToeDecline();
    }
    // Empty event handlers for other window events
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
