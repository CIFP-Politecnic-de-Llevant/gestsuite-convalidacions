package cat.iesmanacor.convalidacions.service;

import cat.iesmanacor.convalidacions.model.Convalidacio;
import cat.iesmanacor.convalidacions.repository.ConvalidacioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConvalidacioService {
    @Autowired
    private ConvalidacioRepository convalidacioRepository;

    public List<Convalidacio> findAll(){
       return convalidacioRepository.findAll();
    }


    public Convalidacio getConvalidacioConvalidacioById(Long id){
        //Ha de ser findById i no getById perquè getById és Lazy
        return convalidacioRepository.findById(id).get();
        //return itemRepository.getById(id);
    }

    @Transactional
    public Convalidacio save(Convalidacio convalidacio) {
        return convalidacioRepository.save(convalidacio);
    }

    @Transactional
    public void esborrar(Convalidacio convalidacio){
        convalidacioRepository.delete(convalidacio);
    }

}

