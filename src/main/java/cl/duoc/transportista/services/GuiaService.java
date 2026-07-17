package cl.duoc.transportista.services;

import java.util.List;

import cl.duoc.transportista.dto.GuiaRequestDTO;
import cl.duoc.transportista.dto.GuiaResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface GuiaService {

    GuiaResponseDTO crearGuia(GuiaRequestDTO request);
    List<GuiaResponseDTO> obtenerTodas();
    GuiaResponseDTO obtenerPorId(Long id);
    GuiaResponseDTO actualizarGuia(Long id, GuiaRequestDTO request);
    void eliminarGuia(Long id);
    List<GuiaResponseDTO> buscarPorTransportistaYFecha(String transportista, String fecha);
    GuiaResponseDTO subirGuiaAS3(Long id, MultipartFile archivo);
    byte[] descargarGuiaDesdS3(Long id);
    List<String> listarArchivosS3();
    List<String> listarArchivosEFS();
    GuiaResponseDTO moverGuia(Long id, String nuevoTransportista, String nuevaFecha);
    GuiaResponseDTO regenerarGuia(Long id, GuiaRequestDTO request);
}