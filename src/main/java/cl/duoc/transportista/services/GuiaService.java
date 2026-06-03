package cl.duoc.transportista.services;

import java.util.List;

import cl.duoc.transportista.dto.GuiaRequestDTO;
import cl.duoc.transportista.dto.GuiaResponseDTO;

/*
 * Servicio para la entidad Guia.
 * Contiene métodos para realizar operaciones CRUD y consultas personalizadas. 
 */
public interface GuiaService {
    
    GuiaResponseDTO crearGuia(GuiaRequestDTO request); 

    List<GuiaResponseDTO> obtenerTodas(); 

    GuiaResponseDTO obtenerPorId(Long id);

    GuiaResponseDTO actualizarGuia(Long id, GuiaRequestDTO request);

    void eliminarGuia(Long id);

    List<GuiaResponseDTO> buscarPorTransportistaYFecha(String transportista, String fecha);
}
