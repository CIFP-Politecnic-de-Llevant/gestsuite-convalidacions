package cat.politecnicllevant.convalidacions.repository;

import cat.politecnicllevant.convalidacions.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findAllByCursAcademic(Long cursAcademic);
}
