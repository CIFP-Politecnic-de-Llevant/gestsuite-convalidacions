package cat.politecnicllevant.convalidacions.service;

import cat.politecnicllevant.convalidacions.model.Resolucio;
import cat.politecnicllevant.convalidacions.repository.ResolucioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResolucioService {
    @Autowired
    private ResolucioRepository resolucioRepository;

    public List<Resolucio> findAll(){
       return resolucioRepository.findAll();
    }

    public Resolucio getResolucioConvalidacioById(Long id){
        //Ha de ser findById i no getById perquè getById és Lazy
        return resolucioRepository.findById(id).get();
        //return solicitudConvalidacioRepository.getById(id);
    }

    @Transactional
    public Resolucio save(Resolucio resolucio) {
        return resolucioRepository.save(resolucio);
    }

    @Transactional
    public void esborrar(Resolucio resolucio){
        resolucioRepository.delete(resolucio);
    }

}

