package Server;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static Map<String, String> sessions = new HashMap<>(); // sessionId -> userid
    private static Connection connection;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/syncnotes", "arctan", "arcpg17");
            System.out.println("Database connected.");
            initDb();


            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initDb() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, email VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS notes (id SERIAL PRIMARY KEY, user_id INT REFERENCES users(id), name VARCHAR(255) NOT NULL, content TEXT DEFAULT '', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                String request = reader.readLine();
                String response = handleRequest(request);
                writer.println(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String handleRequest(String request) {
        String[] parts = request.split("\\|", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "REGISTER":
                return registerUser(data);
            case "LOGIN":
                return loginUser(data);
            default:
                return "ERROR Unknown command";
        }
    }

    private static String registerUser(String data) {
        try {
            String[] credentials = data.split("\\|");
            if (credentials.length < 3) return "ERROR Invalid format";

            String username = credentials[0];
            String email = credentials[1];
            String password = credentials[2];

            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (name,email, password) VALUES (?,?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            return "SUCCESS User registered";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR User already exists";
        }
    }

    private static String loginUser(String data) {
        try {
            String[] credentials = data.split("\\|");
            if (credentials.length < 2) return "ERROR Invalid format";

            String email = credentials[0];
            String password = credentials[1];

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?");
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String sessionId = UUID.randomUUID().toString();
                sessions.put(sessionId, email);
                return "SUCCESS|" + sessionId;
            } else {
                return "ERROR Invalid credentials";
            }
        } catch (SQLException e) {
            return "ERROR Database error";
        }
    }
}
