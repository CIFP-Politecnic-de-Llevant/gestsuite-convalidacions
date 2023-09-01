package cat.politecnicllevant.convalidacions.repository;

import cat.politecnicllevant.convalidacions.model.Resolucio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResolucioRepository extends JpaRepository<Resolucio, Long> {

}
