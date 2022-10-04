package cat.iesmanacor.convalidacions.service;

import cat.iesmanacor.convalidacions.model.Item;
import cat.iesmanacor.convalidacions.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    public List<Item> findAllTitulacions(){
       List<Item> all = itemRepository.findAll();
       List<Item> excluded = new ArrayList<>();
       List<Item> result = new ArrayList<>();

       for(Item item : all){
           excluded.addAll(item.getComposa());
       }

        for(Item item : all){
            if(!excluded.contains(item)){
                result.add(item);
            }
        }

       return result;
    }

    public List<Item> findAllTitulacionsImpartidesAlCentre(){
        List<Item> all = itemRepository.findAllByImpartitAlCentreTrue();
        List<Item> excluded = new ArrayList<>();
        List<Item> result = new ArrayList<>();

        for(Item item : all){
            excluded.addAll(item.getComposa());
        }

        for(Item item : all){
            if(!excluded.contains(item)){
                result.add(item);
            }
        }

        return result;
    }

    public List<Item> findAll(){
        return itemRepository.findAll();
    }

    public Item getItemConvalidacioById(Long id){
        //Ha de ser findById i no getById perquè getById és Lazy
        return itemRepository.findById(id).get();
        //return itemRepository.getById(id);
    }

    public List<Item> getItemConvalidacioByNom(String nom){
        return itemRepository.findItemConvalidacioByNom(nom);
    }

    public List<Item> getItemConvalidacioByCodi(String codi){
        return itemRepository.findItemConvalidacioByCodi(codi);
    }

    @Transactional
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    public void esborrar(Item item){
        itemRepository.delete(item);
    }

}

