package cl.duoc.consumidor.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import cl.duoc.consumidor.config.RabbitMQConfig;
import cl.duoc.consumidor.dto.GuiaMessageDTO;
import cl.duoc.consumidor.models.GuiaProcesada;
import cl.duoc.consumidor.repositories.GuiaProcesadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que consume mensajes de la Cola de Exito de RabbitMQ
 * y los persiste en la tabla "guias_procesadas" de Oracle Cloud.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumidorService {

    private final RabbitTemplate rabbitTemplate;
    private final GuiaProcesadaRepository repository;

    /**
     * Consume TODOS los mensajes disponibles en la Cola de Exito
     * y los guarda en la tabla guias_procesadas.
     *
     * @return Lista de guias procesadas y guardadas en la BD.
     */
    public List<GuiaProcesada> consumirYGuardar() {
        List<GuiaProcesada> procesadas = new ArrayList<>();

        // receiveAndConvert() saca un mensaje de la cola (pull manual).
        // Cuando la cola esta vacia, retorna null y se detiene el ciclo.
        GuiaMessageDTO mensaje;
        while ((mensaje = rabbitTemplate.receiveAndConvert(
                RabbitMQConfig.COLA_EXITO,
                new ParameterizedTypeReference<GuiaMessageDTO>() {})) != null) {

            log.info(">>> Consumido de cola: Guia {} - {}", mensaje.getGuiaId(), mensaje.getNumeroGuia());

            GuiaProcesada procesada = GuiaProcesada.builder()
                    .guiaIdOriginal(mensaje.getGuiaId())
                    .numeroGuia(mensaje.getNumeroGuia())
                    .transportista(mensaje.getTransportista())
                    .fechaDespacho(mensaje.getFechaDespacho())
                    .estado(mensaje.getEstado())
                    .creadoEnOrigen(mensaje.getCreadoEn())
                    .procesadoEn(LocalDateTime.now())
                    .build();

            procesadas.add(repository.save(procesada));
        }

        log.info(">>> Total de guias procesadas en este ciclo: {}", procesadas.size());
        return procesadas;
    }

    /**
     * Retorna todas las guias procesadas almacenadas en la BD.
     */
    public List<GuiaProcesada> obtenerTodas() {
        return repository.findAll();
    }

    /**
     * Consume los mensajes de la Cola de Errores (para revision/monitoreo).
     */
    public List<GuiaMessageDTO> consumirErrores() {
        List<GuiaMessageDTO> errores = new ArrayList<>();

        GuiaMessageDTO mensaje;
        while ((mensaje = rabbitTemplate.receiveAndConvert(
                RabbitMQConfig.COLA_ERRORES,
                new ParameterizedTypeReference<GuiaMessageDTO>() {})) != null) {

            log.warn(">>> Error consumido: Guia {} - Error: {}",
                    mensaje.getNumeroGuia(), mensaje.getMensajeError());
            errores.add(mensaje);
        }

        return errores;
    }
}
