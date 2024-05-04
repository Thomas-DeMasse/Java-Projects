
//Server portion of a client/server stream-socket connection.

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {

    private static int chatId;
    private ArrayList<ChatThread> clients;
    private ServerGUI serverGUI;
    private SimpleDateFormat sdf;
    private int port;
    private boolean isContinue;
    private ServerSocket serverSocket;

    public Server(int port) {
        this(port, null);
    }

    public Server(int port, ServerGUI serverGUI) {
        this.serverGUI = serverGUI;
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        clients = new ArrayList<>();
    }
    public void start() {
        isContinue = true;
        try
        {
            serverSocket = new ServerSocket(port, 2);
            while(isContinue) {
                displayMessage("Server is waiting for Clients to connect...");
                Socket socket = serverSocket.accept();
                if(!isContinue) {
                    break;
                }
                ChatThread t = new ChatThread(socket);
                clients.add(t);
                t.start();
            }
            serverSocket.close();
            for(ChatThread t : clients) {
                try{
                   t.inputStream.close();
                   t.outputStream.close();
                   t.socket.close();
                } catch(IOException ie) {
                    displayMessage("An error has occurred " + ie.getMessage());
                }
            }
        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
            System.exit(1);
        }
    }

    protected void stop() {
        isContinue = false;
        try{
            new Socket("localhost", 5000);
        } catch (Exception e) {
            displayMessage("An error has occurred " + e.getMessage());
        }
    }

    private void displayMessage(String messageToDisplay) {
        String time = sdf.format(new Date()) + " " + messageToDisplay;
        if(serverGUI == null) {
            System.out.println(time);
        } else {
            serverGUI.appendEvent(messageToDisplay);
        }
    }

    private synchronized void broadcast(String message) {
        serverGUI.appendRoom(message);
        for(ChatThread t : clients) {
            if(!t.writeMessage(message)) {
                clients.remove(t);
                displayMessage("Disconnected Client " + t.user + " removed from list.");
            }
        }
    }

    synchronized void remove(int id) {
        for(ChatThread t : clients) {
            if(t.id == id) {
                clients.remove(t);
                return;
            }
        }
    }

    public static void main(String[] args) {
        int portNumber = 1500;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Server [portNumber]");
                return;

        }
        Server server = new Server(portNumber);
        server.start();
    }


    class ChatThread extends Thread {
        Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;

        int id;
        String user;
        ChatClient chatClient;
        String date;

        ChatThread(Socket socket) {
            id = ++chatId;
            this.socket = socket;

            try
            {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                user = (String) inputStream.readObject();

                displayMessage(user + " has joined the chat.\n");
            }
            catch (IOException | ClassNotFoundException e) {
                displayMessage("An error has occured. " + e.getMessage());
            }
        }

        public void run() {
            boolean isContinue = true;
            while (isContinue) {
                try {
                    chatClient = (ChatClient) inputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    displayMessage("An error has occured " + e.getMessage());
                    break;
                }
                String message = chatClient.getMessage();
                if (chatClient.getType() == ChatClient.MESSAGE) {
                    broadcast(user + ": " + message);
                } else if (chatClient.getType() == ChatClient.LOGOUT) {
                    displayMessage(user + " has left the chat.");
                    isContinue = false;
                }
            }
            remove(id);
            close();
        }

        private void close() {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }

            } catch (IOException ie) {
                displayMessage("An error has occured. " + ie.getMessage());
            }
        }
        private boolean writeMessage(String message) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                outputStream.writeObject(message);
            }
            catch(IOException e) {
                displayMessage("Error sending message to " + user);
                displayMessage(e.toString());
            }
            return true;
        }
    }
}