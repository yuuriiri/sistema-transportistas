package cl.duoc.consumidor.models;

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

/**
 * Entidad que representa una guia procesada (consumida desde la cola de RabbitMQ).
 * Se guarda en la tabla "guias_procesadas", DISTINTA a la tabla "guias" del
 * microservicio productor, tal como exige el caso de la Semana 8.
 */
@Entity
@Table(name = "guias_procesadas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID original de la guia en el microservicio transportista
    private Long guiaIdOriginal;

    private String numeroGuia;
    private String transportista;
    private String fechaDespacho;
    private String estado;

    // Fecha en que fue creada originalmente
    private LocalDateTime creadoEnOrigen;

    // Fecha en que fue procesada (consumida de la cola)
    private LocalDateTime procesadoEn;
}
