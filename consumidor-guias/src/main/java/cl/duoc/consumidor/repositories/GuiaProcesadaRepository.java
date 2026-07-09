package cl.duoc.consumidor.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.consumidor.models.GuiaProcesada;

@Repository
public interface GuiaProcesadaRepository extends JpaRepository<GuiaProcesada, Long> {
}
