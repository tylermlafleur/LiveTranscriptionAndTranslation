package GoogleAPI;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.v3.*;

import java.io.IOException;

public class Translate {
    // Set and pass variables to overloaded translateText() method for translation.
    public static String translateText(String text, CredentialsProvider credentials, String targetLanguage) throws IOException {
        String projectId = "lofty-gravity-311304";
        return translateText(projectId, targetLanguage, text, credentials);
    }

    public static String translateText(String projectId, String targetLanguage, String text, CredentialsProvider credentials)
            throws IOException {

        TranslationServiceSettings settings= TranslationServiceSettings.newBuilder().setCredentialsProvider(credentials).build();

        try (TranslationServiceClient client = TranslationServiceClient.create(settings)) {
            LocationName parent = LocationName.of(projectId, "global");

            // Supported Mime Types: https://cloud.google.com/translate/docs/supported-formats
            TranslateTextRequest request =
                    TranslateTextRequest.newBuilder()
                            .setParent(parent.toString())
                            .setMimeType("text/plain")
                            .setTargetLanguageCode(targetLanguage)
                            .addContents(text)
                            .build();

            TranslateTextResponse response = client.translateText(request);

            StringBuilder sb = new StringBuilder();
            for (Translation translation : response.getTranslationsList()) {
                sb.append(translation.getTranslatedText());
            }

            return sb.toString();
        }
    }
}
