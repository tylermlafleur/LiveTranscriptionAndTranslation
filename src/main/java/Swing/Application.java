package Swing;

import GoogleAPI.IAMAuth;
import GoogleAPI.SpeechToText;
import GoogleAPI.Translate;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.translate.v3.SupportedLanguage;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Application {
    private JPanel rootPanel;
    private JButton transcribeButton;
    private JTextArea originalTextArea;
    private JScrollPane originalScrollPane;
    private JScrollPane translatedScrollPane;
    private JTextArea translatedTextArea;
    private JComboBox originalLanguageComboBox;
    private JComboBox translatedLanguageComboBox;
    private JCheckBox saveTranscriptionCheckBox;
    private JCheckBox clearTextCheckBox;
    private JLabel statusLabel;
    private JButton changeSaveLocationButton;
    private JTextField saveLocationTextField;
    private JFileChooser fileChooser;

    private HashMap<String, String> languageMap;
    private List<String> languageList;

    private StringBuilder originalTranscription;
    private StringBuilder translatedTranscription;

    private String originalLanguage;
    private String targetLanguage;

    private List<Component> interactiveComponents;

    private CredentialsProvider credentials;
    SpeechToText stt;

    public Application() throws IOException {
        JFrame frame = new JFrame("Live Translation and Transcription");
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1024, 560);
        frame.setVisible(true);

        originalTranscription = new StringBuilder();
        translatedTranscription = new StringBuilder();

        languageMap = new HashMap<>();
        languageList = new ArrayList<>();

        fileChooser = new JFileChooser();

        // initialize interactiveComponents
        interactiveComponents = new ArrayList<>();
        interactiveComponents.add(transcribeButton);
        interactiveComponents.add(saveTranscriptionCheckBox);
        interactiveComponents.add(originalLanguageComboBox);
        interactiveComponents.add(translatedLanguageComboBox);
        interactiveComponents.add(clearTextCheckBox);
        interactiveComponents.add(changeSaveLocationButton);

        // disable interactive components until loading is complete
        for (Component component : interactiveComponents) {
            component.setEnabled(false);
        }

        // initialize Google API connection objects
        try {
            credentials = IAMAuth.authExplicit("./key.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SupportedLanguage> languages = new Translate(credentials).getLanguages();
        for (SupportedLanguage language : languages) {
            languageMap.put(language.getDisplayName(), language.getLanguageCode());
            languageList.add(language.getDisplayName());
        }

        stt = new SpeechToText(credentials);

        // setup textArea boxes
        originalTextArea.setLineWrap(true);
        originalTextArea.setEditable(false);
        DefaultCaret originalCaret = (DefaultCaret) originalTextArea.getCaret();
        originalCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        translatedTextArea.setLineWrap(true);
        translatedTextArea.setEditable(false);
        DefaultCaret translatedCaret = (DefaultCaret) translatedTextArea.getCaret();
        translatedCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // set default checkbox values
        saveTranscriptionCheckBox.setSelected(true);

        // initialize combo boxes
        originalLanguageComboBox.addItem("loading...");
        translatedLanguageComboBox.addItem("loading... ");
        originalLanguageComboBox.setModel(new DefaultComboBoxModel<String>(languageList.toArray(new String[languageList.size()])));
        translatedLanguageComboBox.setModel(new DefaultComboBoxModel<String>(languageList.toArray(new String[languageList.size()])));

        saveTranscriptionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!saveTranscriptionCheckBox.isSelected()) {
                    changeSaveLocationButton.setEnabled(false);
                } else {
                    changeSaveLocationButton.setEnabled(true);
                }
            }
        });

        originalLanguageComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                originalLanguage = languageMap.get(originalLanguageComboBox.getSelectedItem().toString());
            }
        });

        translatedLanguageComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                targetLanguage = languageMap.get(translatedLanguageComboBox.getSelectedItem().toString());
            }
        });

        // set transcribe button action
        transcribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clearTextCheckBox.isSelected() && !stt.getTranscribing()) {
                    originalTextArea.setText(originalTranscription.toString());
                    translatedTextArea.setText(translatedTranscription.toString());
                } else if (!originalTextArea.getText().toString().equals("")) {
                    originalTextArea.append("\n");
                    translatedTextArea.append("\n");
                }
                if (!stt.getTranscribing()) {
                    stt.setTranscribing(true);
                    beginTranscription();
                    transcribeButton.setText("End Transcription");
                } else {
                    stt.setTranscribing(false);
                    transcribeButton.setText("Begin Transcription");
                }
            }
        });

        changeSaveLocationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(null);
                saveLocationTextField.setText(fileChooser.getSelectedFile().getPath());
                stt.setFileLocation(fileChooser.getSelectedFile().getPath() + "\\");
            }
        });

        // set default values and enable interactiveComponents once loading is complete
        // should be done using threads, temporary fix by moving to the end of setup to maximize time for
        // getLanguages http request to complete
        originalLanguageComboBox.setSelectedItem("English");
        translatedLanguageComboBox.setSelectedItem("Spanish");
        statusLabel.setText("Start Translation");
        for (Component component : interactiveComponents) {
            component.setEnabled(true);
        }
    }

    public void beginTranscription() {
        Thread thread = new Thread(() -> {
            try {
                stt.streamingMicRecognize(saveTranscriptionCheckBox.isSelected(), originalLanguage, targetLanguage,
                        originalTextArea, translatedTextArea, statusLabel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void main(String[] args) throws IOException {
        new Application();
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
        rootPanel.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 10, 0), -1, -1));
        rootPanel.setMinimumSize(new Dimension(480, 480));
        originalScrollPane = new JScrollPane();
        originalScrollPane.setHorizontalScrollBarPolicy(31);
        rootPanel.add(originalScrollPane, new GridConstraints(2, 0, 5, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(160, 40), new Dimension(400, 300), new Dimension(400, 300), 2, false));
        originalTextArea = new JTextArea();
        Font originalTextAreaFont = this.$$$getFont$$$("MS Outlook", -1, 16, originalTextArea.getFont());
        if (originalTextAreaFont != null) originalTextArea.setFont(originalTextAreaFont);
        originalScrollPane.setViewportView(originalTextArea);
        translatedScrollPane = new JScrollPane();
        translatedScrollPane.setHorizontalScrollBarPolicy(31);
        rootPanel.add(translatedScrollPane, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 300), new Dimension(400, 300), new Dimension(400, 300), 2, false));
        translatedTextArea = new JTextArea();
        Font translatedTextAreaFont = this.$$$getFont$$$("MS Outlook", -1, 16, translatedTextArea.getFont());
        if (translatedTextAreaFont != null) translatedTextArea.setFont(translatedTextAreaFont);
        translatedScrollPane.setViewportView(translatedTextArea);
        transcribeButton = new JButton();
        transcribeButton.setText("Begin Translation");
        rootPanel.add(transcribeButton, new GridConstraints(3, 2, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(200, 30), new Dimension(200, 30), new Dimension(200, 30), 0, false));
        originalLanguageComboBox = new JComboBox();
        rootPanel.add(originalLanguageComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(160, 26), new Dimension(160, 26), new Dimension(160, 26), 2, false));
        translatedLanguageComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        translatedLanguageComboBox.setModel(defaultComboBoxModel1);
        rootPanel.add(translatedLanguageComboBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(160, 26), new Dimension(160, 26), new Dimension(160, 26), 2, false));
        saveTranscriptionCheckBox = new JCheckBox();
        saveTranscriptionCheckBox.setText("Save Transcription");
        rootPanel.add(saveTranscriptionCheckBox, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 4, false));
        statusLabel = new JLabel();
        Font statusLabelFont = this.$$$getFont$$$(null, Font.BOLD, 16, statusLabel.getFont());
        if (statusLabelFont != null) statusLabel.setFont(statusLabelFont);
        statusLabel.setText("Loading...");
        rootPanel.add(statusLabel, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Original Language");
        rootPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 2, false));
        final JLabel label2 = new JLabel();
        label2.setText("Translated Language");
        label2.setVerticalAlignment(0);
        rootPanel.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 2, false));
        clearTextCheckBox = new JCheckBox();
        clearTextCheckBox.setText("Clear text on new translation");
        rootPanel.add(clearTextCheckBox, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 4, false));
        changeSaveLocationButton = new JButton();
        changeSaveLocationButton.setText("Change Save Location");
        rootPanel.add(changeSaveLocationButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 4, false));
        saveLocationTextField = new JTextField();
        saveLocationTextField.setEditable(false);
        rootPanel.add(saveLocationTextField, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
