package dwd.log.javalogreader;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectToFile {


    String url;
    HttpClient con;
    JTextArea result;
    boolean inputBox;
    StringBuilder output;
    String input;
    String fileText;
    String username, password;

    public ConnectToFile(String url, HttpClient con, String input, JTextArea result, String fileText, String username, String password, boolean inputBox) throws MalformedURLException, URISyntaxException {
        this.url = url;
        this.con = con;
        this.input = input;
        this.result = result;
        this.fileText = fileText;
        this.username = username;
        this.password = password;
    }

    public ConnectToFile() {
    }

    public String outputString() throws IOException {

        try {
            // 2. Request bauen (GET)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            //TODO: Enable authentication when it is needed
            con = HttpClient.newBuilder()
                    .authenticator(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password.toCharArray());
                        }
                    })
                    .build();


            if(!inputBox) {
                con.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                        .thenApply(HttpResponse::body)
                        .thenAccept(lines -> {
                            StringBuilder filtered = new StringBuilder();
                            AtomicInteger lineCounter = new AtomicInteger();
                            lines.forEach(line -> {
                                lineCounter.getAndIncrement();
                                if (input.isEmpty() || line.contains(input)) {
                                    filtered.append("[").append("FILE: ").append(fileText).append(" | ").append("LINE: ").append(lineCounter).append("]").append("\t").append(line).append("\n");
                                }
                            });
                            SwingUtilities.invokeLater(() -> result.setText(filtered.toString()));
                        })
                        .exceptionally(ex -> {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Verbindungsfehler oder ungültige Datei", "FEHLER", JOptionPane.ERROR_MESSAGE));
                            return null;
                        });
            }else {
                con.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                        .thenApply(HttpResponse::body)
                        .thenAccept(lines -> {
                            StringBuilder filtered = new StringBuilder();
                            AtomicInteger lineCounter = new AtomicInteger();
                            lines.forEach(line -> {
                                lineCounter.getAndIncrement();
                                filtered.append("[").append("FILE: ").append(fileText).append(" | ").append("LINE: ").append(lineCounter).append("]").append("\t").append(line).append("\n");
                            });
                            SwingUtilities.invokeLater(() -> result.setText(filtered.toString()));
                        })
                        .exceptionally(ex -> {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Verbindungsfehler oder ungültige Datei", "FEHLER", JOptionPane.ERROR_MESSAGE));
                            return null;
                        });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public CompletableFuture<Boolean> checkAuth(String username, String password, String host) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + host))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    return statusCode == 200; // Erfolg = true
                })
                .exceptionally(ex -> {
                    ex.printStackTrace(); // optional
                    return false; // Fehler = Login fehlgeschlagen
                });
    }


}
