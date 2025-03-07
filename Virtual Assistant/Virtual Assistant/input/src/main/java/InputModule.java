import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class InputModule {
    // Audio recording parameters
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    private static final float SILENCE_THRESHOLD = 1000; // Adjust based on your environment
    private static final long SILENCE_DURATION_MS = 1500; // 1.5 seconds of silence to detect end of speech

    // Google Cloud credentials
    private final GoogleCredentials credentials;
    private final String credentialsPath;

    public InputModule(String credentialsPath) throws IOException {
        this.credentialsPath = credentialsPath;
        // Load credentials from file
        this.credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
    }

    public String transcribeFromMicrophone() {
        // Create completion latch to wait for the transcription to complete
        CountDownLatch completionLatch = new CountDownLatch(1);

        // Final transcript holder
        StringBuilder finalTranscript = new StringBuilder();

        try {
            // Create SpeechClient with explicit credentials
            SpeechSettings speechSettings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            SpeechClient speechClient = SpeechClient.create(speechSettings);

            // Configure recognition
            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("es-ES") // Spanish
                    .setSampleRateHertz(SAMPLE_RATE)
                    .setModel("default")
                    .build();

            // Configure streaming recognition
            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .setInterimResults(true)
                    .build();

            // Create a streaming observer
            ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<>() {
                private final List<StreamingRecognizeResponse> responses = new ArrayList<>();

                @Override
                public void onStart(StreamController controller) {
                    // Do nothing on start
                }

                @Override
                public void onResponse(StreamingRecognizeResponse response) {
                    responses.add(response);

                    if (response.getResultsCount() > 0) {
                        // Process each response
                        StreamingRecognitionResult result = response.getResultsList().get(0);
                        String transcript = result.getAlternativesList().get(0).getTranscript();

                        // If this is a final result, update the finalTranscript
                        if (result.getIsFinal()) {
                            finalTranscript.setLength(0);
                            finalTranscript.append(transcript);
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error in transcription: " + t.getMessage());
                    completionLatch.countDown();
                }

                @Override
                public void onComplete() {
                    completionLatch.countDown();
                }
            };

            // Start bidirectional streaming
            ClientStream<StreamingRecognizeRequest> clientStream = speechClient.streamingRecognizeCallable().splitCall(responseObserver);

            // First request must only contain the streaming configuration
            StreamingRecognizeRequest configRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingConfig)
                    .build();
            clientStream.send(configRequest);

            // Start audio capture
            AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, true, false);
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            if (!AudioSystem.isLineSupported(targetInfo)) {
                System.err.println("Microphone not supported");
                speechClient.close();
                return "";
            }

            // Set up the microphone
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(targetInfo);
            microphone.open(audioFormat);
            microphone.start();

            System.out.println("Escuchando...");

            // Buffer for reading from the microphone
            byte[] data = new byte[4096];
            long lastSoundTime = System.currentTimeMillis();
            boolean speechEnded = false;

            // Keep reading from the microphone until silence is detected
            while (!speechEnded) {
                int numBytesRead = microphone.read(data, 0, data.length);

                // Check sound level to detect silence
                if (isSoundDetected(data, numBytesRead)) {
                    lastSoundTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - lastSoundTime > SILENCE_DURATION_MS) {
                    // Silence detected for SILENCE_DURATION_MS milliseconds
                    System.out.println("Silence detected, ending transcription...");
                    speechEnded = true;
                }

                // Send the audio data
                if (numBytesRead > 0) {
                    ByteString audioBytes = ByteString.copyFrom(data, 0, numBytesRead);
                    StreamingRecognizeRequest audioRequest = StreamingRecognizeRequest.newBuilder()
                            .setAudioContent(audioBytes)
                            .build();
                    clientStream.send(audioRequest);
                }
            }

            // Close the microphone and the stream
            microphone.close();
            clientStream.closeSend();

            // Wait for the transcription to complete
            try {
                completionLatch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Completion interrupted: " + e.getMessage());
            }

            // Close the speech client when done
            speechClient.close();

        } catch (Exception e) {
            System.err.println("Exception during transcription: " + e.getMessage());
            e.printStackTrace();
        }

        return finalTranscript.toString();
    }

    // Helper method to detect sound level
    private boolean isSoundDetected(byte[] audioData, int bytesRead) {
        // Calculate the sound level
        long sum = 0;
        for (int i = 0; i < bytesRead; i += 2) {
            int sample = ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
            sum += Math.abs(sample);
        }
        double amplitude = sum / (bytesRead / 2.0);

        return amplitude > SILENCE_THRESHOLD;
    }

    // Example usage
    public static void main(String[] args) {
        try {
            InputModule inputModule = new InputModule("Virtual Assistant/utils/credentials.json");
            String transcript = inputModule.transcribeFromMicrophone();
            System.out.println("Final transcript: " + transcript);
        } catch (IOException e) {
            System.err.println("Error initializing InputModule: " + e.getMessage());
            e.printStackTrace();
        }
    }
}