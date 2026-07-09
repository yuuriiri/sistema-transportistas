package cl.duoc.transportista.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import cl.duoc.transportista.dto.GuiaRequestDTO;
import cl.duoc.transportista.dto.GuiaResponseDTO;
import cl.duoc.transportista.models.Guia;
import cl.duoc.transportista.repositories.GuiaRepository;
import lombok.RequiredArgsConstructor;

/**
 * Implementacion del servicio para la entidad Guia.
 * Contiene la logica de negocio para operaciones CRUD y consultas personalizadas.
 *
 * MODIFICADO S8: Despues de crear una guia, se envian los datos a RabbitMQ
 * mediante el GuiaMessageProducer.
 */
@Service
@RequiredArgsConstructor
public class GuiaServiceImpl implements GuiaService {

    private final GuiaRepository guiaRepository;
    private final S3Service s3Service;
    private final PdfService pdfService;
    private final GuiaMessageProducer messageProducer; // NUEVO S8

    // Convierte la entidad en un DTO de respuesta
    private GuiaResponseDTO toDTO(Guia guia) {
        return GuiaResponseDTO.builder()
                .id(guia.getId())
                .numeroGuia(guia.getNumeroGuia())
                .transportista(guia.getTransportista())
                .fechaDespacho(guia.getFechaDespacho())
                .creadoEn(guia.getCreadoEn())
                .build();
    }

    @Override
    public GuiaResponseDTO crearGuia(GuiaRequestDTO request) {
        Guia guia = Guia.builder()
                .numeroGuia(request.getNumeroGuia())
                .transportista(request.getTransportista())
                .fechaDespacho(request.getFechaDespacho())
                .estado("CREADA")
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();

        Guia guiaGuardada = guiaRepository.save(guia);

        // NUEVO S8: Enviar datos de la guia a la cola de RabbitMQ
        messageProducer.enviarGuia(guiaGuardada);

        return toDTO(guiaGuardada);
    }

    @Override
    public List<GuiaResponseDTO> obtenerTodas() {
        return guiaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public GuiaResponseDTO obtenerPorId(Long id) {
        Guia guia = guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con ID: " + id));
        return toDTO(guia);
    }

    @Override
    public GuiaResponseDTO actualizarGuia(Long id, GuiaRequestDTO request) {
        Guia guia = guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con ID: " + id));

        guia.setNumeroGuia(request.getNumeroGuia());
        guia.setTransportista(request.getTransportista());
        guia.setFechaDespacho(request.getFechaDespacho());
        guia.setActualizadoEn(LocalDateTime.now());

        return toDTO(guiaRepository.save(guia));
    }

    @Override
    public void eliminarGuia(Long id) {
        Guia guia = guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        if (guia.getRutaS3() != null) {
            s3Service.eliminarGuia(guia.getRutaS3());
        }

        guiaRepository.deleteById(id);
    }

    @Override
    public List<GuiaResponseDTO> buscarPorTransportistaYFecha(String transportista, String fecha) {
        return guiaRepository.findByTransportistaAndFechaDespacho(transportista, fecha)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GuiaResponseDTO subirGuiaAS3(Long id, MultipartFile archivo) {
        Guia guia = guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        try {
            byte[] pdfBytes = pdfService.generarPdf(guia);
            String nombreArchivo = "guia-" + guia.getNumeroGuia() + ".pdf";

            String s3Key = s3Service.subirGuiaBytes(pdfBytes, guia.getFechaDespacho(), guia.getTransportista(), nombreArchivo);
            guia.setRutaS3(s3Key);
            guia.setEstado("SUBIDA");
            guia.setActualizadoEn(LocalDateTime.now());

            Guia guiaActualizada = guiaRepository.save(guia);

            // NUEVO S8: Tambien enviar a la cola cuando se sube a S3
            messageProducer.enviarGuia(guiaActualizada);

            return toDTO(guiaActualizada);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir guía: " + e.getMessage());
        }
    }

    @Override
    public byte[] descargarGuiaDesdS3(Long id) {
        Guia guia = guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        if (guia.getRutaS3() == null || guia.getRutaS3().isEmpty()) {
            throw new RuntimeException("La guía no tiene archivo en S3");
        }

        return s3Service.descargarGuia(guia.getRutaS3());
    }

    @Override
    public List<String> listarArchivosS3() {
        return s3Service.listarArchivosS3();
    }

    @Override
    public List<String> listarArchivosEFS() {
        return s3Service.listarArchivosEFS();
    }

    @Override
    public GuiaResponseDTO moverGuia(Long id, String nuevoTransportista, String nuevaFecha) {
        Guia guia = guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        if (guia.getRutaS3() == null) {
            throw new RuntimeException("La guía no tiene archivo en S3 para mover");
        }

        String nombreArchivo = "guia-" + guia.getNumeroGuia() + ".pdf";
        String nuevaRuta = s3Service.moverArchivo(guia.getRutaS3(), nuevaFecha, nuevoTransportista, nombreArchivo);

        guia.setTransportista(nuevoTransportista);
        guia.setFechaDespacho(nuevaFecha);
        guia.setRutaS3(nuevaRuta);
        guia.setActualizadoEn(LocalDateTime.now());

        return toDTO(guiaRepository.save(guia));
    }

    @Override
    public GuiaResponseDTO regenerarGuia(Long id, GuiaRequestDTO request) {
        Guia guia = guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        if (guia.getRutaS3() != null) {
            s3Service.eliminarGuia(guia.getRutaS3());
        }

        guia.setNumeroGuia(request.getNumeroGuia());
        guia.setTransportista(request.getTransportista());
        guia.setFechaDespacho(request.getFechaDespacho());
        guia.setActualizadoEn(LocalDateTime.now());

        try {
            byte[] pdfBytes = pdfService.generarPdf(guia);
            String nombreArchivo = "guia-" + guia.getNumeroGuia() + ".pdf";
            String s3Key = s3Service.subirGuiaBytes(pdfBytes, guia.getFechaDespacho(), guia.getTransportista(), nombreArchivo);
            guia.setRutaS3(s3Key);
            guia.setEstado("SUBIDA");
        } catch (IOException e) {
            throw new RuntimeException("Error al regenerar guía: " + e.getMessage());
        }

        return toDTO(guiaRepository.save(guia));
    }
}