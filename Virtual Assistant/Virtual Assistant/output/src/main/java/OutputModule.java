import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ALL")
public class OutputModule {
    // Audio playback parameters
    private static final AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;

    // Singleton instance for reusing the TTS client
    private static OutputModule instance;

    // Thread pool for async operations
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    // Pre-warm flag
    private static boolean isPrewarmed = false;

    // Google Cloud credentials
    private final GoogleCredentials credentials;
    private final String credentialsPath;

    // TTS Client
    private TextToSpeechClient textToSpeechClient;

    // Voice selection parameters - configurable
    private String languageCode = "es-ES";  // Default to Spanish
    private String voiceName = "es-ES-Standard-A";  // Default voice
    private double pitch = 0.0;  // Default pitch (normal)
    private double speakingRate = 1.0;  // Default speaking rate (normal)

    // Audio profile
    private AudioEncoding audioEncoding = AudioEncoding.LINEAR16;  // Default encoding

    /**
     * Private constructor for singleton pattern
     */
    private OutputModule(String credentialsPath) throws IOException {
        this.credentialsPath = credentialsPath;
        // Load credentials from file
        this.credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));

        // Initialize the TTS client
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        this.textToSpeechClient = TextToSpeechClient.create(settings);
    }

    /**
     * Get singleton instance
     */
    public static synchronized OutputModule getInstance(String credentialsPath) throws IOException {
        if (instance == null) {
            instance = new OutputModule(credentialsPath);

            // Pre-warm the connection if not already done
            if (!isPrewarmed) {
                instance.prewarmConnection();
                isPrewarmed = true;
            }
        }
        return instance;
    }

    /**
     * Pre-warm the connection to Google Cloud
     */
    private void prewarmConnection() {
        executor.submit(() -> {
            try {
                // Send a tiny request to establish connection
                SynthesisInput input = SynthesisInput.newBuilder()
                        .setText(".")
                        .build();

                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode(languageCode)
                        .setName(voiceName)
                        .build();

                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(audioEncoding)
                        .setPitch(pitch)
                        .setSpeakingRate(speakingRate)
                        .build();

                textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                System.out.println("TTS connection pre-warmed successfully");
            } catch (Exception e) {
                System.err.println("Error pre-warming TTS connection: " + e.getMessage());
            }
        });
    }

    /**
     * Speaks the provided text using Google Cloud TTS
     * @param text The text to be spoken
     * @param waitForCompletion Whether to wait for speech to complete
     * @return true if successful, false otherwise
     */
    public boolean speak(String text, boolean waitForCompletion) {
        try {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // Build the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setName(voiceName)
                    .build();

            // Build the audio config
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(audioEncoding)
                    .setPitch(pitch)
                    .setSpeakingRate(speakingRate)
                    .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio content
            ByteString audioContent = response.getAudioContent();

            // Play the audio - either wait or play asynchronously
            if (waitForCompletion) {
                playAudio(audioContent.toByteArray());
            } else {
                playAudioAsync(audioContent.toByteArray());
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error in TTS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convenience method with default wait behavior
     */
    public boolean speak(String text) {
        return speak(text, true);
    }

    /**
     * Plays the audio bytes through the default audio system
     * @param audioData The audio data to play
     * @throws Exception If there's an error playing the audio
     */
    private void playAudio(byte[] audioData) throws Exception {
        // Set up an audio input stream from the synthesized audio content
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                new ByteArrayInputStream(audioData));

        // Get the audio format from the audio input stream
        AudioFormat format = audioInputStream.getFormat();

        // Set up a data line info object for the source data line
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        // Get a source data line for playback
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        // Create a buffer for transferring the audio data to the line
        byte[] buffer = new byte[4096];
        int bytesRead;

        // Set up a latch to wait for playback to complete
        CountDownLatch playbackLatch = new CountDownLatch(1);

        // Read and play the audio data
        try {
            while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                line.write(buffer, 0, bytesRead);
            }

            // Wait for the buffer to empty
            line.drain();
        } finally {
            // Close and clean up resources
            line.close();
            audioInputStream.close();
            playbackLatch.countDown();
        }

        // Wait for playback to complete
        playbackLatch.await();
    }

    /**
     * Plays audio asynchronously
     */
    private void playAudioAsync(byte[] audioData) {
        executor.submit(() -> {
            try {
                playAudio(audioData);
            } catch (Exception e) {
                System.err.println("Error playing audio asynchronously: " + e.getMessage());
            }
        });
    }

    /**
     * Sets the language code for TTS
     * @param languageCode The language code (e.g., "es-ES", "en-US")
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    /**
     * Sets the voice name for TTS
     * @param voiceName The voice name (e.g., "es-ES-Standard-A")
     */
    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    /**
     * Sets the pitch for TTS (between -20.0 and 20.0)
     * @param pitch The pitch value
     */
    public void setPitch(double pitch) {
        this.pitch = Math.max(-20.0, Math.min(20.0, pitch));
    }

    /**
     * Sets the speaking rate for TTS (between 0.25 and 4.0)
     * @param speakingRate The speaking rate
     */
    public void setSpeakingRate(double speakingRate) {
        this.speakingRate = Math.max(0.25, Math.min(4.0, speakingRate));
    }

    /**
     * Sets the audio encoding for TTS
     * @param encoding The audio encoding
     */
    public void setAudioEncoding(AudioEncoding encoding) {
        this.audioEncoding = encoding;
    }

    /**
     * Shuts down the executor service and client
     * Should be called when the application exits
     */
    public void shutdown() {
        executor.shutdown();
        if (textToSpeechClient != null) {
            textToSpeechClient.close();
        }
    }

    /**
     * Example usage with singleton pattern and optimizations
     */
    public static void ttsSpeech(String text) {
        try {
            OutputModule outputModule = OutputModule.getInstance("Virtual Assistant/utils/credentials.json");
            outputModule.speak(text, false); // non-blocking call
            // Note: Don't close the client after each use
        } catch (IOException e) {
            System.err.println("Error in TTS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String text = "Buenos días! Soy Hedy, tu asistente virtual, estoy aquí para ayudarte!";
        long startTime = System.nanoTime(); // Iniciar el cronómetro
        ttsSpeech(text);
        long endTime = System.nanoTime(); // Detener el cronómetro
        double elapsedTimeInSeconds = (endTime - startTime) / 1.0e9; // Convertir nanosegundos a segundos
        System.out.println("Tiempo de inicio de la reproducción: " + elapsedTimeInSeconds + " segundos");

        // Since the speech plays asynchronously now, let's wait to prevent program exit
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Only shutdown when application exits
        try {
            OutputModule.getInstance("Virtual Assistant/utils/credentials.json").shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}