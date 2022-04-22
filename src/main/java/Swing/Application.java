package Swing;

import GoogleAPI.IAMAuth;
import GoogleAPI.SpeechToText;
import com.google.api.gax.core.CredentialsProvider;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Application {
    private JPanel rootPanel;
    private JTextPane originalTextPane;
    private JButton transcribeButton;
    private JTextArea originalTextArea = new JTextArea();
    private JScrollPane originalScrollPane = new JScrollPane();
    private JScrollPane translatedScrollPane;
    private JTextArea translatedTextArea;

    private StringBuilder originalTranscription;
    private StringBuilder translatedTranscription;

    private CredentialsProvider credentials;
    SpeechToText stt;

    public Application() throws IOException {
        originalTranscription = new StringBuilder();
        translatedTranscription = new StringBuilder();

        stt = new SpeechToText();
        credentials = IAMAuth.authExplicit(".\\res\\key.json");

        originalTextArea.setLineWrap(true);
        originalTextArea.setEditable(false);
        translatedTextArea.setLineWrap(true);
        translatedTextArea.setEditable(false);

        transcribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(stt.getTranscribing());
                if (!stt.getTranscribing()) {
                    stt.setTranscribing(true);
                    beginTranscription();
                    transcribeButton.setText("End Transcription");
                } else {
                    stt.setTranscribing(false);
                    transcribeButton.setText("Begin Transcription");
                }
                originalTextArea.setText(originalTranscription.toString());
                translatedTextArea.setText(translatedTranscription.toString());
            }
        });
    }

    public void beginTranscription() {
        // Supported Languages: https://cloud.google.com/translate/docs/languages
        final String targetLanguage = "es";

        Thread thread = new Thread(() -> {
            try {
                stt.streamingMicRecognize(true, credentials, targetLanguage, originalTranscription, translatedTranscription);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Application");
        frame.setContentPane(new Application().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1024, 560);
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setMinimumSize(new Dimension(480, 480));
        originalScrollPane = new JScrollPane();
        originalScrollPane.setHorizontalScrollBarPolicy(31);
        rootPanel.add(originalScrollPane, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 300), new Dimension(400, 300), new Dimension(400, 300), 0, false));
        originalTextArea = new JTextArea();
        originalScrollPane.setViewportView(originalTextArea);
        translatedScrollPane = new JScrollPane();
        translatedScrollPane.setHorizontalScrollBarPolicy(31);
        rootPanel.add(translatedScrollPane, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 300), new Dimension(400, 300), new Dimension(400, 300), 0, false));
        translatedTextArea = new JTextArea();
        translatedScrollPane.setViewportView(translatedTextArea);
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        transcribeButton = new JButton();
        transcribeButton.setText("Begin Transcription");
        rootPanel.add(transcribeButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(200, 30), new Dimension(200, 30), new Dimension(200, 30), 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}