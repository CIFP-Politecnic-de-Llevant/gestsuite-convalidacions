package cat.politecnicllevant.convalidacions.repository;

import cat.politecnicllevant.convalidacions.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

}
