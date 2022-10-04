package cat.iesmanacor.convalidacions.service;

import cat.iesmanacor.convalidacions.model.Categoria;
import cat.iesmanacor.convalidacions.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria getCategoriaConvalidacioById(Long id){
        //Ha de ser findById i no getById perquè getById és Lazy
        return categoriaRepository.findById(id).get();
        //return itemRepository.getById(id);
    }

    public List<Categoria> findAll(){
        return categoriaRepository.findAll();
    }

    @Transactional
    public void esborrar(Categoria categoria){
        categoriaRepository.delete(categoria);
    }
}

