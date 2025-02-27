import ai.picovoice.porcupine.Porcupine;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


@SuppressWarnings("ALL")
public class WakeWordDetector {

    private final GUICallback guiCallback;
    private Porcupine porcupine;
    private TargetDataLine micDataLine;

    public WakeWordDetector(String accessKey, String modelPath,
                            String[] keywordPaths, float[] sensitivities, int audioDeviceIndex, GUICallback guiCallback) throws Exception {
        this.guiCallback = guiCallback;

        // Initialize Porcupine
        porcupine = new Porcupine.Builder()
                .setAccessKey(accessKey)
                .setModelPath(modelPath)
                .setKeywordPaths(keywordPaths)
                .setSensitivities(sensitivities)
                .build();

        // Configure microphone
        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        micDataLine = getAudioDevice(audioDeviceIndex, dataLineInfo);
        micDataLine.open(format);
    }

    public void startListening() {
        try {
            micDataLine.start();
            guiCallback.appendMessage("Hedy", "Escuchando...");
            int frameLength = porcupine.getFrameLength();
            ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
            captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
            short[] porcupineBuffer = new short[frameLength];

            while (true) {
                int bytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());
                if (bytesRead == frameLength * 2) {
                    captureBuffer.asShortBuffer().get(porcupineBuffer);
                    int result = porcupine.process(porcupineBuffer);
                    if (result >= 0) {
                        guiCallback.appendMessage("Hedy", "Palabra clave detectada!");
                        break; // Notify GUI or start transcription
                    }
                }
            }
        } catch (Exception e) {
            guiCallback.appendMessage("Hedy", "Error during wake word detection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            releaseResources();
        }
    }

    public void stopListening() {
        if (micDataLine != null) {
            micDataLine.stop();
        }
    }

    public void releaseResources() {
        if (micDataLine != null) {
            micDataLine.close();
        }
        if (porcupine != null) {
            porcupine.delete();
        }
    }

    private TargetDataLine getAudioDevice(int deviceIndex, DataLine.Info dataLineInfo) throws LineUnavailableException {
        if (deviceIndex >= 0) {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            if (deviceIndex < mixers.length) {
                Mixer mixer = AudioSystem.getMixer(mixers[deviceIndex]);
                if (mixer.isLineSupported(dataLineInfo)) {
                    return (TargetDataLine) mixer.getLine(dataLineInfo);
                }
            }
            System.err.printf("Invalid audio device index: %d. Using default device.\n", deviceIndex);
        }
        return (TargetDataLine) AudioSystem.getLine(dataLineInfo);
    }
}
