package Server;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class Server {
    private static final int PORT = 5000;
    private static Map<String, Integer> sessions = new HashMap<>(); // sessionId -> userid
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
        System.out.println(request);
        String[] parts = request.split("\\|", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "REGISTER":
                return registerUser(data);
            case "LOGIN":
                return loginUser(data);
            case "CREATE_FILE":
                return createFile(data);
            case "QUERY_FILES":
                return  queryAllFiles(data);
            case "UPDATE_FILE":
                return updateFile(data);
            case "QUERY_FILE":
                return querySingleFile(data);
            case "DELETE_FILE":
                return deleteFile(data);
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

            PreparedStatement stmt = connection.prepareStatement("SELECT id FROM users WHERE email = ? AND password = ?");
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer userId = rs.getInt("id"); // Fetch user_id from result set
                String sessionId = UUID.randomUUID().toString();
                sessions.put(sessionId, userId); // Store user_id instead of email
                return "SUCCESS|" + sessionId;
            } else {
                return "ERROR Invalid credentials";
            }
        } catch (SQLException e) {
            return "ERROR Database error";
        }
    }
    private static String createFile(String data) {
        String sessionId = data.split("\\|")[0];
        String fileName = data.split("\\|")[1];
        Integer userId = sessions.get(sessionId);
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO notes (user_id, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            stmt.executeUpdate();

            return "SUCCESS|File created";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR|Failed to create file";
        }
    }

    private static String queryAllFiles(String sessionId) {
        Integer userId = sessions.get(sessionId);
        System.out.println(userId+" " + sessionId);
        System.out.println("here");
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name FROM notes WHERE user_id = ?")) {
            stmt.setInt(1, (userId));

            ResultSet rs = stmt.executeQuery();

            List<String> files = new ArrayList<>();
            while (rs.next()) {
                files.add(rs.getInt("id") + ":" + rs.getString("name"));
            }
            return "SUCCESS|" + String.join(",", files);
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR|Failed to retrieve files";
        }
    }

    private static String updateFile(String data) {
        System.out.println(data);

        String[] parts = data.split("\\|", 3);

        String sessionId = parts[0];
        String fileId = parts[1];
        String encodedContent = parts[2];
        String content = new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);
        System.out.println(content);
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE notes SET content = ? WHERE id = ?")) {
            stmt.setString(1, content);
            stmt.setInt(2, Integer.parseInt(fileId));

            int rows = stmt.executeUpdate();
            return rows > 0 ? "SUCCESS|File updated" : "ERROR|File not found";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR|Failed to update file";
        }
    }

    private static String querySingleFile(String data) {
        String sessionId = data.split("\\|")[0];
        String fileId = data.split("\\|")[1];
        Integer userId = sessions.get(sessionId);

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT content FROM notes WHERE id = ? AND user_id = ?")) {
            stmt.setInt(1, Integer.parseInt(fileId));
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? "SUCCESS|" + rs.getString("content") : "ERROR|File not found";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR|Failed to retrieve file";
        }
    }

    private static String deleteFile(String data) {
        String sessionId = data.split("\\|")[0];
        String fileId = data.split("\\|")[1];
        Integer userId = sessions.get(sessionId);

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM notes WHERE id = ? AND user_id =  ?")) {
            stmt.setInt(1, Integer.parseInt(fileId));
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0 ? "SUCCESS|File deleted" : "ERROR|File not found";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR|Failed to delete file";
        }
    }
}
