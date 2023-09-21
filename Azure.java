import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Scanner;

import com.microsoft.azure.cognitiveservices.vision.faceapi.*;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.*;

public class Main {
    // Recognition model 3 was released in May 2020
    private static final String RECOGNITION_MODEL3 = RecognitionModel.RECOGNITION_03;
    
    public static FaceClient authenticate(String endpoint, String key) {
        return new FaceClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
    }
    
    private static List<DetectedFace> detectFaceRecognize(FaceClient faceClient, String url, String recognitionModel) {
        // Detect faces from image URL. Since only recognizing, use recognition model 1.
        // We use detection model 2 because we are not retrieving attributes.
        DetectFaceOptions options = new DetectFaceOptions()
                .withRecognitionModel(recognitionModel)
                .withDetectionModel(DetectionModel.DETECTION_02);
        return faceClient.detectFaces(url, options);
    }
    
    public static void findSimilar(FaceClient client, String recognitionModel) throws IOException {
        System.out.println("========Achar Similares========");
        System.out.println();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Insira o link da face base (links muito grandes podem causar erros):\n");
        String sourceImageFileName = scanner.nextLine();
        
        System.out.println("Insira o link das possíveis faces similares e digite FIM quando acabar (links muito grandes podem causar erros):\n");
        List<String> targetImageFileNames = new ArrayList<>();
        String aux;
        int i = 1;
        do {
            System.out.println("Imagem " + i + ":\n");
            aux = scanner.nextLine();
            if (!aux.equalsIgnoreCase("FIM")) {
                targetImageFileNames.add(aux);
            }
            i++;
        } while (!aux.equalsIgnoreCase("FIM"));
        
        List<UUID> targetFaceIds = new ArrayList<>();
        for (String targetImageFileName : targetImageFileNames) {
            // Detect faces from target image URL.
            List<DetectedFace> faces = detectFaceRecognize(client, targetImageFileName, recognitionModel);
            // Add detected faceId to list of UUIDs.
            targetFaceIds.add(faces.get(0).faceId());
        }
        
        // Detect faces from source image URL.
        List<DetectedFace> detectedFaces = detectFaceRecognize(client, sourceImageFileName, recognitionModel);
        System.out.println();
        
        // Find a similar face(s) in the list of IDs. Comparing only the first in the list for testing purposes.
        List<SimilarFace> similarResults = client.findSimilar(detectedFaces.get(0).faceId(), null, null, targetFaceIds);
        i = 1;
        for (SimilarFace similarResult : similarResults) {
            System.out.println("A imagem " + i + " com o FaceID:" + similarResult.faceId() + " é similar à imagem base com a confiança: " + similarResult.confidence() + ".");
            i++;
        }
        System.out.println();
    }
    
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        // Input your Azure Face API URL and subscription key
        System.out.println("Insira a URL da sua aplicação no Azure:\n");
        String urlServico = scanner.nextLine();
        System.out.println("Insira a chave da sua aplicação no Azure:\n");
        String chaveServico = scanner.nextLine();
        
        // Authenticate.
        FaceClient client = authenticate(urlServico, chaveServico);
        findSimilar(client, RECOGNITION_MODEL3);
    }
}
