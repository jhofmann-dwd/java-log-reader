package dwd.log.javalogreader;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectToFile {


    String url;
    HttpClient con;
    StringBuilder output;
    JTextField input;
    JTextArea result;
    JTextField fileText;

    public ConnectToFile(String url, HttpClient con, JTextField input, JTextArea result, JTextField fileText) throws MalformedURLException, URISyntaxException {
        this.url = url;
        this.con = con;
        this.input = input;
        this.result = result;
        this.fileText = fileText;
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
                            return new PasswordAuthentication("<insert username>", "<insert pw>".toCharArray());
                        }
                    })
                    .build();


            con.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenApply(HttpResponse::body)
                    .thenAccept(lines -> {
                        StringBuilder filtered = new StringBuilder();
                        AtomicInteger lineCounter = new AtomicInteger();
                        lines.forEach(line -> {
                            lineCounter.getAndIncrement();
                            if (input.getText().isEmpty() || line.contains(input.getText())) {
                                filtered.append("[").append("FILE: ").append(fileText.getText()).append(" | ").append("LINE: ").append(lineCounter).append("]").append("\t").append(line).append("\n");
                            }
                        });
                        SwingUtilities.invokeLater(() -> result.setText(filtered.toString()));
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Verbindungsfehler oder ungültige Datei", "FEHLER", JOptionPane.ERROR_MESSAGE));
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
