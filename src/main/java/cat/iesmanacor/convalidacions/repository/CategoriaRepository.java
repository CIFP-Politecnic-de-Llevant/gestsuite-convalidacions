package cat.iesmanacor.convalidacions.repository;

import cat.iesmanacor.convalidacions.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
