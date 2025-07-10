package dwd.log.javalogreader;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
            // 3. Request senden und Antwort erhalten
            /*HttpResponse<String> response = con.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                JOptionPane.showMessageDialog(null, "Error 404: File not Found", "ERROR 404", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException();
            }

            if (response.statusCode() == 403) {
                JOptionPane.showMessageDialog(null, "Error 403: Access Restricted", "ERROR 403", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException();
            }

            if (response.statusCode() == 500) {
                JOptionPane.showMessageDialog(null, "Error 500: Internal Error", "ERROR 500", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException();
            }

            // 4. Antwort in StringBuilder speichern
            output = new StringBuilder();
            output.append(response.body());

            return output.toString();*/

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

        /*con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int status = 0;
        try {
            status = con.getResponseCode();
        } catch (IOException ex) {
            System.getLogger(LogReader.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        if(status == 200){
         try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            output = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                output.append(inputLine).append("\n");
            }
            in.close();
            return output.toString();

        } catch (IOException ex) {
            System.getLogger(LogReader.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        }else{

            throw new IOException("Error");
        }
        con.disconnect();

        throw new IOException("Error");*/
    }


}
