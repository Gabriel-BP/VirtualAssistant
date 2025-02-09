import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.OutputStream;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import java.io.FileInputStream;


public class TextToSpeechService {

    private final TextToSpeechClient client;

    public TextToSpeechService(String credentialsPath) throws Exception {
        // Configura las credenciales desde el archivo proporcionado
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
        this.client = TextToSpeechClient.create(TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build());
    }

    public void synthesizeSpeech(String text, String outputFilePath) throws Exception {
        // Configura el texto de entrada
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        // Configura la voz deseada
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("es-ES")
                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                .build();

        // Configura el formato del audio de salida
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        // Realiza la solicitud de síntesis de texto a voz
        SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

        // Escribe el contenido del audio en un archivo
        ByteString audioContents = response.getAudioContent();
        try (OutputStream out = new FileOutputStream(outputFilePath)) {
            out.write(audioContents.toByteArray());
            System.out.println("Audio content written to file: " + outputFilePath);
        }
    }

    public void close() throws Exception {
        client.close();
    }

    public static void main(String[] args) {
        try {
            String credentialsPath = "credentials.json";
            String outputFilePath = "output.mp3";
            String textToSpeak = "Hola, me llamo Hedy, y soy tu asistente virtual.";

            TextToSpeechService tts = new TextToSpeechService(credentialsPath);
            tts.synthesizeSpeech(textToSpeak, outputFilePath);
            tts.close();
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}