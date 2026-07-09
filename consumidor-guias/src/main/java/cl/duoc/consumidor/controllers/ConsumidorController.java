package cl.duoc.consumidor.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.consumidor.dto.GuiaMessageDTO;
import cl.duoc.consumidor.models.GuiaProcesada;
import cl.duoc.consumidor.services.ConsumidorService;
import lombok.RequiredArgsConstructor;

/**
 * Controlador del microservicio Consumidor.
 *
 * Endpoints:
 *   POST /api/consumidor/procesar  -> Consume mensajes de la Cola 1 y los guarda en la BD.
 *   GET  /api/consumidor/procesadas -> Lista todas las guias procesadas guardadas en la BD.
 *   POST /api/consumidor/errores   -> Consume mensajes de la Cola 2 (errores) para revision.
 */
@RestController
@RequestMapping("/api/consumidor")
@RequiredArgsConstructor
public class ConsumidorController {

    private final ConsumidorService consumidorService;

    /**
     * Endpoint que consume los mensajes de la Cola de Exito (cola.guias.exito)
     * y los guarda en la tabla guias_procesadas de Oracle Cloud.
     */
    @PostMapping("/procesar")
    public ResponseEntity<List<GuiaProcesada>> procesarCola() {
        List<GuiaProcesada> procesadas = consumidorService.consumirYGuardar();

        if (procesadas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(procesadas);
    }

    /**
     * Lista todas las guias que ya fueron procesadas y guardadas en la BD.
     */
    @GetMapping("/procesadas")
    public ResponseEntity<List<GuiaProcesada>> listarProcesadas() {
        return ResponseEntity.ok(consumidorService.obtenerTodas());
    }

    /**
     * Consume los mensajes de la Cola de Errores para revision.
     */
    @PostMapping("/errores")
    public ResponseEntity<List<GuiaMessageDTO>> verErrores() {
        List<GuiaMessageDTO> errores = consumidorService.consumirErrores();

        if (errores.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(errores);
    }
}
