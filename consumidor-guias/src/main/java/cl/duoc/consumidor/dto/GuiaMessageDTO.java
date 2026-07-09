package cl.duoc.consumidor.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa el mensaje recibido desde las colas de RabbitMQ.
 * Debe tener la MISMA estructura que el DTO del productor para que
 * la deserializacion JSON funcione correctamente.
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
    private String mensajeError;
}
