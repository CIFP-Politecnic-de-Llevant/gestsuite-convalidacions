package cat.politecnicllevant.convalidacions.controller;

import cat.politecnicllevant.convalidacions.model.Categoria;
import cat.politecnicllevant.convalidacions.model.Item;
import cat.politecnicllevant.convalidacions.service.CategoriaService;
import cat.politecnicllevant.convalidacions.service.ItemService;
import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private Gson gson;

    @GetMapping({"/titulacions","/public/titulacions"})
    public ResponseEntity<List<Item>> getTitulacionsPrincipals() {
        List<Item> items = itemService.findAllTitulacions();
        items = items.stream().filter(i->i.getComposa().size()>0).collect(Collectors.toList());

        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping({"/titulacions/loe"})
    public ResponseEntity<List<Item>> getTitulacionsLoe() {
        List<Item> items = itemService.findAll();
        items = items.stream().filter(i->i.getCategoria().getIdcategoria() == 1).toList();

        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping({"/titulacions/all","/public/titulacions/all"})
    public ResponseEntity<List<Item>> getTitulacionsTotes() {
        List<Item> items = itemService.findAllTitulacions();

        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping({"/titulacionscentre","/public/titulacionscentre"})
    public ResponseEntity<List<Item>> getTitulacionsImpartidesAlCentre() {
        List<Item> items = itemService.findAllTitulacionsImpartidesAlCentre();
        items = items.stream().filter(i->i.getComposa().size()>0).collect(Collectors.toList());

        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping({"/items","/public/items"})
    public ResponseEntity<List<Item>> getItems() {

        List<Item> items = itemService.findAll();

        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @PostMapping("/titulacio/desar")
    public ResponseEntity<Notificacio> desarTitulacioConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idTitulacio = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idTitulacio = jsonObject.get("id").getAsLong();
        }

        String codi = "";
        if(jsonObject.get("codi")!=null && !jsonObject.get("codi").isJsonNull()) {
            codi = jsonObject.get("codi").getAsString();
        }

        String nom = jsonObject.get("nom").getAsString();

        String nomOriginal = "";
        if(jsonObject.get("nomOriginal")!=null && !jsonObject.get("nomOriginal").isJsonNull()) {
            nomOriginal = jsonObject.get("nomOriginal").getAsString();
        }
        Boolean impartitAlCentre = jsonObject.get("impartitAlCentre").getAsBoolean();

        String idCategoria = jsonObject.get("categoria").getAsJsonObject().get("value").getAsString();
        Categoria categoria = categoriaService.getCategoriaConvalidacioById(Long.valueOf(idCategoria));

        List<Item> itemsFill = new ArrayList<>();
        JsonArray itemsComposicioJSON = jsonObject.get("composa").getAsJsonArray();
        for(JsonElement itemComposicio: itemsComposicioJSON){
            String codiItem = itemComposicio.getAsJsonObject().get("codi").getAsString();
            String nomItem = itemComposicio.getAsJsonObject().get("nom").getAsString();
            String nomOriginalItem = itemComposicio.getAsJsonObject().get("nomOriginal").getAsString();
            Boolean impartitAlCentreItem = jsonObject.get("impartitAlCentre").getAsBoolean();


            String idCategoriaItem = itemComposicio.getAsJsonObject().get("categoria").getAsJsonObject().get("value").getAsString();
            Categoria categoriaItem = categoriaService.getCategoriaConvalidacioById(Long.valueOf(idCategoriaItem));

            Long idFill = null;
            if (itemComposicio.getAsJsonObject().get("id") != null && !itemComposicio.getAsJsonObject().get("id").isJsonNull()){
                idFill = itemComposicio.getAsJsonObject().get("id").getAsLong();
            }

            Item itemFill;

            if(idFill != null) {
                itemFill = itemService.getItemConvalidacioById(idFill);
            } else {
                itemFill = new Item();
            }
            itemFill.setCodi(codiItem);
            itemFill.setNom(nomItem);
            itemFill.setNomOriginal(nomOriginalItem);
            itemFill.setImpartitAlCentre(impartitAlCentreItem);
            itemFill.setCategoria(categoriaItem);

            Item i = itemService.save(itemFill);

            itemsFill.add(i);

        }

        Item titulacio;

        if(idTitulacio != null) {
            titulacio = itemService.getItemConvalidacioById(idTitulacio);
        } else {
            titulacio = new Item();
        }

        titulacio.setCodi(codi);
        titulacio.setNom(nom);
        titulacio.setNomOriginal(nomOriginal);
        titulacio.setImpartitAlCentre(impartitAlCentre);
        titulacio.setCategoria(categoria);

        titulacio.getComposa().clear();
        for(Item item :itemsFill){
            titulacio.getComposa().add(item);
        }

        itemService.save(titulacio);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Titulació desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }


    @GetMapping("/titulacio/{id}")
    public ResponseEntity<Item> getTitulacioById(@PathVariable("id") String idtitulacio) {

        Item titulacio = itemService.getItemConvalidacioById(Long.valueOf(idtitulacio));

        return new ResponseEntity<>(titulacio, HttpStatus.OK);
    }

    @GetMapping("/item/getbynom/{nom}")
    public ResponseEntity<List<Item>> getItemByNom(@PathVariable("nom") String nom) {

        List<Item> item = itemService.getItemConvalidacioByNom(nom);

        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @GetMapping("/item/getbycodi/{codi}")
    public ResponseEntity<List<Item>> getItemByCodi(@PathVariable("codi") String codi) {

        List<Item> item = itemService.getItemConvalidacioByCodi(codi);

        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @PostMapping("/titulacio/esborrar")
    public ResponseEntity<Notificacio> esborrarItemConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            Long idTitulacio = null;
            if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()) {
                idTitulacio = jsonObject.get("id").getAsLong();
            }


            if (idTitulacio != null) {
                Item item = itemService.getItemConvalidacioById(idTitulacio);
                itemService.esborrar(item);

                Notificacio notificacio = new Notificacio();
                notificacio.setNotifyMessage("Titulació esborrada correctament");
                notificacio.setNotifyType(NotificacioTipus.SUCCESS);
                return new ResponseEntity<>(notificacio, HttpStatus.OK);
            }

            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("No s'ha pogut esborrar la titulació");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }


}