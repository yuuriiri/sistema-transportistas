package cl.duoc.transportista.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cl.duoc.transportista.dto.GuiaRequestDTO;
import cl.duoc.transportista.dto.GuiaResponseDTO;
import cl.duoc.transportista.models.Guia;
import cl.duoc.transportista.repositories.GuiaRepository;
import lombok.RequiredArgsConstructor;
/*
    * Implementación del servicio para la entidad Guia.
    * Contiene la lógica de negocio para realizar operaciones CRUD y consultas personalizadas.
*/
@Service 
@RequiredArgsConstructor
public class GuiaServiceImpl implements GuiaService {
    
    private final GuiaRepository guiaRepository;

    //Convierte la entidad en un DTO de respuesta
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
                .estado("CREADA") // Estado inicial de la guía
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();

                return toDTO(guiaRepository.save(guia));
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
        guiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con ID: " + id));
        guiaRepository.deleteById(id);
    }

    @Override
    public List<GuiaResponseDTO> buscarPorTransportistaYFecha(String transportista, String fecha) {
        return guiaRepository.findByTransportistaAndFechaDespacho(transportista, fecha)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}
