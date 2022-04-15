import com.google.api.gax.core.CredentialsProvider;

public class Client {
    public static void main(String[] args) throws Exception {
        CredentialsProvider credentials = IAMAuth.authExplicit(".\\res\\key2.json");

        Thread thread = new Thread(() -> {
            try {
                SpeechToText.streamingMicRecognize(true, credentials);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        System.out.println("test");
        System.out.println(Translate.translateText("this is a test", credentials));
    }
}
