package cat.politecnicllevant.convalidacions.repository;

import cat.politecnicllevant.convalidacions.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
