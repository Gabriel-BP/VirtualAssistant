import ai.picovoice.porcupine.Porcupine;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class WakeWordDetector {

    private Porcupine porcupine;
    private TargetDataLine micDataLine;

    public WakeWordDetector(String accessKey, String modelPath,
                            String[] keywordPaths, float[] sensitivities, int audioDeviceIndex) throws Exception {
        // Inicializa Porcupine
        porcupine = new Porcupine.Builder()
                .setAccessKey(accessKey)
                .setModelPath(modelPath)
                .setKeywordPaths(keywordPaths)
                .setSensitivities(sensitivities)
                .build();

        // Configura el micrófono
        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        micDataLine = getAudioDevice(audioDeviceIndex, dataLineInfo);
        micDataLine.open(format);
    }

    public void startListening() {
        try {
            micDataLine.start();
            System.out.println("Listening for wake words...");
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
                        System.out.printf("[%s] Wake word detected!\n",
                                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        // Aquí puedes iniciar la lógica del asistente virtual.
                    }
                }
            }
        } catch (Exception e) {
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

    private TargetDataLine getAudioDevice(int deviceIndex, DataLine.Info dataLineInfo)
            throws LineUnavailableException {
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
        // Cambiar aquí
        return (TargetDataLine) AudioSystem.getLine(dataLineInfo);
    }
}
