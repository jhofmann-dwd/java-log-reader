package dwd.log.javalogreader.forms;

import dwd.log.javalogreader.ConnectToFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class LogReader extends JFrame{
    private JPanel contentPane;
    private JTextField fileText;
    private JTextField explicitSearchText;
    private JTextField pathText;
    private JTextArea outputText;
    private JLabel explicitSearchLabel;
    private JLabel pathLabel;
    private JLabel fileLabel;
    private JButton confirmBtn;
    private JButton exitBtn;

    HttpClient con;
    ConnectToFile cf;


    public LogReader() throws MalformedURLException, URISyntaxException {

        setTitle("Book Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(contentPane);
        pack();

        // Set the frame location to the center of the screen
        setLocationRelativeTo(null);

        /*// Cancel button event listener
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelChanges();
            }
        });*/

        // Set the frame visible
        setVisible(true);
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                outputText.setText("");

                HashMap<Boolean, String> test = new HashMap();

                test.put(pathText.getText().isEmpty() || pathText.getText() == null, "Pfad");
                test.put(explicitSearchText.getText().isEmpty() || explicitSearchLabel.getText() == null , "Suchwert");
                test.put(fileText.getText().isEmpty() || fileText.getText() == null, "Dateiname");

                for(Map.Entry<Boolean, String> entry : test.entrySet())
                {
                    Boolean key = entry.getKey();
                    String value = entry.getValue();

                    if(key)
                    {
                        JOptionPane.showMessageDialog(null, "Ungültiger " + value, "FEHLER", JOptionPane.ERROR_MESSAGE);
                        throw new RuntimeException();
                    }
                }
                try {
                    con = HttpClient.newHttpClient();
                    cf = new ConnectToFile("http://perseus:8255/" + pathText.getText(), con);
                    outputText.setText(cf.outputString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);            }
        });
    }
}
