package cat.iesmanacor.convalidacions.controller;

import cat.iesmanacor.convalidacions.model.Convalidacio;
import cat.iesmanacor.convalidacions.model.Item;
import cat.iesmanacor.convalidacions.service.ConvalidacioService;
import cat.iesmanacor.convalidacions.service.ItemService;
import cat.iesmanacor.common.model.Notificacio;
import cat.iesmanacor.common.model.NotificacioTipus;
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
import java.util.List;

@RestController
public class ConvalidacioController {

    @Autowired
    private ConvalidacioService convalidacioService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private Gson gson;


    @GetMapping("/convalidacio/llistat")
    public ResponseEntity<List<Convalidacio>> getConvalidacions() {
        List<Convalidacio> convalidacions = convalidacioService.findAll();

        return new ResponseEntity<>(convalidacions, HttpStatus.OK);
    }

    @GetMapping("/convalidacio/{id}")
    public ResponseEntity<Convalidacio> getConvalidacionsById(@PathVariable("id") String idconvalidacio) {
        Convalidacio convalidacio = convalidacioService.getConvalidacioConvalidacioById(Long.valueOf(idconvalidacio));

        return new ResponseEntity<>(convalidacio, HttpStatus.OK);
    }

    @PostMapping("/convalidacio/desar")
    public ResponseEntity<Notificacio> desarConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idConvalidacio = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idConvalidacio = jsonObject.get("id").getAsLong();
        }

        Convalidacio convalidacio;

        if(idConvalidacio != null) {
            convalidacio = convalidacioService.getConvalidacioConvalidacioById(idConvalidacio);
            convalidacio.getOrigens().clear();
            convalidacio.getConvalida().clear();
        } else {
            convalidacio = new Convalidacio();
        }

        JsonArray origensJSON = jsonObject.get("origens").getAsJsonArray();

        for(JsonElement itemJSON: origensJSON){
            Long idItem = itemJSON.getAsJsonObject().get("id").getAsLong();
            Item item = itemService.getItemConvalidacioById(idItem);
            convalidacio.getOrigens().add(item);
        }

        JsonArray convalidaJSON = jsonObject.get("convalida").getAsJsonArray();

        for(JsonElement itemJSON: convalidaJSON){
            Long idItem = itemJSON.getAsJsonObject().get("id").getAsLong();
            Item item = itemService.getItemConvalidacioById(idItem);
            convalidacio.getConvalida().add(item);
        }

        convalidacioService.save(convalidacio);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Convalidació desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }



    @PostMapping("/convalidacio/esborrar")
    public ResponseEntity<Notificacio> esborrarConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idConvalidacio = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idConvalidacio = jsonObject.get("id").getAsLong();
        }


        if(idConvalidacio != null) {
            System.out.println("id conv"+idConvalidacio);
            Convalidacio convalidacio = convalidacioService.getConvalidacioConvalidacioById(idConvalidacio);
            if(convalidacio != null) {
                convalidacioService.esborrar(convalidacio);

                Notificacio notificacio = new Notificacio();
                notificacio.setNotifyMessage("Convalidació esborrada correctament");
                notificacio.setNotifyType(NotificacioTipus.SUCCESS);
                return new ResponseEntity<>(notificacio, HttpStatus.OK);
            }
        }

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("No s'ha pogut esborrar la convalidació");
        notificacio.setNotifyType(NotificacioTipus.ERROR);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);

    }


}