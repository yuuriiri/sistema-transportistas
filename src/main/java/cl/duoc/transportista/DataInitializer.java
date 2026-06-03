package cl.duoc.transportista;

import cl.duoc.transportista.models.Guia;
import cl.duoc.transportista.repositories.GuiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GuiaRepository guiaRepository;

    @Override
    public void run(String... args) throws Exception {

        if (guiaRepository.count() == 0) {

            guiaRepository.save(Guia.builder()
                    .numeroGuia("G-001")
                    .transportista("Transportes del Sur")
                    .fechaDespacho("2025-06-01")
                    .estado("CREADA")
                    .rutaS3(null)
                    .creadoEn(LocalDateTime.now())
                    .actualizadoEn(LocalDateTime.now())
                    .build());

            guiaRepository.save(Guia.builder()
                    .numeroGuia("G-002")
                    .transportista("Logística Norte")
                    .fechaDespacho("2025-06-02")
                    .estado("SUBIDA")
                    .rutaS3("s3://transportista-bucket/guias/G-002.pdf")
                    .creadoEn(LocalDateTime.now())
                    .actualizadoEn(LocalDateTime.now())
                    .build());

            guiaRepository.save(Guia.builder()
                    .numeroGuia("G-003")
                    .transportista("Transportes del Sur")
                    .fechaDespacho("2025-06-03")
                    .estado("CREADA")
                    .rutaS3(null)
                    .creadoEn(LocalDateTime.now())
                    .actualizadoEn(LocalDateTime.now())
                    .build());

            System.out.println(">>> DataInitializer: datos de prueba cargados correctamente.");
        } else {
            System.out.println(">>> DataInitializer: ya existen datos, no se cargaron duplicados.");
        }
    }
}