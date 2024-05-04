import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JLabel label;
    private JTextField textField;
    private JTextField textFieldServer, textFieldPort;
    private JButton login, logout;
    private JTextArea textArea;
    private boolean connected;
    private Client client;
    private int defaultPort;
    private String defaultHost;

    ClientGUI(String host, int port) {
        super("Chat Client");
        defaultPort = port;
        defaultHost = host;

        JPanel northPanel = new JPanel(new GridLayout(3,1));
        JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));

        textFieldServer = new JTextField(host);
        textFieldPort = new JTextField("" + port);
        textFieldPort.setHorizontalAlignment(SwingConstants.RIGHT);

        serverAndPort.add(new JLabel("Server Address: "));
        serverAndPort.add(textFieldServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(textFieldPort);
        serverAndPort.add(new JLabel(""));
        northPanel.add(serverAndPort);

        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        textField = new JTextField("Anonymous");
        textField.setBackground(Color.WHITE);
        northPanel.add(textField);
        add(northPanel, BorderLayout.NORTH);

        textArea = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(textArea));
        textArea.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);		// you have to login before being able to logout

        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        textField.requestFocus();
    }
    void append(String str) {
        textArea.append(str);
        textArea.setCaretPosition(textArea.getText().length() - 1);
    }
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        label.setText("Enter your username below");
        textField.setText("Anonymous");
        // reset port number and host name as a construction time
        textFieldPort.setText("" + defaultPort);
        textFieldServer.setText(defaultHost);
        // let the user change them
        textFieldServer.setEditable(false);
        textFieldPort.setEditable(false);
        // don't react to a <CR> after the username
        textField.removeActionListener(this);
        connected = false;
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == logout) {
            client.sendMessage(new ChatClient(ChatClient.LOGOUT, ""));
            return;
        }
        if(connected) {
            client.sendMessage(new ChatClient(ChatClient.MESSAGE, textField.getText()));
            textField.setText("");
            return;
        }


        if(o == login) {
            String username = textField.getText().trim();
            // empty username ignore it
            if(username.length() == 0)
                return;
            // empty serverAddress ignore it
            String server = textFieldServer.getText().trim();
            if(server.length() == 0)
                return;
            // empty or invalid port numer, ignore it
            String portNumber = textFieldPort.getText().trim();
            if(portNumber.length() == 0)
                return;
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                return;
            }

            client = new Client(server, port, username, this);
            if(!client.start())
                return;
            textField.setText("");
            label.setText("Enter your message below");
            connected = true;

            login.setEnabled(false);
            logout.setEnabled(true);

            textFieldServer.setEditable(false);
            textFieldPort.setEditable(false);
            // Action listener for when the user enter a message
            textField.addActionListener(this);
        }

    }

    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 1500);
    }

}
