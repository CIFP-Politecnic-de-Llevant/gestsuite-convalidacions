package cat.iesmanacor.convalidacions.controller;

import cat.iesmanacor.convalidacions.model.Categoria;
import cat.iesmanacor.convalidacions.service.CategoriaService;
import cat.iesmanacor.common.model.Notificacio;
import cat.iesmanacor.common.model.NotificacioTipus;
import com.google.gson.Gson;
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
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private Gson gson;

    @GetMapping("/categories")
    public ResponseEntity<List<Categoria>> getCategories() {

        List<Categoria> categories = categoriaService.findAll();

        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<Categoria> getCategoriaById(@PathVariable("id") String idcategoria) {

        Categoria categoria = categoriaService.getCategoriaConvalidacioById(Long.valueOf(idcategoria));

        return new ResponseEntity<>(categoria, HttpStatus.OK);
    }

    @PostMapping("/categoria/desar")
    public ResponseEntity<Notificacio> desarCategoriaConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idCategoria = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idCategoria = jsonObject.get("id").getAsLong();
        }

        String nom = jsonObject.get("nom").getAsString();

        Categoria categoria;

        if(idCategoria != null) {
            categoria = categoriaService.getCategoriaConvalidacioById(idCategoria);
        } else {
            categoria = new Categoria();
        }

        categoria.setNom(nom);

        categoriaService.save(categoria);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Categoria desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/categoria/esborrar")
    public ResponseEntity<Notificacio> esborrarCategoriaConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idCategoria = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idCategoria = jsonObject.get("id").getAsLong();
        }


        if(idCategoria != null) {
            Categoria categoria = categoriaService.getCategoriaConvalidacioById(idCategoria);
            categoriaService.esborrar(categoria);

            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Categoria esborrada correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("No s'ha pogut esborrar la categoria");
        notificacio.setNotifyType(NotificacioTipus.ERROR);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);

    }



}