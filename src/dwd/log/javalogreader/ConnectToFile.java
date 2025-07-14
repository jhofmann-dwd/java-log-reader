package dwd.log.javalogreader;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectToFile {


    String url;
    HttpClient con;
    JTextArea result;
    boolean inputBox;
    //StringBuilder output;
    String input;
    String fileText;
    String username, password;
    String output;

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

public CompletableFuture<String> outputString() {
    try {
        HttpClient client = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenApply(response -> {
                    if (response.statusCode() == 401) {
                        throw new CompletionException(
                                new IOException("Unauthorized: Bitte überprüfen Sie Ihre Anmeldedaten"));
                    }
                    return response.body();
                }).thenApply(lines -> {
                    StringBuilder filtered = new StringBuilder();
                    AtomicInteger lineCounter = new AtomicInteger();
                    lines.forEach(line -> {
                        lineCounter.getAndIncrement();
                        if ((input != null && line != null) &&
                            (input.isEmpty() && inputBox || line.contains(input))) {
                            filtered.append("[")
                                    //.append("[FILE: ").append(fileText)
                                    //.append(" | ")
                                    .append("LINE: ")
                                    .append(lineCounter)
                                    .append("]\t")
                                   .append(line).append("\n");
                        }
                        output = filtered.toString();
                    });
                    return filtered.toString();
                })
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(null, 
                                "Error: " + ex.getMessage(), 
                                "ERROR", 
                                JOptionPane.ERROR_MESSAGE));
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            if (this.output != null && this.result.isDisplayable()) {
                                this.result.setText(output);
                            }
                        });
                    }
                });
    } catch (URISyntaxException e) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(e);
        return future;
    }
}

    public CompletableFuture<Boolean> checkAuth(String username, String password, String host) {
        HttpClient client = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + host))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    return response.statusCode() != 401;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                });
    }




}