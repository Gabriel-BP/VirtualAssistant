import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@SuppressWarnings("ALL")
public class GoogleNLPAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(GoogleNLPAnalyzer.class.getName());
    private final LanguageServiceClient languageClient;

    /**
     * Constructor que inicializa el cliente de Google Cloud Natural Language
     * utilizando credenciales desde un archivo JSON específico.
     *
     * @param credentialsPath Ruta al archivo JSON de credenciales
     * @throws IOException Si hay un error al cargar las credenciales o crear el cliente
     */
    public GoogleNLPAnalyzer(String credentialsPath) throws IOException {
        // Cargar credenciales desde el archivo especificado
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsPath));

        // Configurar el cliente con las credenciales cargadas
        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        // Crear el cliente con la configuración personalizada
        this.languageClient = LanguageServiceClient.create(settings);

        // Configurar SLF4J para evitar los warnings
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    /**
     * Analiza las entidades en el texto proporcionado.
     *
     * @param text El texto a analizar
     * @param language Código de idioma ISO-639-1 (por ejemplo, "es" para español, "en" para inglés)
     * @return AnalyzeEntitiesResponse con los resultados del análisis
     */
    public AnalyzeEntitiesResponse analyzeEntities(String text, String language) {
        Document document = Document.newBuilder()
                .setContent(text)
                .setType(Type.PLAIN_TEXT)
                .setLanguage(language)
                .build();

        AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
                .setDocument(document)
                .build();

        return languageClient.analyzeEntities(request);
    }

    /**
     * Clasifica el texto en categorías.
     * Nota: El texto debe tener al menos 20 tokens para la clasificación.
     * Nota: Esta operación solo funciona con ciertos idiomas (principalmente inglés).
     *
     * @param text El texto a clasificar
     * @return ClassifyTextResponse con los resultados de la clasificación, o null si no es compatible
     */
    public ClassifyTextResponse classifyText(String text, String language) {
        try {
            Document document = Document.newBuilder()
                    .setContent(text)
                    .setType(Type.PLAIN_TEXT)
                    .setLanguage(language) // Usar el idioma proporcionado
                    .build();

            ClassifyTextRequest request = ClassifyTextRequest.newBuilder()
                    .setDocument(document)
                    .build();

            return languageClient.classifyText(request);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al clasificar texto: " + e.getMessage());
            return null;
        }
    }

    /**
     * Imprime los resultados del análisis de entidades de forma legible.
     *
     * @param response La respuesta del análisis de entidades
     */
    public void printEntityAnalysisResults(AnalyzeEntitiesResponse response) {
        System.out.println("=== ANÁLISIS DE ENTIDADES ===");

        for (Entity entity : response.getEntitiesList()) {
            System.out.printf("Entidad: %s\n", entity.getName());
            System.out.printf("Tipo: %s\n", entity.getType());
            System.out.printf("Relevancia: %.2f\n", entity.getSalience());

            System.out.println("Metadatos:");
            for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
                System.out.printf("\t%s: %s\n", entry.getKey(), entry.getValue());
            }

            System.out.println("Menciones:");
            for (EntityMention mention : entity.getMentionsList()) {
                System.out.printf("\tTexto: %s\n", mention.getText().getContent());
                System.out.printf("\tTipo: %s\n", mention.getType());
            }

            System.out.println();
        }
    }

    /**
     * Imprime los resultados de la clasificación de texto de forma legible.
     *
     * @param response La respuesta de la clasificación de texto
     */
    public void printClassificationResults(ClassifyTextResponse response) {
        if (response == null) {
            System.out.println("=== CLASIFICACIÓN DE TEXTO ===");
            System.out.println("No se pudo realizar la clasificación. Esta funcionalidad no está disponible para todos los idiomas.");
            System.out.println("Para clasificación, intente usar texto en inglés.");
            return;
        }

        System.out.println("=== CLASIFICACIÓN DE TEXTO ===");

        if (response.getCategoriesCount() == 0) {
            System.out.println("No se encontraron categorías para este texto.");
            return;
        }

        for (ClassificationCategory category : response.getCategoriesList()) {
            System.out.printf("Categoría: %s\n", category.getName());
            System.out.printf("Confianza: %.2f\n", category.getConfidence());
            System.out.println();
        }
    }

    /**
     * Traduce el texto a inglés para poder usar la funcionalidad de clasificación.
     * Nota: Esto es una implementación ficticia. Para una implementación real,
     * se debería usar Google Cloud Translation API u otro servicio similar.
     *
     * @param text El texto a traducir
     * @return El texto traducido
     */
    public String translateToEnglish(String text) {
        // Aquí se implementaría la lógica de traducción
        // Por ahora, este metodo es un placeholder
        return text;
    }

    /**
     * Cierra el cliente de la API cuando ya no sea necesario.
     */
    public void close() {
        languageClient.close();
    }

    /**
     * Metodo de ejemplo de uso de la clase.
     */
    public static void main(String[] args) {
        // Ruta específica a las credenciales
        String credentialsPath = "Virtual Assistant/utils/credentials.json";

        try {
            // Inicializar el analizador con la ruta a las credenciales
            GoogleNLPAnalyzer analyzer = new GoogleNLPAnalyzer(credentialsPath);

            String sampleTextSpanish = "Qué tiempo hace en barcelona";

            // Análisis de entidades
            System.out.println("\n***** ANÁLISIS *****\n");
            AnalyzeEntitiesResponse entityResponseEs = analyzer.analyzeEntities(sampleTextSpanish, "es");
            analyzer.printEntityAnalysisResults(entityResponseEs);

            // Cerrar el cliente cuando ya no sea necesario
            analyzer.close();

        } catch (IOException e) {
            System.err.println("Error al inicializar el cliente de Google Cloud Natural Language: " + e.getMessage());
            e.printStackTrace();
        }
    }
}