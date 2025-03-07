import ai.picovoice.porcupine.Porcupine;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("ALL")
public class WakeWordDetector {

    private final GUICallback guiCallback;
    private final HedyAssistant assistant; // Añadimos el asistente para procesar las entradas
    private Porcupine porcupine;
    private TargetDataLine micDataLine;

    public WakeWordDetector(String accessKey, String modelPath,
                            String[] keywordPaths, float[] sensitivities, int audioDeviceIndex,
                            GUICallback guiCallback, HedyAssistant assistant) throws Exception {
        this.guiCallback = guiCallback;
        this.assistant = assistant; // Guardamos la referencia al asistente

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
            guiCallback.appendMessage("Hedy", "Escuchando... Diga la palabra clave para activar");
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
                        guiCallback.appendMessage("Hedy", "¡Palabra clave detectada! Hable ahora...");

                        // Detenemos temporalmente la detección
                        micDataLine.stop();
                        micDataLine.close();

                        // Creamos un nuevo InputModule y transcribimos
                        try {
                            InputModule inputModule = new InputModule("Virtual Assistant/utils/credentials.json");
                            String transcript = inputModule.transcribeFromMicrophone(); // Usamos el metodo que abre su propio micrófono

                            if (!transcript.isEmpty()) {
                                guiCallback.appendMessage("Usuario", transcript);

                                // Procesamos la transcripción con el asistente
                                if (assistant != null) {
                                    String response = assistant.processInput(transcript);
                                    guiCallback.appendMessage("Hedy", response);
                                } else {
                                    guiCallback.appendMessage("Hedy", "No puedo procesar la entrada porque el asistente no está inicializado.");
                                }
                            } else {
                                guiCallback.appendMessage("Hedy", "No se detectó audio. Intente hablar más fuerte.");
                            }
                        } catch (Exception e) {
                            guiCallback.appendMessage("Hedy", "Error al transcribir: " + e.getMessage());
                            e.printStackTrace();
                        }

                        // Reiniciamos el micrófono para la detección de palabras clave
                        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
                        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
                        micDataLine = getAudioDevice(-1, dataLineInfo);
                        micDataLine.open(format);
                        micDataLine.start();
                        guiCallback.appendMessage("Hedy", "Volviendo a escuchar la palabra clave...");
                    }
                }
            }
        } catch (Exception e) {
            guiCallback.appendMessage("Hedy", "Error durante la detección de la palabra clave: " + e.getMessage());
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