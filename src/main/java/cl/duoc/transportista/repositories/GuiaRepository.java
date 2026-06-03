package cl.duoc.transportista.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.transportista.models.Guia;

/**
 * Repositorio para la entidad Guia.
 * Contiene métodos para realizar operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface GuiaRepository extends JpaRepository<Guia, Long> {
    
    List<Guia> findByTransportista(String transportista);

    List<Guia> findByFechaDespacho(String fechaDespacho);

    List<Guia> findByTransportistaAndFechaDespacho(String transportista, String fechaDespacho);
}
