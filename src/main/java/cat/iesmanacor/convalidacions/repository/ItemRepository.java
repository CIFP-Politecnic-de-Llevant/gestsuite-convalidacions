package cat.iesmanacor.convalidacions.repository;

import cat.iesmanacor.convalidacions.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByImpartitAlCentreTrue();
    List<Item> findItemConvalidacioByNom(String nom);
    List<Item> findItemConvalidacioByCodi(String codi);
}
