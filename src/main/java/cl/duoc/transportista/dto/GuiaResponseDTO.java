package cl.duoc.transportista.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la respuesta de una guía de despacho.
 * Se utiliza para transferir datos del servidor al cliente.
 * No interactúa directamente con la base de datos, sino que se utiliza para presentar información relevante sobre una guía de despacho al cliente.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaResponseDTO {
    private Long id;
    private String numeroGuia;
    private String transportista;
    private String fechaDespacho;
    private LocalDateTime creadoEn;
    // rutaS3 y estado no se incluyen en la respuesta para el cliente, ya que son detalles internos que no necesitan ser expuestos.
}
