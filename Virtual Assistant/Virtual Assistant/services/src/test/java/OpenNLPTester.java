import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class OpenNLPTester {

    public static void main(String[] args) throws IOException {
            // Cargar el modelo entrenado
            File modelFile = new File("Virtual Assistant/utils/intent-detector.bin");
            DoccatModel model = new DoccatModel(new FileInputStream(modelFile));
            DocumentCategorizerME categorizer = new DocumentCategorizerME(model);

            String input = "qué fecha es";

            // Procesar la entrada
            String[] tokens = input.split("\\s+");
            double[] outcomes = categorizer.categorize(tokens);
            String bestCategory = categorizer.getBestCategory(outcomes);

            // Mostrar resultados
            System.out.println("\n=== Resultados ===");
            System.out.println("Intención principal: " + bestCategory);
            System.out.printf("Confianza: %.2f%%%n", outcomes[categorizer.getIndex(bestCategory)] * 100);

            // Mostrar probabilidades de todas las categorías
            System.out.println("\nProbabilidades por categoría:");
            Map<String, Double> probabilities = new HashMap<>();
            for (int i = 0; i < categorizer.getNumberOfCategories(); i++) {
                probabilities.put(categorizer.getCategory(i), outcomes[i]);
            }

            probabilities.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(entry -> System.out.printf("- %s: %.2f%%%n",
                            entry.getKey(), entry.getValue() * 100));
            System.out.println();


    }
}