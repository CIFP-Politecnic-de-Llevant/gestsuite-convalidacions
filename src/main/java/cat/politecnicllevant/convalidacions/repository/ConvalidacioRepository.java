package cat.politecnicllevant.convalidacions.repository;

import cat.politecnicllevant.convalidacions.model.Convalidacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConvalidacioRepository extends JpaRepository<Convalidacio, Long> {
}
