package dwd.log.javalogreader.forms;

import dwd.log.javalogreader.ConnectToFile;
import dwd.log.javalogreader.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class LoginForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField userText;
    private JPasswordField passText;
    private JLabel passLabel;
    private JLabel userLabel;
    private JTextField hostText;
    private JLabel hostLabel;

    ConnectToFile cf;

    // Load font from resources
    InputStream crobotoBold = Main.class.getResourceAsStream("/dwd/log/javalogreader/resources/Roboto-Regular.ttf");
    InputStream crobotoRegular = Main.class.getResourceAsStream("/dwd/log/javalogreader/resources/Roboto-Regular.ttf");
    InputStream crobotoBigBold = Main.class.getResourceAsStream("/dwd/log/javalogreader/resources/Roboto-Regular.ttf");
    Font robotoBold = Font.createFont(Font.TRUETYPE_FONT, crobotoBold).deriveFont(14f).deriveFont(Font.BOLD); // 18pt size and bold
    Font robotoRegular = Font.createFont(Font.TRUETYPE_FONT, crobotoRegular).deriveFont(14f); // 18pt size

    public LoginForm() throws IOException, FontFormatException {
        setTitle("DWD LogReader Login");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.setPreferredSize(new Dimension(295, 215));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        buttonOK.setFont(robotoBold);
        buttonCancel.setFont(robotoBold);
        userLabel.setFont(robotoBold);
        userText.setFont(robotoRegular);
        passLabel.setFont(robotoBold);
        passText.setFont(robotoRegular);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                } catch (FontFormatException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() throws IOException, URISyntaxException, FontFormatException {
        /*AtomicReference<LogReader> logReader = new AtomicReference<>();

        cf = new ConnectToFile();

        cf.checkAuth(userText.getText(), Arrays.toString(passText.getPassword()), hostText.getText()).thenAccept(success -> {
            if(success) {
                try {
                     logReader.set(new LogReader(userText.getText(), Arrays.toString(passText.getPassword()), hostText.getText()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                } catch (FontFormatException e) {
                    throw new RuntimeException(e);
                }
                JOptionPane.showMessageDialog(null, "Login erfolgreich!");
                logReader.get().setVisible(true);
                dispose();
            }
            else {
                JOptionPane.showMessageDialog(null, "Login fehlgeschlagen", "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });*/

        LogReader lr = new LogReader(userText.getText(), Arrays.toString(passText.getPassword()), hostText.getText());
        lr.setVisible(true);
        dispose();

    }

    private void onCancel() {
        System.exit(1);
        dispose();
    }

    /*public static void main(String[] args) {
        LoginForm dialog = new LoginForm();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }*/
}
