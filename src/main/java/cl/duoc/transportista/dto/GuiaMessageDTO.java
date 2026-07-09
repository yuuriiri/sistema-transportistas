package cl.duoc.transportista.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa el mensaje enviado a las colas de RabbitMQ.
 * Contiene toda la informacion de la guia de despacho para ser
 * procesada por el microservicio consumidor.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaMessageDTO implements Serializable {

    private Long guiaId;
    private String numeroGuia;
    private String transportista;
    private String fechaDespacho;
    private String estado;
    private LocalDateTime creadoEn;

    // Solo se llena cuando el mensaje va a la cola de errores
    private String mensajeError;
}