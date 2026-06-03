package cl.duoc.transportista.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la solicitud de creación o actualización de una guía de despacho.
 * No interactúa directamente con la base de datos, sino que se utiliza para transferir datos entre el cliente y el servidor.
 * Contiene los campos necesarios para crear o actualizar una guía
 */

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaRequestDTO {
    private String numeroGuia;
    private String transportista; 
    private String fechaDespacho;
}
