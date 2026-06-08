package cl.duoc.transportista.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // Guarda temporalmente en EFS y luego sube a S3
    public String subirGuia(MultipartFile archivo, String fecha, String transportista) throws IOException {
        // Guardar temporalmente en EFS
        String efsPath = "/mnt/efs/" + fecha + "/" + transportista;
        File directorio = new File(efsPath);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        Path archivoTemporal = Paths.get(efsPath, archivo.getOriginalFilename());
        Files.write(archivoTemporal, archivo.getBytes());

        // Sube a S3 con ruta organizada por fecha/transportista
        String s3Key = fecha + "/" + transportista + "/" + archivo.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build(),
                RequestBody.fromBytes(archivo.getBytes())
        );

        // Elimina archivo temporal del EFS
        Files.deleteIfExists(archivoTemporal);

        return s3Key;
    }

    // Descargar archivo desde S3
    public byte[] descargarGuia(String s3Key) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build()
            );
            return response.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar la guía desde S3: " + e.getMessage());
        }
    }

    // Eliminar archivo de S3
    public void eliminarGuia(String s3Key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build()
        );
    }

    public String subirGuiaBytes(byte[] contenido, String fecha, String transportista, String nombreArchivo) throws IOException {
        // Guardar temporalmente en EFS
        String efsPath = "/mnt/efs/" + fecha + "/" + transportista;
        File directorio = new File(efsPath);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        Path archivoTemporal = Paths.get(efsPath, nombreArchivo);
        Files.write(archivoTemporal, contenido);

        // Subir a S3
        String s3Key = fecha + "/" + transportista + "/" + nombreArchivo;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build(),
                RequestBody.fromBytes(contenido)
        );

        // Eliminar temporal del EFS
        Files.deleteIfExists(archivoTemporal);

        return s3Key;
    }
}