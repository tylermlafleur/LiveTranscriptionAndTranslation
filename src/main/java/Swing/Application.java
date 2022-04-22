package Swing;

import GoogleAPI.IAMAuth;
import GoogleAPI.SpeechToText;
import com.google.api.gax.core.CredentialsProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Application {
    private JPanel rootPanel;
    private JTextPane originalTextPane;
    private JButton transcribeButton;
    private JTextArea originalTextArea;
    private JScrollPane originalScrollPane;
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
                if(!stt.getTranscribing()) {
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

    public void beginTranscription(){
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
}
