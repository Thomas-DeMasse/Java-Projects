import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client
{
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;

    private ClientGUI clientGUI;

    private String server, user;
    private int port;

    Client(String server, int port, String username) {
        this(server, port, username, null);
    }

    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.user = username;
        this.clientGUI = cg;
    }

    public boolean start() {
        try {
            socket = new Socket(server, port);
        }
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }
        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try
        {
            inputStream  = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        new ListenFromServer().start();
        try
        {
            outputStream.writeObject(user);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        return true;
    }


    private void display(String msg) {
        if(clientGUI == null)
            System.out.println(msg);
        else
            clientGUI.append(msg + "\n");
    }

    void sendMessage(ChatClient msg) {
        try {
            outputStream.writeObject(msg);
        }
        catch(IOException e) {
            display("An error has occurred. " + e);
        }
    }

    private void disconnect() {
        try {
            if(inputStream != null) {
                inputStream.close();
            }
        }
        catch(Exception e) {
            display("An error has occurred. " + e.getMessage());
        }
        try {
            if(outputStream != null) {
                outputStream.close();
            }
        }
        catch(Exception e) {
            display("An error has occurred. " + e.getMessage());
        }
        try{
            if(socket != null) {
                socket.close();
            }
        }
        catch(Exception e) {
            display("An error has occurred. " + e.getMessage());
        }

        if(clientGUI != null) {
            clientGUI.connectionFailed();
        }
    }

    public static void main(String[] args) {
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";

        switch(args.length) {
            case 3:
                serverAddress = args[2];
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number. " + e.getMessage());
                    return;
                }
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Usage is: java Client " + userName + portNumber + serverAddress);
                return;
        }
        Client client = new Client(serverAddress, portNumber, userName);
        if(!client.start())
            return;
        Scanner scan = new Scanner(System.in);
        while(true) {
            System.out.print("> ");
            String msg = scan.nextLine();
            if(msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new ChatClient(ChatClient.LOGOUT, ""));
                break;
            }
            else {
                client.sendMessage(new ChatClient(ChatClient.MESSAGE, msg));
            }
        }
        client.disconnect();
    }

    class ListenFromServer extends Thread {
        public void run() {
            while(true) {
                try {
                    String msg = (String) inputStream.readObject();
                    if(clientGUI == null) {
                        System.out.println(msg);
                        System.out.print("> ");
                    }
                    else {
                        clientGUI.append(msg);
                    }
                }
                catch(IOException e) {
                    display("Server has closed the connection: " + e);
                    if(clientGUI != null)
                        clientGUI.connectionFailed();
                    break;
                }
                catch(ClassNotFoundException e) {
                    display("An error has occurred. " + e.getMessage());
                }
            }
        }
    }
}