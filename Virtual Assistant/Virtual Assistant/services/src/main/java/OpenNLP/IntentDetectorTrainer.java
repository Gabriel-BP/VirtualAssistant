package OpenNLP;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

public class IntentDetectorTrainer {

    public static void main(String[] args) {
        try {
            // Ruta a los archivos de entrenamiento y prueba
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
        // Configurar el flujo de datos de entrenamiento
        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(trainingDataFile);
        ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
        ObjectStream<DocumentSample> sampleStream = new IntentStream(lineStream);

        // Configurar los parámetros de entrenamiento
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, "100");
        params.put(TrainingParameters.CUTOFF_PARAM, "1");

        // Entrenar el modelo

        return DocumentCategorizerME.train("es", sampleStream, params, new DoccatFactory());
    }

    private static void saveModel(DoccatModel model) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("Virtual Assistant/utils/intent-detector.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            model.serialize(bos);
        }
        System.out.println("Modelo guardado en: " + "Virtual Assistant/utils/intent-detector.bin");
    }

    private static void evaluateModel(DoccatModel model, File testDataFile) throws IOException {
        // Crear categorizador
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);

        // Leer archivo de prueba
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(testDataFile), StandardCharsets.UTF_8))) {

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

                // Actualizar contador de categorías
                categoryCount.put(expectedCategory, categoryCount.getOrDefault(expectedCategory, 0) + 1);

                // Clasificar
                double[] outcomes = categorizer.categorize(text.split("\\s+"));
                String predictedCategory = categorizer.getBestCategory(outcomes);

                // Comprobar precisión
                totalSamples++;
                if (predictedCategory.equals(expectedCategory)) {
                    correctPredictions++;
                    correctCount.put(expectedCategory, correctCount.getOrDefault(expectedCategory, 0) + 1);
                }
            }

            // Mostrar resultados
            System.out.println("\nEvaluación del modelo:");
            System.out.println("Total de muestras: " + totalSamples);
            System.out.println("Predicciones correctas: " + correctPredictions);
            double accuracy = (double) correctPredictions / totalSamples * 100;
            System.out.printf("Precisión: %.2f%%\n", accuracy);

            // Mostrar precisión por categoría
            System.out.println("\nPrecisión por categoría:");
            for (String category : categoryCount.keySet()) {
                int total = categoryCount.get(category);
                int correct = correctCount.getOrDefault(category, 0);
                double categoryAccuracy = (double) correct / total * 100;
                System.out.printf("%s: %.2f%% (%d/%d)\n", category, categoryAccuracy, correct, total);
            }
        }
    }

    private static void testModel(DoccatModel model) {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);

        // Frases de prueba
        String[] testSentences = {
                "dime qué hora es por favor",
                "necesito saber la fecha de hoy",
                "cómo está el clima en Barcelona",
                "cuál es la temperatura actual",
                "qué películas hay en el cine esta semana",
                "series que están de moda actualmente",
                "qué temperatura hace",
                "cuál es el tiempo en Londres",
                "que tiempo hace"
        };

        System.out.println("\nPruebas con frases de ejemplo:");
        for (String sentence : testSentences) {
            double[] outcomes = categorizer.categorize(sentence.split("\\s+"));
            String category = categorizer.getBestCategory(outcomes);

            System.out.println("Frase: \"" + sentence + "\"");
            System.out.println("Intención detectada: " + category);
            System.out.println("Confianza: " + outcomes[categorizer.getIndex(category)] * 100 + "%");

            // Mostrar top 3 categorías
            System.out.println("Top 3 categorías:");
            Map<String, Double> probabilities = new HashMap<>();
            for (int i = 0; i < outcomes.length; i++) {
                String cat = categorizer.getCategory(i);
                probabilities.put(cat, outcomes[i]);
            }

            probabilities.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .forEach(entry -> System.out.printf("  - %s: %.2f%%\n", entry.getKey(), entry.getValue() * 100));

            System.out.println();
        }
    }

    // Clase para leer los datos en formato de OpenNLP DocumentSample
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
                    String text = parts[0];
                    String category = parts[1];

                    // Tokenizamos el texto (split básico por espacios)
                    String[] tokens = text.split("\\s+");
                    return new DocumentSample(category, tokens);
                }
            }
            return null;
        }

        @Override
        public void reset() throws IOException, UnsupportedOperationException {
            lineStream.reset();
        }

        @Override
        public void close() throws IOException {
            lineStream.close();
        }
    }
}