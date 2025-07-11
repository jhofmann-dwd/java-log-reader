package dwd.log.javalogreader.forms;

import dwd.log.javalogreader.ConnectToFile;
import dwd.log.javalogreader.Main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
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
    private JLabel outputLabel;
    private JCheckBox noSearchCheck;

    //Local Variables
    private static final String HTTP_REQUEST_BEGIN = "http://";

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


        setTitle("Book Editor");
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

                HashMap<Boolean, String> test = new HashMap();

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
                    con = HttpClient.newHttpClient();
                    cf = new ConnectToFile(HTTP_REQUEST_BEGIN + host + pathText.getText() + fileText.getText(), con, explicitSearchText.getText(), outputText, fileText.getText(), username, password, noSearchCheck.isSelected());
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
