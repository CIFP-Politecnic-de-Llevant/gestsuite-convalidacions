package cat.iesmanacor.convalidacions.service;

import cat.iesmanacor.convalidacions.model.Convalidacio;
import cat.iesmanacor.convalidacions.model.Item;
import cat.iesmanacor.convalidacions.model.Resolucio;
import cat.iesmanacor.convalidacions.model.Solicitud;
import cat.iesmanacor.convalidacions.repository.ConvalidacioRepository;
import cat.iesmanacor.convalidacions.repository.ResolucioRepository;
import cat.iesmanacor.convalidacions.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SolicitudService {
    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private ResolucioRepository resolucioRepository;

    @Autowired
    private ConvalidacioRepository convalidacioRepository;

    public List<Solicitud> findAll(){
       return solicitudRepository.findAll();
    }

    public Solicitud getSolicitudConvalidacioById(Long id){
        //Ha de ser findById i no getById perquè getById és Lazy
        return solicitudRepository.findById(id).get();
        //return solicitudConvalidacioRepository.getById(id);
    }

    @Transactional
    public Solicitud save(Solicitud solicitud) {
        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public void esborrarEstudisOrigenSolicitud(Solicitud solicitud) {
        Set<Item> estudisOrigen = new HashSet<>(solicitudRepository.getById(solicitud.getIdsolicitud()).getEstudisOrigen());
        for (Item estudiOrigen : estudisOrigen) {
            solicitudRepository.getById(solicitud.getIdsolicitud()).getEstudisOrigen().remove(estudiOrigen);
        }
    }

    @Transactional
    public void esborrarResolucionsSolicitud(Solicitud solicitud) {
        Set<Resolucio> resolucions = new HashSet<>(solicitudRepository.getById(solicitud.getIdsolicitud()).getResolucions());
        for (Resolucio resolucio: resolucions) {
            solicitudRepository.getById(solicitud.getIdsolicitud()).getResolucions().remove(resolucio);
            resolucioRepository.delete(resolucio);
        }
    }

    @Transactional
    public void esborrar(Solicitud solicitud){
        solicitudRepository.delete(solicitud);
    }

    public List<Item> calculaConvalidacions(Solicitud solicitud){
        List<Item> result = new ArrayList<>();

        if (solicitud != null && solicitud.getEstudisOrigen() != null && solicitud.getEstudisOrigen().size() > 0 && solicitud.getEstudisEnCurs() != null) {
            List<Item> itemsCursats = new ArrayList<>(solicitud.getEstudisOrigen());
            Item estudisEnCurs = solicitud.getEstudisEnCurs();

            List<Convalidacio> convalidacions = convalidacioRepository.findAll();

            //1- Emplenem tots els items amb les composicions d'aquests
            List<Item> itemsCursatsAll = new ArrayList<>();
            for (Item item : itemsCursats) {
                itemsCursatsAll.add(item);
                itemsCursatsAll.addAll(item.getComposa());
            }

            //2- Emplenem els items cursats amb totes les convalidacions possibles
            boolean needUpdate = true;
            while (needUpdate) {
                needUpdate = false;
                for (Convalidacio convalidacio : convalidacions) {
                    List<Item> origens = new ArrayList<>(convalidacio.getOrigens());
                    List<Item> convalida = new ArrayList<>(convalidacio.getConvalida());

                    //Comprovem que la convalidació conté tots els ítems cursats
                    boolean hasAllOrigens = true;
                    for (Item origen : origens) {
                        if (!itemsCursatsAll.contains(origen)) {
                            hasAllOrigens = false;
                            break;
                        }
                    }

                    //Si té tots els orígens que exigeix la convalidació, mirem si no està inclòs dins els
                    //ítems cursats. Si no està inclòs, fem una altra passada recursiva
                    //Ex: Sistemes informàtics --> UC0XXXX --> sistemes informàtics (ATUREM)
                    if (hasAllOrigens) {
                        for (Item desti : convalida) {
                            if (!itemsCursatsAll.contains(desti)) {
                                itemsCursatsAll.add(desti);
                                needUpdate = true;
                            }
                        }
                    }
                }
            }

            //2.1 emplenem també FOL i Empresa
            System.out.println("Comprovació FOL i Empresa");
            for(Item estudiEnCurs: estudisEnCurs.getComposa()){
                for(Item estudiPrevi: solicitud.getEstudisOrigen()) {
                    for(Item modulPrevi: estudiPrevi.getComposa()) {
                        //System.out.println("Estudi" + estudiEnCurs.getNom() + " ---- " + modulPrevi.getNom());
                        if (
                                estudiEnCurs.getNom().equalsIgnoreCase(modulPrevi.getNom()) &&
                                (
                                        estudiEnCurs.getNom().equalsIgnoreCase("Formació i orientació laboral") ||
                                        estudiEnCurs.getNom().equalsIgnoreCase("Formación y orientación laboral") ||
                                        estudiEnCurs.getNom().equalsIgnoreCase("Empresa i iniciativa emprenedora") ||
                                        estudiEnCurs.getNom().equalsIgnoreCase("Empresa e iniciativa emprendedora")
                                )
                        ) {
                            System.out.println("Afegim FOL i/o empresa. Estudi" + estudiEnCurs.getNom() + " ---- " + modulPrevi.getNom());
                            result.add(estudiEnCurs);
                        }
                    }
                }
            }

            //3- Emplenem tots els items amb les composicions d'aquests
            List<Item> estudisEnCursAll = new ArrayList<>();
            estudisEnCursAll.add(estudisEnCurs);
            estudisEnCursAll.addAll(estudisEnCurs.getComposa());


            //4- Filtrem els resultats i només retornem els que són dels estudis en curs
            for (Item item : itemsCursatsAll) {
                if (estudisEnCursAll.contains(item)) {
                    result.add(item);
                }
            }
        }

        return result;
    }

}

