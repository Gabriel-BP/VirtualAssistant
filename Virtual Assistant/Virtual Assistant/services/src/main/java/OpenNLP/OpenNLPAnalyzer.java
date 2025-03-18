package OpenNLP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class OpenNLPAnalyzer {

    public static String analize(String input) throws IOException {
        // Cargar el modelo entrenado
        File modelFile = new File("Virtual Assistant/utils/intent-detector.bin");
        DoccatModel model = new DoccatModel(new FileInputStream(modelFile));
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);

        // Procesar la entrada
        String[] tokens = input.split("\\s+");
        double[] outcomes = categorizer.categorize(tokens);
        String bestCategory = categorizer.getBestCategory(outcomes);

        // Mostrar resultados
        double confianza =  outcomes[categorizer.getIndex(bestCategory)] * 100;
        if (confianza < 20) {
            return null;
        }
        return bestCategory;
    }

    public static void main(String[] args) throws IOException {
        String category = analize("Hola");
        System.out.println(category);
    }
}