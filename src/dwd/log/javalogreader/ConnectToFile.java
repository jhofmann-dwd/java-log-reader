package dwd.log.javalogreader;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConnectToFile {


    String url;
    HttpClient con;
    StringBuilder output;

    public ConnectToFile(String url, HttpClient con) throws MalformedURLException, URISyntaxException {
        this.url = url;
        this.con = con;
    }

    public String outputString() throws IOException {

        try {
            // 2. Request bauen (GET)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            // 3. Request senden und Antwort erhalten
            HttpResponse<String> response = con.send(request, HttpResponse.BodyHandlers.ofString());

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

            return output.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new IOException("Error");

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
