package version2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class JsonPlaceholderAPI {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public static void main(String[] args) throws IOException {
        // Examples of using the methods
        String newUser = "{\"name\": \"John Doe\", \"username\": \"johndoe\", \"email\": \"john@example.com\"}";
        System.out.println(createUser(newUser));

        String updatedUser = "{\"id\": 1, \"name\": \"John Smith\", \"username\": \"johnsmith\", \"email\": \"johnsmith@example.com\"}";
        System.out.println(updateUser(1, updatedUser));

        System.out.println(deleteUser(1));

        System.out.println(getAllUsers());

        System.out.println(getUserById(1));

        System.out.println(getUserByUsername("Bret"));

        // Get and save comments of the last post of a user
        getAndSaveCommentsOfLastPost(1);
    }

    public static String createUser(String userJson) throws IOException {
        URL url = new URL(BASE_URL + "/users");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = userJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return getResponse(conn);
    }

    public static String updateUser(int id, String userJson) throws IOException {
        URL url = new URL(BASE_URL + "/users" + "/" + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = userJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return getResponse(conn);
    }

    public static String deleteUser(int id) throws IOException {
        URL url = new URL(BASE_URL + "/users" + "/" + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        int responseCode = conn.getResponseCode();

        return responseCode >= 200 && responseCode < 300 ? "User deleted successfully" : "Failed to delete user";
    }

    public static String getAllUsers() throws IOException {
        URL url = new URL(BASE_URL + "/users");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return getResponse(conn);
    }

    public static String getUserById(int id) throws IOException {
        URL url = new URL(BASE_URL + "/users" + "/" + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return getResponse(conn);
    }

    public static String getUserByUsername(String username) throws IOException {
        URL url = new URL(BASE_URL + "/users" + "?username=" + username);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return getResponse(conn);
    }

    public static void getAndSaveCommentsOfLastPost(int userId) throws IOException {
        // Get user's posts
        URL postsUrl = new URL(BASE_URL + "/users/" + userId + "/posts");
        HttpURLConnection postsConn = (HttpURLConnection) postsUrl.openConnection();
        postsConn.setRequestMethod("GET");
        postsConn.setRequestProperty("Accept", "application/json");
        String postsResponse = getResponse(postsConn);

// Extract the last post ID
        int lastPostId = -1;
        String[] posts = postsResponse.split("\\{");
        for (String post : posts) {
            if (post.contains("\"id\":")) {
                String[] fields = post.split(",");
                for (String field : fields) {
                    if (field.contains("\"id\":")) {
                        int id = Integer.parseInt(field.split(":")[1].trim());
                        if (id > lastPostId) {
                            lastPostId = id;
                        }
                    }
                }
            }
        }

        if (lastPostId == -1) {
            System.out.println("No posts found for user with ID " + userId);
            return;
        }

        // Get comments of the last post
        URL commentsUrl = new URL(BASE_URL + "/posts/" + lastPostId + "/comments");
        HttpURLConnection commentsConn = (HttpURLConnection) commentsUrl.openConnection();
        commentsConn.setRequestMethod("GET");
        commentsConn.setRequestProperty("Accept", "application/json");
        String commentsResponse = getResponse(commentsConn);

        // Save comments to file
        String filename = "user-" + userId + "-post-" + lastPostId + "-comments.json";
        try (FileWriter file = new FileWriter(filename)) {
            file.write(commentsResponse);
            System.out.println("Comments saved to " + filename);
        }
    }

    private static String getResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            return "Failed: HTTP error code : " + responseCode;
        }
    }
}
