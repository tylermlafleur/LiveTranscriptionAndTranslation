package GoogleAPI;// Imports the Google Cloud client library
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Google Cloud GoogleAPI.TextToSpeech API sample application. Example usage: mvn package exec:java
 * -Dexec.mainClass='com.example.texttospeech.QuickstartSample'
 */
public class TextToSpeech {

    /** Demonstrates using the Text-to-Speech API. */
    public static void TextToSpeech(String text, GoogleCredentials credentials) throws Exception {
        // Instantiates a client
        TextToSpeechSettings settings =
                TextToSpeechSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Write the response to the output file.
            try (OutputStream out = new FileOutputStream("output.mp3")) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file \"output.mp3\"");
            }
        }
    }
}