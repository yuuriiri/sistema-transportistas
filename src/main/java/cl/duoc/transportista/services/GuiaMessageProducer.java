package cl.duoc.transportista.services;

import cl.duoc.transportista.config.RabbitMQConfig;
import cl.duoc.transportista.dto.GuiaMessageDTO;
import cl.duoc.transportista.models.Guia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Componente productor que transmite mensajes a ambas colas de RabbitMQ.
 *
 * Logica:
 *   1. Intenta enviar el mensaje a la Cola de Exito (cola.guias.exito).
 *   2. Si falla, envia el mensaje a la Cola de Errores (cola.guias.errores)
 *      incluyendo el mensaje de error.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuiaMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Envia los datos de una guia a la cola de exito.
     * Si falla, redirige a la cola de errores.
     */
    public void enviarGuia(Guia guia) {
        GuiaMessageDTO mensaje = buildMensaje(guia);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_EXITO,
                    mensaje
            );
            log.info(">>> Mensaje enviado a Cola de Exito - Guia: {}", guia.getNumeroGuia());

        } catch (Exception e) {
            log.error(">>> Error al enviar a Cola de Exito: {}. Redirigiendo a Cola de Errores.", e.getMessage());
            enviarAColaErrores(mensaje, e.getMessage());
        }
    }

    /**
     * Simula un error: envia la guia directamente a la Cola de Errores (DLQ).
     * Se usa para demostrar el funcionamiento de la cola de errores.
     */
    public void simularError(Guia guia, String motivoError) {
        GuiaMessageDTO mensaje = buildMensaje(guia);
        enviarAColaErrores(mensaje, motivoError);
    }

    /**
     * Construye el DTO del mensaje a partir de la entidad Guia.
     */
    private GuiaMessageDTO buildMensaje(Guia guia) {
        return GuiaMessageDTO.builder()
                .guiaId(guia.getId())
                .numeroGuia(guia.getNumeroGuia())
                .transportista(guia.getTransportista())
                .fechaDespacho(guia.getFechaDespacho())
                .estado(guia.getEstado())
                .creadoEn(guia.getCreadoEn())
                .build();
    }

    /**
     * Envia un mensaje directamente a la cola de errores.
     */
    private void enviarAColaErrores(GuiaMessageDTO mensaje, String error) {
        try {
            mensaje.setMensajeError(error);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_ERROR,
                    mensaje
            );
            log.warn(">>> Mensaje enviado a Cola de Errores - Guia: {}", mensaje.getNumeroGuia());

        } catch (Exception ex) {
            log.error(">>> ERROR CRITICO: No se pudo enviar a ninguna cola. Guia: {} - Error: {}",
                    mensaje.getNumeroGuia(), ex.getMessage());
        }
    }
}