package cl.duoc.transportista.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Clase que representa la entidad "Guia" en el sistema de transporte.
 * Esta clase se mapea a la tabla "guias" en la base de datos.
 * Contiene información relevante sobre una guía de despacho, como su número,
 * transportista, fecha de despacho, ruta del archivo en S3, estado, y timestamps
 * para seguimiento de creación y actualización.
 */

@Entity 
@Table(name = "guias")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroGuia;
    private String transportista; 
    private String fechaDespacho; 
    private String rutaS3; 
    private String estado; 

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    
}
