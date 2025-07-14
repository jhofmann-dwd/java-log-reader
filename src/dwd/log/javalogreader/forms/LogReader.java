package dwd.log.javalogreader.forms;

import dwd.log.javalogreader.ConnectToFile;
import dwd.log.javalogreader.Main;
import dwd.log.javalogreader.UrlBuilder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.time.LocalTime;
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
    private JLabel outputLabel;
    private JCheckBox noSearchCheck;
    private JComboBox minSecondCB;
    private JLabel minSecondLabel;
    private JLabel minMinuteLabel;
    private JLabel minHourLabel;
    private JLabel timeLabel;
    private JComboBox minHourCB;
    private JComboBox minMinuteCB;
    private JLabel maxHourLabel;
    private JLabel maxMinuteLabel;
    private JLabel maxSecondLabel;
    private JComboBox maxHourCB;
    private JComboBox maxMinuteCB;
    private JComboBox maxSecondCB;

    //Local Variables
    private static final String HTTP_REQUEST_BEGIN = "http://";
    private static final char HTTP_REQUEST_SLASH = '/';
    String pastContent = "";

    // Load font from resources
    InputStream crobotoBold = Main.class.getResourceAsStream("/dwd/log/javalogreader/resources/Roboto-Regular.ttf");
    InputStream crobotoRegular = Main.class.getResourceAsStream("/dwd/log/javalogreader/resources/Roboto-Regular.ttf");
    InputStream crobotoBigBold = Main.class.getResourceAsStream("/dwd/log/javalogreader/resources/Roboto-Regular.ttf");
    Font robotoBold = Font.createFont(Font.TRUETYPE_FONT, crobotoBold).deriveFont(18f).deriveFont(Font.BOLD); // 18pt size and bold
    Font robotoRegular = Font.createFont(Font.TRUETYPE_FONT, crobotoRegular).deriveFont(18f); // 18pt size
    Font robotoBoldBig = Font.createFont(Font.TRUETYPE_FONT, crobotoBigBold).deriveFont(22f). deriveFont(Font.BOLD);

    HttpClient con;
    ConnectToFile cf;

    public LogReader(String username, String password, String host) throws IOException, URISyntaxException, FontFormatException {

        Border border = explicitSearchText.getBorder();
        Border margin = new EmptyBorder(2,0,2,0);
        explicitSearchText.setBorder(new CompoundBorder(border, margin));
        fileText.setBorder(new CompoundBorder(border, margin));
        pathText.setBorder(new CompoundBorder(border, margin));
        explicitSearchLabel.setBorder(new EmptyBorder(0,0,0,5));
        pathLabel.setBorder(new EmptyBorder(0,100,0,5));
        fileLabel.setBorder(new EmptyBorder(0,0,0,5));
        outputLabel.setBorder(new EmptyBorder(30,0,5,0));

        //Setting up fonts
        pathLabel.setFont(robotoBold);
        pathText.setFont(robotoRegular);
        fileLabel.setFont(robotoBold);
        fileText.setFont(robotoRegular);
        explicitSearchLabel.setFont(robotoBold);
        explicitSearchText.setFont(robotoRegular);
        confirmBtn.setFont(robotoBold);
        exitBtn.setFont(robotoBold);
        outputText.setFont(robotoRegular);
        outputLabel.setFont(robotoBoldBig);
        timeLabel.setFont(robotoBoldBig);
        minHourLabel.setFont(robotoBold);
        minHourCB.setFont(robotoRegular);
        minMinuteLabel.setFont(robotoBold);
        minMinuteCB.setFont(robotoRegular);
        minSecondLabel.setFont(robotoBold);
        minSecondCB.setFont(robotoRegular);
        maxHourLabel.setFont(robotoBold);
        maxHourCB.setFont(robotoRegular);
        maxMinuteLabel.setFont(robotoBold);
        maxMinuteCB.setFont(robotoRegular);
        maxSecondLabel.setFont(robotoBold);
        maxSecondCB.setFont(robotoRegular);

        //Set minutes and seconds
        for(int i = 0; i < 60; i++)
        {
            minMinuteCB.addItem(i);
            minSecondCB.addItem(i);
            maxMinuteCB.addItem(i);
            maxSecondCB.addItem(i);
        }
        //Set Hour
        for(int i = 0; i < 24; i++)
        {
            minHourCB.addItem(i);
            maxHourCB.addItem(i);
        }

        minHourCB.setSelectedIndex(12);
        maxHourCB.setSelectedIndex(12);
        maxMinuteCB.setSelectedIndex(1);

        setTitle("Java Log Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(contentPane);
        pack();

        // Set the frame location to the center of the screen
        setLocationRelativeTo(null);

        outputText.setEditable(false);


        // Set the frame visible
        setVisible(true);

        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                outputText.setText("");



                try {
                    LocalTime minTime = LocalTime.of(minHourCB.getSelectedIndex(), minMinuteCB.getSelectedIndex(), minSecondCB.getSelectedIndex());
                    LocalTime maxTime = LocalTime.of(maxHourCB.getSelectedIndex(), maxMinuteCB.getSelectedIndex(), maxSecondCB.getSelectedIndex());

                    boolean isTrue = !minTime.isAfter(maxTime);

                    if (!isTrue) {
                        throw new RuntimeException();
                    }

                HashMap<Boolean, String> test = new HashMap<>();

                test.put(pathText.getText().isEmpty() || pathText.getText() == null, "Pfad");
                test.put(explicitSearchText.getText().isEmpty() && !noSearchCheck.isSelected() || explicitSearchLabel.getText() == null && !noSearchCheck.isSelected(), "Suchwert");
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
                    String url = UrlBuilder.buildUrl(host, pathText.getText(), fileText.getText());
                    con = HttpClient.newBuilder()
                            .authenticator(new Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(username, password.toCharArray());
                                }
                            })
                            .build();

                    cf = new ConnectToFile(url, con, explicitSearchText.getText(), outputText, fileText.getText(), username, password, noSearchCheck.isSelected(), minTime, maxTime);
                    // Handle the CompletableFuture properly
                    cf.outputString()
                            .exceptionally(ex -> {
                                SwingUtilities.invokeLater(() -> {
                                    outputText.setText("Error: " + ex.getMessage());
                                    JOptionPane.showMessageDialog(null,
                                            "Error processing request: " + ex.getMessage(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                });
                                return null;
                            });

                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid input: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (URISyntaxException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid URL format: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
                }catch (RuntimeException f)
                {
                    JOptionPane.showMessageDialog(null, "Inkorrekte Zeitangabe", "FEHLER", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);            }
        });

        noSearchCheck.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

                if(noSearchCheck.isSelected())
                {
                    pastContent = explicitSearchText.getText();
                    explicitSearchText.setEditable(false);
                    explicitSearchText.setText("");
                }
                else{
                    explicitSearchText.setEditable(true);
                    explicitSearchText.setText(pastContent);
                    pastContent = "";
                }
            }
        });
    }
}
