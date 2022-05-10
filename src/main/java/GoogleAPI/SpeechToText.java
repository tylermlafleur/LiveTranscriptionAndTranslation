package GoogleAPI;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.cloud.translate.Language;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpeechToText {
    private boolean transcribing;
    private String fileLocation;
    private FileOutput originalTextFile;
    private FileOutput translatedTextFile;

    private StringBuilder originalTranscription;
    private StringBuilder translatedTranscription;

    private Translate translate;
    private CredentialsProvider credentials;

    public SpeechToText(CredentialsProvider credentials) throws IOException {
        this.credentials = credentials;
        translate = new Translate(credentials);
        fileLocation = "";
        transcribing = false;
        originalTranscription = new StringBuilder();
        translatedTranscription = new StringBuilder();
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public void setTranscribing(boolean transcribing) {
            this.transcribing = transcribing;
    }

    public boolean getTranscribing() {
        return transcribing;
    }

    public void streamingMicRecognize(boolean recordTranscription, String originalLanguage, String targetLanguage,
                                      JTextArea originalTextArea, JTextArea translatedTextArea, JLabel statusLabel) {

        SpeechSettings settings = null;
        try {
            settings = SpeechSettings.newBuilder().setCredentialsProvider(credentials).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        try (SpeechClient client = SpeechClient.create(settings)) {

            responseObserver =
                    new ResponseObserver<StreamingRecognizeResponse>() {
                        ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                        public void onStart(StreamController controller) {
                            if(recordTranscription) {
                                try {
                                    originalTextFile = new FileOutput(fileLocation + "originalTranscription.txt");
                                    translatedTextFile = new FileOutput(fileLocation + "translatedTranscription.txt");
                                    originalTextFile.openFile();
                                    translatedTextFile.openFile();
                                    originalTextFile.writeToFile("Begin Transcription. Date: " + new Date().toString() + " Original Language = " + originalLanguage + ":\n");
                                    String translateOut = "Begin Transcription. Date: " + new Date().toString() + " Translated to = " + targetLanguage + ":\n";
                                    translatedTextFile.writeToFile(translate.translateText(translateOut, targetLanguage));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        public void onResponse(StreamingRecognizeResponse response) {
                            String res = response.getResultsList().get(0).getAlternativesList().get(0).getTranscript().trim() + ".";
                            String translation = null;
                            try {
                                translation = translate.translateText(res, targetLanguage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Original: " + res);
                            System.out.println("Translation: " + translation);

                            originalTextArea.append(res + "\n");
                            translatedTextArea.append(translation + "\n");

                            if(recordTranscription) {
                                try {
                                    originalTextFile.writeToFile(res + "\n");
                                    translatedTextFile.writeToFile(translation + "\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            originalTranscription.append(res + "\n");
                            translatedTranscription.append(translation + "\n");


                            responses.add(response);
                        }

                        public void onComplete() {
                            try {
                                statusLabel.setText("Start Translation");
                                if (recordTranscription) {
                                    originalTextFile.writeToFile("End transcription.\n\n");
                                    translatedTextFile.writeToFile("End transcription.\n\n");
                                    originalTextFile.closeFile();
                                    translatedTextFile.closeFile();
                                }
                                System.out.println("\nOriginal Transcription:\n" + originalTranscription.toString());
                                String translateOut = "Translated Transcription:\n" + translatedTranscription.toString();
                                System.out.println(translate.translateText(translateOut, targetLanguage));
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
            statusLabel.setText("Start speaking...");
            System.out.println("Start speaking");

            AudioInputStream audio = new AudioInputStream(targetDataLine);
            while (transcribing) {
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
