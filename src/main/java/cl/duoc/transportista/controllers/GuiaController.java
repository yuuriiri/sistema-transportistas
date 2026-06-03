package cl.duoc.transportista.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.transportista.dto.GuiaRequestDTO;
import cl.duoc.transportista.dto.GuiaResponseDTO;
import cl.duoc.transportista.services.GuiaService;
import lombok.RequiredArgsConstructor;

/*
    * Controlador para la entidad Guia.
    * Contiene endpoints para realizar operaciones CRUD y consultas personalizadas.
*/
@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
public class GuiaController {
    
    private final GuiaService guiaService;

    @PostMapping
    public ResponseEntity<GuiaResponseDTO> crearGuia(@RequestBody GuiaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guiaService.crearGuia(request));
    }

    @GetMapping
    public ResponseEntity<List<GuiaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(guiaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuiaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(guiaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuiaResponseDTO> actualizarGuia(
            @PathVariable Long id,
            @RequestBody GuiaRequestDTO request) {
        return ResponseEntity.ok(guiaService.actualizarGuia(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGuia(@PathVariable Long id) {
        guiaService.eliminarGuia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<GuiaResponseDTO>> buscarPorTransportistaYFecha(
            @RequestParam String transportista,
            @RequestParam String fecha) {
        return ResponseEntity.ok(guiaService.buscarPorTransportistaYFecha(transportista, fecha));
    }
}
