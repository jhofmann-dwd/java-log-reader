package dwd.log.javalogreader;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectToFile {


    String url;
    HttpClient con;
    LocalTime minTime;
    LocalTime maxTime;
    JTextArea result;
    boolean inputBox;
    //StringBuilder output;
    String input;
    String fileText;
    String username, password;
    String output = "";

    public ConnectToFile(String url, HttpClient con, String input, JTextArea result, String fileText, String username, String password, boolean inputBox, LocalTime minTime, LocalTime maxTime) throws MalformedURLException, URISyntaxException {
        this.url = url;
        this.con = con;
        this.input = input;
        this.result = result;
        this.fileText = fileText;
        this.username = username;
        this.password = password;
        this.minTime = minTime;
        this.maxTime = maxTime;
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

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 401) {
                            throw new CompletionException(
                                    new IOException("Unauthorized: Bitte überprüfen Sie Ihre Anmeldedaten"));
                        }

                        try {
                            // Save response body to file
                            String fileName = url.substring(url.lastIndexOf('/') + 1);
                            Path filePath = Path.of(fileName);
                            Files.writeString(filePath, response.body());

                            // Schedule file deletion after 3 hours
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        Files.deleteIfExists(filePath);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 3 * 60 * 60 * 1000); // 3 hours in milliseconds

                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }

                        ArrayList<String> inputList = new ArrayList<>();

                        // Process the response as before
                        //StringBuilder filtered = new StringBuilder();
                        AtomicInteger lineCounter = new AtomicInteger();
                        response.body().lines().forEach(line -> {
                            lineCounter.getAndIncrement();
                            long time = Integer.parseInt(line.substring(0, 8), 16) * 1000L;
                            LocalDateTime date = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(time), java.time.ZoneId.systemDefault());
                            if(date.isAfter(minTime.atDate(date.toLocalDate())) && date.isBefore(maxTime.atDate(date.toLocalDate())))
                            {
                            if ((input != null && line != null) &&
                                    (input.isEmpty() && inputBox || line.contains(input))) {
                                /*filtered.append("[")
                                        .append("LINE: ")
                                        .append(lineCounter)
                                        .append("]\t")
                                        .append(line).append("\n");*/
                                inputList.add(String.format("[LINE: %s]\t%s\n", lineCounter, line));
                            }}
                            //output = filtered.toString();
                        });
                        inputList.forEach(s -> {
                            output += s + "\n";
                        });
                        return output;
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