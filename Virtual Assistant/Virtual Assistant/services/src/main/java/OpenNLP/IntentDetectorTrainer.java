package OpenNLP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import opennlp.tools.doccat.*;
import opennlp.tools.util.*;

public class IntentDetectorTrainer {

    public static void main(String[] args) {
        try {
            // Usa los archivos proporcionados para entrenamiento y prueba
            File trainingDataFile = new File("Virtual Assistant/utils/train.txt");
            File testDataFile = new File("Virtual Assistant/utils/test.txt");

            // Entrenar el modelo
            DoccatModel model = trainModel(trainingDataFile);

            // Guardar el modelo en disco
            saveModel(model);

            // Evaluar el modelo con datos de prueba
            evaluateModel(model, testDataFile);

            // Probar el modelo con algunas frases de ejemplo
            testModel(model);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static DoccatModel trainModel(File trainingDataFile) throws IOException {
        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(trainingDataFile);
        ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
        ObjectStream<DocumentSample> sampleStream = new IntentStream(lineStream);

        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, "100");
        params.put(TrainingParameters.CUTOFF_PARAM, "1");

        return DocumentCategorizerME.train("es", sampleStream, params, new DoccatFactory());
    }

    private static void saveModel(DoccatModel model) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("Virtual Assistant/utils/intent-detector.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            model.serialize(bos);
        }
        System.out.println("Modelo guardado en: Virtual Assistant/utils/intent-detector.bin");
    }

    private static void evaluateModel(DoccatModel model, File testDataFile) throws IOException {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testDataFile), StandardCharsets.UTF_8))) {
            String line;
            int totalSamples = 0;
            int correctPredictions = 0;
            Map<String, Integer> categoryCount = new HashMap<>();
            Map<String, Integer> correctCount = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\t");
                if (parts.length != 2) continue;

                String text = parts[0];
                String expectedCategory = parts[1];
                categoryCount.put(expectedCategory, categoryCount.getOrDefault(expectedCategory, 0) + 1);

                double[] outcomes = categorizer.categorize(text.split("\\s+"));
                String predictedCategory = categorizer.getBestCategory(outcomes);

                totalSamples++;
                if (predictedCategory.equals(expectedCategory)) {
                    correctPredictions++;
                    correctCount.put(expectedCategory, correctCount.getOrDefault(expectedCategory, 0) + 1);
                }
            }

            System.out.printf("\nPrecisión: %.2f%%\n", (double) correctPredictions / totalSamples * 100);
            categoryCount.forEach((category, total) -> {
                int correct = correctCount.getOrDefault(category, 0);
                System.out.printf("%s: %.2f%% (%d/%d)\n", category, (double) correct / total * 100, correct, total);
            });
        }
    }

    private static void testModel(DoccatModel model) {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
        String[] testSentences = {
                "dime qué hora es por favor",
                "necesito saber la fecha de hoy",
                "cómo está el clima en Barcelona",
                "qué películas hay en el cine esta semana"
        };

        for (String sentence : testSentences) {
            double[] outcomes = categorizer.categorize(sentence.split("\\s+"));
            String category = categorizer.getBestCategory(outcomes);
            System.out.printf("Frase: \"%s\" → Intención detectada: %s (%.2f%%)\n", sentence, category, outcomes[categorizer.getIndex(category)] * 100);
        }
    }

    static class IntentStream implements ObjectStream<DocumentSample> {
        private final ObjectStream<String> lineStream;

        IntentStream(ObjectStream<String> lineStream) {
            this.lineStream = lineStream;
        }

        @Override
        public DocumentSample read() throws IOException {
            String line = lineStream.read();
            if (line != null && !line.trim().isEmpty()) {
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    // 1. Convertir a minúsculas
                    String cleanText = parts[0].toLowerCase();

                    // 2. Eliminar signos de puntuación
                    cleanText = cleanText.replaceAll("[^a-zA-Z0-9áéíóúüñÁÉÍÓÚÜÑ\\s]", "");

                    // 3. Tokenizar el texto (usando TokenizerME si está disponible)
                    String[] tokens = cleanText.split("\\s+"); // O usa TokenizerME aquí

                    return new DocumentSample(parts[1], tokens);
                }
            }
            return null;
        }

        @Override
        public void reset() throws IOException {
            lineStream.reset();
        }

        @Override
        public void close() throws IOException {
            lineStream.close();
        }
    }
}