package OpenNLP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class OpenNLPAnalyzer {

    private static Map<String, Double> categoryThresholds = new HashMap<>();

    static {
        // Definir umbrales específicos para cada categoría
        categoryThresholds.put("decir_hora", 30.0);
        categoryThresholds.put("decir_fecha", 35.0);
        categoryThresholds.put("consultar_clima", 40.0);
        categoryThresholds.put("consultar_clima_lugar", 40.0);
        categoryThresholds.put("consultar_cartelera", 40.0);
        categoryThresholds.put("consultar_estrenos", 42.0);
        categoryThresholds.put("consultar_peliculas_populares", 40.0);
        categoryThresholds.put("consultar_series_emision", 42.0);
        categoryThresholds.put("consultar_series_populares", 40.0);
        categoryThresholds.put("fallback", 10.0); // Umbral muy bajo para fallback
    }

    public static String analize(String input) throws IOException {
        // Cargar el modelo entrenado
        File modelFile = new File("Virtual Assistant/utils/intent-detector.bin");
        DoccatModel model = new DoccatModel(new FileInputStream(modelFile));
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);

        // Procesar la entrada
        String[] tokens = input.split("\\s+");
        double[] outcomes = categorizer.categorize(tokens);

        // Encontrar la mejor y segunda mejor categoría
        String bestCategory = null;
        String secondBestCategory = null;
        double bestScore = -1;
        double secondBestScore = -1;

        for (int i = 0; i < outcomes.length; i++) {
            if (outcomes[i] > bestScore) {
                secondBestScore = bestScore;
                secondBestCategory = bestCategory;
                bestScore = outcomes[i];
                bestCategory = categorizer.getCategory(i);
            } else if (outcomes[i] > secondBestScore) {
                secondBestScore = outcomes[i];
                secondBestCategory = categorizer.getCategory(i);
            }
        }

        // Calcular la confianza y la diferencia entre las dos mejores categorías
        double confianza = bestScore * 100;
        double diferencia = (bestScore - secondBestScore) * 100;

        // Obtener el umbral específico para la categoría o usar el valor por defecto
        double threshold = categoryThresholds.getOrDefault(bestCategory, 20.0);

        // Umbral de diferencia mínima (más alto para categorías de entretenimiento)
        double minDiferencia = 10.0;
        if (bestCategory.contains("cartelera") || bestCategory.contains("peliculas") ||
                bestCategory.contains("series") || bestCategory.contains("estrenos")) {
            minDiferencia = 15.0; // Diferencia mayor para categorías de entretenimiento
        }

        // Tratamiento especial para la categoría fallback
        if (bestCategory.equals("fallback")) {
            System.out.println("Entrada: " + input);
            System.out.println("Categoría detectada: fallback");
            System.out.println("Resultado: NULL (delegando al LLM)");
            return null;
        }

        // Mostrar información para depuración (opcional)
        System.out.println("Entrada: " + input);
        System.out.println("Mejor categoría: " + bestCategory + " (confianza: " + confianza + "%)");
        System.out.println("Segunda mejor: " + secondBestCategory + " (confianza: " + secondBestScore * 100 + "%)");
        System.out.println("Diferencia: " + diferencia + "%");
        System.out.println("Umbral para " + bestCategory + ": " + threshold + "%");

        // Verificar si cumple con ambos criterios: umbral de confianza y diferencia mínima
        if (confianza < threshold || diferencia < minDiferencia) {
            System.out.println("Resultado: NULL (no cumple criterios)");
            return null;
        }

        System.out.println("Resultado: " + bestCategory);
        return bestCategory;
    }

    public static void main(String[] args) {
        try {
            // Ejemplos para probar
            String[] ejemplos = {
                    "qué hora es ahora",
                    "que tiempo hace en Madrid",
                    "hola buenos días",
                    "qué películas están en cartelera",
                    "no estoy seguro de qué quiero preguntar",
                    "cuéntame sobre la historia de España",
                    "quiero saber sobre inteligencia artificial",
                    "¿cómo funciona un motor de combustión?",
                    "dime cómo preparar una paella",
                    "¿cuál es la capital de Australia?",
                    "que tiempo hace"
            };

            for (String ejemplo : ejemplos) {
                System.out.println("\n===== PRUEBA =====");
                String resultado = analize(ejemplo);
                System.out.println("===== FIN PRUEBA =====\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}