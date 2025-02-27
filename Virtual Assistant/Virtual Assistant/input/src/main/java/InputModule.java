import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class InputModule {

    public static void main(String[] args) {
        try {
            new InputModule().streamingMicRecognize(60000); // Reconocer por 1 minuto
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void streamingMicRecognize(int durationMs) throws Exception {
        ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        SpeechClient client = null;
        TargetDataLine targetDataLine = null;

        try {
            // Configurar credenciales y cliente
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("Virtual Assistant/utils/credentials.json"));
            SpeechSettings settings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            client = SpeechClient.create(settings);

            // Configurar respuesta del cliente
            responseObserver = new ResponseObserver<>() {
                ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                @Override
                public void onStart(StreamController controller) {}

                @Override
                public void onResponse(StreamingRecognizeResponse response) {
                    responses.add(response);
                }

                @Override
                public void onComplete() {
                    System.out.println("Transcripción finalizada:");
                    for (StreamingRecognizeResponse response : responses) {
                        if (!response.getResultsList().isEmpty()) {
                            StreamingRecognitionResult result = response.getResultsList().get(0);
                            if (!result.getAlternativesList().isEmpty()) {
                                System.out.printf("Transcripción: %s\n", result.getAlternatives(0).getTranscript());
                            }
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error durante el reconocimiento: " + t.getMessage());
                }
            };

            // Configurar cliente de streaming
            ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);
            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("es-ES")
                    .setSampleRateHertz(16000)
                    .build();
            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .build();

            // Primer request
            clientStream.send(StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build());

            // Configurar audio
            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(targetInfo)) {
                System.err.println("El micrófono no es compatible.");
                return;
            }

            targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            System.out.println("Empieza a hablar...");

            // Capturar y enviar audio
            long startTime = System.currentTimeMillis();
            AudioInputStream audioStream = new AudioInputStream(targetDataLine);
            byte[] data = new byte[6400];

            while (System.currentTimeMillis() - startTime < durationMs) {
                audioStream.read(data);
                clientStream.send(StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build());
            }

            System.out.println("Se detiene la grabación.");
        } finally {
            if (targetDataLine != null && targetDataLine.isOpen()) {
                targetDataLine.stop();
                targetDataLine.close();
            }
            if (responseObserver != null) {
                responseObserver.onComplete();
            }
            if (client != null) {
                client.close();
            }
        }
    }
}
