package GoogleAPI;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.v3.*;

import java.io.IOException;
import java.util.List;

public class Translate {
    private CredentialsProvider credentials;
    private TranslationServiceClient client;
    private String projectId;

    public Translate (CredentialsProvider credentials) throws IOException {
        this.credentials = credentials;
        projectId = "lofty-gravity-311304";
        TranslationServiceSettings settings= TranslationServiceSettings.newBuilder().setCredentialsProvider(credentials).build();
        client = TranslationServiceClient.create(settings);
    }

    public String translateText(String text, String targetLanguage) throws IOException {
        String projectId = "lofty-gravity-311304";
        return translateText(projectId, targetLanguage, text);
    }

    public String translateText(String projectId, String targetLanguage, String text)
            throws IOException {
        System.out.println(targetLanguage);

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

    public List<SupportedLanguage> getLanguages() {
        LocationName parent = LocationName.of(projectId, "global");
        GetSupportedLanguagesRequest request =
                GetSupportedLanguagesRequest.newBuilder().setParent(parent.toString()).setDisplayLanguageCode("en").build();

        SupportedLanguages response = client.getSupportedLanguages(request);
        return response.getLanguagesList();
    }
}
