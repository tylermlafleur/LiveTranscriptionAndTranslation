import com.google.api.gax.core.CredentialsProvider;

public class Main {
    public static void main(String[] args) throws Exception {
        final int timeToRun = 15000;
        // Supported Languages: https://cloud.google.com/translate/docs/languages
        final String targetLanguage = "es";

        CredentialsProvider credentials = IAMAuth.authExplicit(".\\res\\key2.json");

        Thread thread = new Thread(() -> {
            try {
                SpeechToText.streamingMicRecognize(true, credentials, timeToRun, targetLanguage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
