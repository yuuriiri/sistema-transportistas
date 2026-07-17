package cl.duoc.transportista.services;

import cl.duoc.transportista.config.RabbitMQConfig;
import cl.duoc.transportista.dto.GuiaMessageDTO;
import cl.duoc.transportista.models.Guia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Componente productor: envia mensajes SOLO a la cola principal.
 * Si el envio falla, registra el error mediante log.
 * La DLQ es responsabilidad de RabbitMQ, no del productor.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuiaMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void enviarGuia(Guia guia) {
        GuiaMessageDTO mensaje = GuiaMessageDTO.builder()
                .guiaId(guia.getId())
                .numeroGuia(guia.getNumeroGuia())
                .transportista(guia.getTransportista())
                .fechaDespacho(guia.getFechaDespacho())
                .estado(guia.getEstado())
                .creadoEn(guia.getCreadoEn())
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    mensaje
            );
            log.info(">>> Mensaje enviado a cola principal - Guia: {}", guia.getNumeroGuia());

        } catch (Exception e) {
            log.error(">>> Error al enviar mensaje a la cola: {} - Guia: {}",
                    e.getMessage(), guia.getNumeroGuia());
        }
    }
}