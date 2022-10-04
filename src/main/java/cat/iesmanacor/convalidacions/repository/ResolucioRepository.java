package cat.iesmanacor.convalidacions.repository;

import cat.iesmanacor.convalidacions.model.Resolucio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResolucioRepository extends JpaRepository<Resolucio, Long> {

}
