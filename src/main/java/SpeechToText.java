import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import java.io.IOException;
import java.util.ArrayList;

public class SpeechToText {
    public static  void streamingMicRecognize(boolean recordTranscription, CredentialsProvider credentials, int timeToRun, String targetLanguage) throws Exception {

        SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentials).build();

        ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        try (SpeechClient client = SpeechClient.create(settings)) {

            responseObserver =
                    new ResponseObserver<StreamingRecognizeResponse>() {
                        ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                        public void onStart(StreamController controller) {}

                        public void onResponse(StreamingRecognizeResponse response) {
                            String res = response.getResultsList().get(0).getAlternativesList().get(0).getTranscript().trim() + ".";
                            System.out.println("Original: " + res);
                            try {
                                System.out.println("Translation: " + Translate.translateText(res, credentials, targetLanguage));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            responses.add(response);
                        }

                        public void onComplete() {
                            StringBuilder originalText = new StringBuilder();
                            StringBuilder translatedText = new StringBuilder();

                            try {
                                for (StreamingRecognizeResponse response : responses) {
                                    StreamingRecognitionResult result = response.getResultsList().get(0);
                                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);

                                    translatedText.append(Translate.translateText(alternative.getTranscript(), credentials, targetLanguage).trim() + "\n");
                                    originalText.append(alternative.getTranscript().trim() + "\n");
                                }
                                if (recordTranscription) {
                                    FileOutput.writeToFile("originalTranscription.txt",
                                            "Begin new transcription:\n" + originalText.toString() + "End transcription.\n\n");
                                    FileOutput.writeToFile("translatedTranscription.txt",
                                            "Begin new transcription:\n" + translatedText.toString() + "End transcription.\n\n");
                                }
                                System.out.println("\nOriginal Transcription:\n" + originalText.toString());
                                System.out.println("Translated Transcription:\n" + translatedText.toString());

                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        public void onError(Throwable t) {
                            System.out.println(t);
                        }
                    };

            ClientStream<StreamingRecognizeRequest> clientStream =
                    client.streamingRecognizeCallable().splitCall(responseObserver);

            RecognitionConfig recognitionConfig =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setLanguageCode("en-US")
                            .setSampleRateHertz(16000)
                            .build();
            StreamingRecognitionConfig streamingRecognitionConfig =
                    StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).setInterimResults(false).build();

            StreamingRecognizeRequest request =
                    StreamingRecognizeRequest.newBuilder()
                            .setStreamingConfig(streamingRecognitionConfig)
                            .build();

            clientStream.send(request);
            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info targetInfo =
                    new Info(TargetDataLine.class, audioFormat);

            if (!AudioSystem.isLineSupported(targetInfo)) {
                System.out.println("Microphone not supported");
                System.exit(0);
            }

            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            System.out.println("Start speaking");
            long startTime = System.currentTimeMillis();
            long estimatedTime = System.currentTimeMillis() - startTime;

            AudioInputStream audio = new AudioInputStream(targetDataLine);
            while (estimatedTime <= timeToRun ) {
                estimatedTime = System.currentTimeMillis() - startTime;
                byte[] data = new byte[640];
                audio.read(data);
                request = StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build();
                clientStream.send(request);
            }
            targetDataLine.stop();
            targetDataLine.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        responseObserver.onComplete();
    }
}
