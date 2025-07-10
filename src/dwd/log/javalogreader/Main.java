package dwd.log.javalogreader;

import dwd.log.javalogreader.forms.LogReader;
import dwd.log.javalogreader.forms.LoginForm;

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
/*
                SwingUtilities.invokeLater(() -> {
                    LoginForm login = new LoginForm(null);
                    login.setVisible(true);

                    if (login.isSucceeded()) {

                    } else {
                        System.exit(0);
                    }
                });
*/
                LoginForm login = null;
                try {
                    login = new LoginForm();
                } catch (IOException | FontFormatException e) {
                    throw new RuntimeException(e);
                }
                login.setVisible(true);
            }
        });
    }
}
