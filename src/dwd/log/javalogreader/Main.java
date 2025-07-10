package dwd.log.javalogreader;

import dwd.log.javalogreader.forms.LogReader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) {
        // Launch the book editor form
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LogReader logReader = null;
                try {
                    logReader = new LogReader();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (FontFormatException e) {
                    throw new RuntimeException(e);
                }
                logReader.setVisible(true);
            }
        });
    }
}
