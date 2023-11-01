package cat.politecnicllevant.convalidacions.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.convalidacions.dto.core.gestib.GrupDto;
import cat.politecnicllevant.convalidacions.dto.core.gestib.UsuariDto;
import cat.politecnicllevant.convalidacions.dto.google.FitxerBucketDto;
import cat.politecnicllevant.convalidacions.model.*;
import cat.politecnicllevant.convalidacions.pdf.service.PdfService;
import cat.politecnicllevant.convalidacions.restclient.CoreRestClient;
import cat.politecnicllevant.convalidacions.service.ConvalidacioService;
import cat.politecnicllevant.convalidacions.service.ItemService;
import cat.politecnicllevant.convalidacions.service.ResolucioService;
import cat.politecnicllevant.convalidacions.service.SolicitudService;

//import cat.politecnicllevant.pdf.service.PdfService;
import cat.politecnicllevant.convalidacions.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static org.bouncycastle.asn1.cms.CMSAttributes.contentType;

@RestController
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ConvalidacioService convalidacioService;

    @Autowired
    private ResolucioService resolucioService;

    @Autowired
    private CoreRestClient coreRestClient;

    @Autowired
    private PdfService pdfService;

    //@Autowired
    //private GMailService gMailService;

    //@Autowired
    //private FitxerBucketService fitxerBucketService;

    @Autowired
    private Gson gson;

    @Value("${gc.storage.bucketnamedata}")
    private String bucketName;

    @Value("${gc.storage.convalidacions.path-files}")
    private String bucketPathFiles;

    @Value("${centre.convalidacions.notificar-resolucions}")
    private String notificarResolucionsEmails;

    @Value("${microservice.core.address}")
    private String coreAddress;



    @GetMapping("/solicitud/llistat")
    public ResponseEntity<List<Solicitud>> getSolicituds() {
        List<Solicitud> solicituds = solicitudService.findAll();

        return new ResponseEntity<>(solicituds, HttpStatus.OK);
    }

    @GetMapping("/solicitud/{id}")
    public ResponseEntity<Solicitud> getConvalidacionsById(@PathVariable("id") String idsolicitud) throws IOException, GeneralSecurityException {
        Solicitud solicitudConvalidacio = solicitudService.getSolicitudConvalidacioById(Long.valueOf(idsolicitud));

        for (Long idFitxerBucket : solicitudConvalidacio.getFitxersAlumne()) {

            ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.getFitxerBucketById(idFitxerBucket);
            FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

            if (fitxerBucket != null) {
                JsonObject jsonFitxerBucket = new JsonObject();
                jsonFitxerBucket.addProperty("idfitxer", fitxerBucket.getIdfitxer());
                jsonFitxerBucket.addProperty("nom", fitxerBucket.getNom());
                jsonFitxerBucket.addProperty("bucket", fitxerBucket.getBucket());
                jsonFitxerBucket.addProperty("path", fitxerBucket.getPath());

                ResponseEntity<String> urlResponse = coreRestClient.generateSignedURL(jsonFitxerBucket.toString());
                String url = urlResponse.getBody();

                fitxerBucket.setUrl(url);
            }
        }

        return new ResponseEntity<>(solicitudConvalidacio, HttpStatus.OK);
    }

    @PostMapping({"/solicitud/calculconvalidacio", "/public/solicitud/calculconvalidacio"})
    public ResponseEntity<List<Item>> calculaConvalidacions(@RequestBody Solicitud solicitud) {

        List<Item> result = solicitudService.calculaConvalidacions(solicitud);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/solicitud/changeEstat")
    public ResponseEntity<Notificacio> canviarEstat(@RequestBody String json) {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idSolicitud = jsonObject.get("id").getAsLong();
        String estat = jsonObject.get("estat").getAsString();

        Solicitud solicitud = solicitudService.getSolicitudConvalidacioById(idSolicitud);

        if(estat.equals("pendent")) {
            solicitud.setEstat(SolcititudEstat.PENDENT_RESOLUCIO);
        } else if(estat.equals("standby")) {
            solicitud.setEstat(SolcititudEstat.STAND_BY);
        }
        solicitudService.save(solicitud);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Sol·licitud desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }


    @PostMapping("/solicitud/desar")
    public ResponseEntity<Notificacio> desarSolicitud(@RequestBody String json, HttpServletRequest request) throws Exception {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idSolicitud = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()) {
            idSolicitud = jsonObject.get("id").getAsLong();
        }

        Solicitud solicitud;

        if (idSolicitud != null) {
            solicitud = solicitudService.getSolicitudConvalidacioById(idSolicitud);
            System.out.println("Esborrant estudis origen i resolucions");
            solicitudService.esborrarEstudisOrigenSolicitud(solicitud);
            solicitudService.esborrarResolucionsSolicitud(solicitud);
        } else {
            solicitud = new Solicitud();
            //Introduim només una vegada la data de creació de la sol·licitud
            Date ara = new Date();
            solicitud.setDataCreacio(ara);
        }

        //Alumne
        JsonObject alumneJSON = jsonObject.get("alumne").getAsJsonObject();
        String idAlumne = alumneJSON.get("id").getAsString();

        ResponseEntity<UsuariDto> alumneResponse = coreRestClient.getProfile(idAlumne);
        UsuariDto alumne = alumneResponse.getBody();
        //Usuari alumne = usuariService.findById(alumneJSON.get("id").getAsLong());
        solicitud.setAlumne(alumne.getIdusuari());

        //Estudis Origen
        JsonArray estudisEnCursJSON = jsonObject.get("estudisOrigen").getAsJsonArray();

        for (JsonElement itemJSON : estudisEnCursJSON) {
            Long idItem = itemJSON.getAsJsonObject().get("id").getAsLong();
            Item item = itemService.getItemConvalidacioById(idItem);
            solicitud.getEstudisOrigen().add(item);

            //Agafam item composa que ens arriba per paràmetre (potser no tots els ítems estiguin marcats i només arribarien els marcats)
            if (itemJSON.getAsJsonObject().get("composa") != null && !itemJSON.getAsJsonObject().get("composa").isJsonNull()) {
                JsonArray itemComposa = itemJSON.getAsJsonObject().get("composa").getAsJsonArray();
                for (JsonElement itemFillJSON : itemComposa) {
                    Long idItemFill = itemFillJSON.getAsJsonObject().get("iditem").getAsLong();
                    Item itemFill = itemService.getItemConvalidacioById(idItemFill);
                    solicitud.getEstudisOrigen().add(itemFill);
                }
            }
        }

        if (jsonObject.get("estudisOrigenManual") != null && !jsonObject.get("estudisOrigenManual").isJsonNull()) {
            String estudisOrigenManual = jsonObject.get("estudisOrigenManual").getAsString();
            solicitud.setEstudisOrigenManual(estudisOrigenManual);
        }

        if (jsonObject.get("estudisOrigenObservacions") != null && !jsonObject.get("estudisOrigenObservacions").isJsonNull()) {
            String estudisOrigenObservacions = jsonObject.get("estudisOrigenObservacions").getAsString();
            solicitud.setEstudisOrigenObservacions(estudisOrigenObservacions);
        }

        //Estudis en curs
        Long idItemEnCurs = jsonObject.get("estudisEnCurs").getAsJsonObject().get("id").getAsLong();
        Item itemEnCurs = itemService.getItemConvalidacioById(idItemEnCurs);
        solicitud.setEstudisEnCurs(itemEnCurs);

        if (jsonObject.get("estudisEnCursObservacions") != null && !jsonObject.get("estudisEnCursObservacions").isJsonNull()) {
            String estudisEnCursObservacions = jsonObject.get("estudisEnCursObservacions").getAsString();
            solicitud.setEstudisEnCursObservacions(estudisEnCursObservacions);
        }

        //Estat
        solicitud.setEstat(SolcititudEstat.PENDENT_RESOLUCIO);

        Solicitud solicitudConvalidacio = solicitudService.save(solicitud);

        //Observacions
        if (jsonObject.get("observacions") != null && !jsonObject.get("observacions").isJsonNull()) {
            String observacions = jsonObject.get("observacions").getAsString();
            solicitud.setObservacions(observacions);
        }

        //Resolucions
        JsonArray resolucionsJSON = jsonObject.get("resolucions").getAsJsonArray();

        //Flag per comprovar si totes les resolución estan resoltes (APROVAT o DENEGAT)
        boolean hasAllResolucions = true;
        for (JsonElement resolucioJSON : resolucionsJSON) {
            Resolucio resolucioConvalidacio = new Resolucio();

            resolucioConvalidacio.setSolicitud(solicitudConvalidacio);

            ResolucioEstat estat = ResolucioEstat.valueOf(resolucioJSON.getAsJsonObject().get("estat").getAsString());
            resolucioConvalidacio.setEstat(estat);

            String qualificacio = "";
            if (resolucioJSON.getAsJsonObject().get("qualificacio") != null && !resolucioJSON.getAsJsonObject().get("qualificacio").isJsonNull()) {
                qualificacio = resolucioJSON.getAsJsonObject().get("qualificacio").getAsString();
                resolucioConvalidacio.setQualificacio(qualificacio);
            }

            Long idEstudi = resolucioJSON.getAsJsonObject().get("estudi").getAsJsonObject().get("id").getAsLong();
            Item estudi = itemService.getItemConvalidacioById(idEstudi);
            resolucioConvalidacio.setEstudi(estudi);

            if (resolucioJSON.getAsJsonObject().get("observacions") != null && !resolucioJSON.getAsJsonObject().get("observacions").isJsonNull()) {
                String resolucioObservacions = resolucioJSON.getAsJsonObject().get("observacions").getAsString();
                resolucioConvalidacio.setObservacions(resolucioObservacions);
            }

            if (estat == ResolucioEstat.PENDENT || estat == ResolucioEstat.PREAPROVAT) {
                hasAllResolucions = false;
            }

            resolucioService.save(resolucioConvalidacio);
        }

        //Actualitzem l'estat de la sol·licitud depenent de les resolucions
        if(solicitud.getEstat()==null || (!solicitud.getEstat().equals(SolcititudEstat.RESOLT) && !solicitud.getEstat().equals(SolcititudEstat.CANCELAT)) ) {
            if (hasAllResolucions) {
                solicitud.setEstat(SolcititudEstat.PENDENT_SIGNATURA);
            } else {
                solicitud.setEstat(SolcititudEstat.PENDENT_RESOLUCIO);
            }
        }
        solicitudService.save(solicitud);


        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Sol·licitud desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }


    @PostMapping("/public/solicitud/desar")
    public ResponseEntity<Notificacio> desarSolicitudPublic(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Solicitud solicitud = new Solicitud();
        //Introduim només una vegada la data de creació de la sol·licitud
        Date ara = new Date();
        solicitud.setDataCreacio(ara);

        //Alumne
        String nomAlumne = jsonObject.get("nomAlumneManual").getAsString();
        String cognomsAlumne = jsonObject.get("cognomsAlumneManual").getAsString();
        solicitud.setNomAlumneManual(nomAlumne);
        solicitud.setCognomsAlumneManual(cognomsAlumne);

        //Estudis Origen
        JsonArray estudisEnCursJSON = jsonObject.get("estudisOrigen").getAsJsonArray();

        for (JsonElement itemJSON : estudisEnCursJSON) {
            Long idItem = itemJSON.getAsJsonObject().get("id").getAsLong();
            Item item = itemService.getItemConvalidacioById(idItem);
            solicitud.getEstudisOrigen().add(item);

            //Agafam item composa que ens arriba per paràmetre (potser no tots els ítems estiguin marcats i només arribarien els marcats)
            if (itemJSON.getAsJsonObject().get("composa") != null && !itemJSON.getAsJsonObject().get("composa").isJsonNull()) {
                JsonArray itemComposa = itemJSON.getAsJsonObject().get("composa").getAsJsonArray();
                for (JsonElement itemFillJSON : itemComposa) {
                    Long idItemFill = itemFillJSON.getAsJsonObject().get("iditem").getAsLong();
                    Item itemFill = itemService.getItemConvalidacioById(idItemFill);
                    solicitud.getEstudisOrigen().add(itemFill);
                }
            }
        }

        if (jsonObject.get("estudisOrigenManual") != null && !jsonObject.get("estudisOrigenManual").isJsonNull()) {
            String estudisOrigenManual = jsonObject.get("estudisOrigenManual").getAsString();
            solicitud.setEstudisOrigenManual(estudisOrigenManual);
        }

        if (jsonObject.get("estudisOrigenObservacions") != null && !jsonObject.get("estudisOrigenObservacions").isJsonNull()) {
            String estudisOrigenObservacions = jsonObject.get("estudisOrigenObservacions").getAsString();
            solicitud.setEstudisOrigenObservacions(estudisOrigenObservacions);
        }

        //Estudis en curs
        Long idItemEnCurs = jsonObject.get("estudisEnCurs").getAsJsonObject().get("id").getAsLong();
        Item itemEnCurs = itemService.getItemConvalidacioById(idItemEnCurs);
        solicitud.setEstudisEnCurs(itemEnCurs);

        if (jsonObject.get("estudisEnCursObservacions") != null && !jsonObject.get("estudisEnCursObservacions").isJsonNull()) {
            String estudisEnCursObservacions = jsonObject.get("estudisEnCursObservacions").getAsString();
            solicitud.setEstudisEnCursObservacions(estudisEnCursObservacions);
        }


        //Arxius
        if(jsonObject.get("files") != null && !jsonObject.get("files").isJsonNull()) {
            JsonArray arxiusJSON = jsonObject.get("files").getAsJsonArray();

            for (JsonElement arxiuJSON : arxiusJSON) {
                String pathArxiu = arxiuJSON.getAsString();

                File arxiu = new File(pathArxiu);
                System.out.println("Arxiu desat a " + pathArxiu);

                ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.uploadObject(bucketPathFiles + "/filesalumnes/"+ arxiu.getName(), pathArxiu, bucketName);
                FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

                ResponseEntity<FitxerBucketDto> fitxerBucketSavedResponse = coreRestClient.save(fitxerBucket);
                FitxerBucketDto fitxerBucketSaved = fitxerBucketSavedResponse.getBody();

                solicitud.getFitxersAlumne().add(fitxerBucketSaved.getIdfitxer());
            }
        }


        //Estat
        solicitud.setEstat(SolcititudEstat.PENDENT_RESOLUCIO);

        Solicitud solicitudConvalidacio = solicitudService.save(solicitud);

        //Resolucions
        List<Item> convalidacions = solicitudService.calculaConvalidacions(solicitudConvalidacio);
        JsonArray resolucionsJSON = jsonObject.get("resolucions").getAsJsonArray();

        //Flag per comprovar si totes les resolución estan resoltes (APROVAT o DENEGAT)
        for (JsonElement resolucioJSON : resolucionsJSON) {
            Resolucio resolucioConvalidacio = new Resolucio();

            resolucioConvalidacio.setSolicitud(solicitudConvalidacio);


            String qualificacio = "";
            resolucioConvalidacio.setQualificacio(qualificacio);

            Long idEstudi = resolucioJSON.getAsJsonObject().get("estudi").getAsJsonObject().get("id").getAsLong();
            Item estudi = itemService.getItemConvalidacioById(idEstudi);
            resolucioConvalidacio.setEstudi(estudi);

            boolean convalidacioFound = convalidacions.stream().anyMatch(c->c.getIditem().equals(resolucioConvalidacio.getEstudi().getIditem()));

            if(convalidacioFound){
                resolucioConvalidacio.setEstat(ResolucioEstat.PREAPROVAT);
            } else {
                resolucioConvalidacio.setEstat(ResolucioEstat.PENDENT);
            }

            if (resolucioJSON.getAsJsonObject().get("observacions") != null && !resolucioJSON.getAsJsonObject().get("observacions").isJsonNull()) {
                String resolucioObservacions = resolucioJSON.getAsJsonObject().get("observacions").getAsString();
                resolucioConvalidacio.setObservacions(resolucioObservacions);
            }

            resolucioService.save(resolucioConvalidacio);
        }

        //Actualitzem l'estat de la sol·licitud depenent de les resolucions
        solicitud.setEstat(SolcititudEstat.PENDENT_RESOLUCIO);

        solicitudService.save(solicitud);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Sol·licitud enregistrada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/public/solicitud/upload")
    public ResponseEntity<String> handleFileUpload(@RequestPart(value = "file") final MultipartFile uploadfile) throws IOException {
        String path = saveUploadedFiles(uploadfile);
        return new ResponseEntity<>(path, HttpStatus.OK);
    }

    private String saveUploadedFiles(final MultipartFile file) throws IOException {

        Date ara = new Date();

        Random random = new Random();
        //Generate numbers between 0 and 1.000.000.000
        int randomValue = random.nextInt(1000000001);

        String absolutePath = StringUtils.stripAccents("/tmp/" + ara.getTime() + "_" +randomValue+"_"+ file.getOriginalFilename());
        absolutePath = absolutePath.replaceAll("\\s+","");

        final byte[] bytes = file.getBytes();
        final Path path = Paths.get(absolutePath);
        Files.write(path, bytes);

        System.out.println(absolutePath);
        return absolutePath;
    }



    @PostMapping("/solicitud/esborrar")
    public ResponseEntity<Notificacio> esborrarSolicitud(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idSolicitud = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()) {
            idSolicitud = jsonObject.get("id").getAsLong();
        }

        if (idSolicitud != null) {

            Solicitud solicitudConvalidacio = solicitudService.getSolicitudConvalidacioById(idSolicitud);

            if (solicitudConvalidacio.getResolucions() != null) {
                for (Resolucio resolucioConvalidacio : solicitudConvalidacio.getResolucions()) {
                    resolucioService.esborrar(resolucioConvalidacio);
                }
            }

            //Esborrem els fitxers pujats per l'alumne
            if(solicitudConvalidacio.getEstudisOrigen() != null){
                for(Long idfitxerBucket: solicitudConvalidacio.getFitxersAlumne()){
                    ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.getFitxerBucketById(idfitxerBucket);
                    FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

                    coreRestClient.deleteObject(fitxerBucket.getPath(),fitxerBucket.getBucket());
                    coreRestClient.delete(fitxerBucket);
                }
            }

            //Esborrem el fitxer de la resolució
            if(solicitudConvalidacio.getFitxerResolucio()!=null){
                Long idfitxerBucket = solicitudConvalidacio.getFitxerResolucio();

                ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.getFitxerBucketById(idfitxerBucket);
                FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

                coreRestClient.deleteObject(fitxerBucket.getPath(),fitxerBucket.getBucket());
                coreRestClient.delete(fitxerBucket);
            }

            solicitudService.esborrar(solicitudConvalidacio);

            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Sol·licitud esborrada correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("No s'ha pogut esborrar la convalidació");
        notificacio.setNotifyType(NotificacioTipus.ERROR);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }


    @PostMapping("/pdf/signfile")
    public ResponseEntity<Notificacio> signPDF(HttpServletRequest request) throws IOException {
        try {

            //ARXIU PDF
            Part filePart = request.getPart("arxiu");

            InputStream is = filePart.getInputStream();

            // Reads the file into memory

            //Path path = Paths.get(audioPath); byte[] data = Files.readAllBytes(path);
            //ByteString audioBytes = ByteString.copyFrom(data);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] readBuf = new byte[4096];
            while (is.available() > 0) {
                int bytesRead = is.read(readBuf);
                os.write(readBuf, 0, bytesRead);
            }

            // Passam l'arxiu a dins una carpeta
            String fileName = "/tmp/arxiu.pdf";

            OutputStream outputStream = new FileOutputStream(fileName);
            os.writeTo(outputStream);

            File f = new File(fileName);


            System.out.println(f.getAbsolutePath());

            //ARXIU SIGNATURA
            Part filePartSignatura = request.getPart("signatura");

            InputStream isSignatura = filePartSignatura.getInputStream();

            // Reads the file into memory

            //Path path = Paths.get(audioPath); byte[] data = Files.readAllBytes(path);
            //ByteString audioBytes = ByteString.copyFrom(data);

            ByteArrayOutputStream osSignatura = new ByteArrayOutputStream();
            byte[] readBufSignatura = new byte[4096];
            while (isSignatura.available() > 0) {
                int bytesReadSignatura = isSignatura.read(readBufSignatura);
                osSignatura.write(readBufSignatura, 0, bytesReadSignatura);
            }

            // Passam l'arxiu a dins una carpeta
            String fileNameSignatura = "/tmp/signatura.p12";

            OutputStream outputStreamSignatura = new FileOutputStream(fileNameSignatura);
            osSignatura.writeTo(outputStreamSignatura);

            File fSignatura = new File(fileNameSignatura);


            //PASSWORD
            Part passwordPart = request.getPart("password");
            InputStream isPassword = passwordPart.getInputStream();
            String password = new BufferedReader(
                    new InputStreamReader(isPassword, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            //ID SOLICITUD
            Part idsolicitudPart = request.getPart("idsolicitud");
            InputStream isIdSolicitud = idsolicitudPart.getInputStream();
            String idsolicitud = new BufferedReader(
                    new InputStreamReader(isIdSolicitud, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            //Configurem JAVA perquè accepti els certificats
            //System.setProperty("javax.net.ssl.trustStore","/tmp/signatura.p12");
            //System.setProperty("javax.net.ssl.trustStorePassword",password);

            //SIGNAR
            System.out.println("Signing file 9: "+fSignatura.getAbsolutePath()+"---"+password+"---"+f.getAbsolutePath());
            boolean signed = pdfService.signDocument(fSignatura.getAbsolutePath(),password,f.getAbsolutePath());
            if(signed){
                System.out.println("File signed, uploading to Core and Google Cloud Storage");

                String fileNameSigned = "/tmp/arxiu_signed.pdf";
                File fileSigned = new File(fileNameSigned);

                String remotePath = "";
                String boundary = "---------------"+UUID.randomUUID().toString();


                //MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                //body.add("file", new File("/tmp/arxiu_signed.pdf"));

                System.out.println("pre remote path");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
                parts.add("file", new FileSystemResource(new File("/tmp/arxiu_signed.pdf")));

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

                String serverUrl = this.coreAddress + "/api/core/public/fitxerbucket/uploadlocal";

                // Create an SSL context that trusts all certificates
                SSLContext sslContext = SSLContext.getInstance("TLS");
                TrustManager[] trustAllCertificates = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };
                sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
                factory.setHttpClient(HttpClients.custom().setSslcontext(sslContext).build());

                RestTemplate restTemplate = new RestTemplate(factory);

                //RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new FormHttpMessageConverter()); // Use FormHttpMessageConverter for multipart form data

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        serverUrl,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                remotePath = responseEntity.getBody();
                System.out.println("fi remote path");










                /*final HttpPost httpPost = new HttpPost(this.coreAddress + "/api/core/public/fitxerbucket/uploadlocal");

                final HttpEntity httpEntity = MultipartEntityBuilder.create()
                        .addBinaryBody("file", new File("/tmp/arxiu_signed.pdf"), ContentType.APPLICATION_OCTET_STREAM, "arxiu.pdf")
                        .build();

                httpPost.setEntity(httpEntity);
                httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);*/


                //try(CloseableHttpClient client = HttpClientBuilder.create()
                //        .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build()))
                //        .build()) {

               /* SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null,
                                new TrustStrategy() {
                                    @Override
                                    public boolean isTrusted(final X509Certificate[] chain, final String authType)
                                            throws CertificateException {
                                        return true;
                                    }
                                })
                        .build();

                try(CloseableHttpClient client = HttpClientBuilder.create().setSSLContext(sslContext).build()) {
                    remotePath = client.execute(httpPost, response -> {
                        //do something with response
                        System.out.println("Response de cridada 2...");
                        System.out.println(response.getStatusLine().getStatusCode());
                        System.out.println(response.getStatusLine().getReasonPhrase());
                        InputStream responseInputStream = response.getEntity().getContent();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseInputStream));
                        String line = "";
                        String out = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            out += line;
                        }
                        return out;
                    });
                }*/

                //final HttpGet getMethod = new HttpGet(HOST_WITH_SSL);

                /*
                final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
                final SSLContext sslContext = SSLContexts.custom()
                        .loadTrustMaterial(null, acceptingTrustStrategy)
                        .build();
                final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
                final Registry<ConnectionSocketFactory> socketFactoryRegistry =
                        RegistryBuilder.<ConnectionSocketFactory> create()
                                .register("https", sslsf)
                                .register("http", new PlainConnectionSocketFactory())
                                .build();

                final BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);

                //Upload multipart
                try( CloseableHttpClient httpClient = HttpClients.custom()
                        .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build()){

                         remotePath = httpClient.execute(httpPost, response -> {
                        //do something with response
                        System.out.println("Response de cridada 6...");
                        System.out.println(response.getStatusLine().getStatusCode());
                        System.out.println(response.getStatusLine().getReasonPhrase());
                        InputStream responseInputStream = response.getEntity().getContent();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseInputStream));
                        String line = "";
                        String out = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            out += line;
                        }
                        System.out.println("finalitzada cridada 4...");
                        return out;
                     });
                }
 */
                System.out.println("pre remote path");
                System.out.println("Remote path"+remotePath);
                System.out.println("fi remote path");



                Date ara = new Date();
                Solicitud solicitudConvalidacio = solicitudService.getSolicitudConvalidacioById(Long.valueOf(idsolicitud));

                ResponseEntity<UsuariDto> alumneResponse = coreRestClient.getProfile(solicitudConvalidacio.getAlumne().toString());
                UsuariDto alumne = alumneResponse.getBody();

                ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.uploadObject(this.bucketPathFiles+"/"+alumne.getGestibExpedient()+"/convalidacio_"+ara.getTime()+".pdf",remotePath,bucketName);
                FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

                ResponseEntity<FitxerBucketDto> fitxerBucketSavedResponse = coreRestClient.save(fitxerBucket);
                FitxerBucketDto fitxerBucketSaved = fitxerBucketSavedResponse.getBody();

                solicitudConvalidacio.setEstat(SolcititudEstat.RESOLT);
                solicitudConvalidacio.setFitxerResolucio(fitxerBucketSaved.getIdfitxer());
                solicitudConvalidacio.setDataSignatura(ara);
                solicitudService.save(solicitudConvalidacio);

                String nomAlumne = "";
                if(alumne.getGestibNom()!=null){
                    nomAlumne += alumne.getGestibNom();
                }

                if(alumne.getGestibCognom1()!=null){
                    nomAlumne += " " + alumne.getGestibCognom1();
                }

                if(alumne.getGestibCognom2()!=null){
                    nomAlumne += " " + alumne.getGestibCognom2();
                }

                String body = "";
                body += "Benvolgut/da "+ nomAlumne +", ";
                body += "<br><br>";
                body += "Des de Politècnic de Llevant us fem arribar la <strong>resolució</strong> de la convalidació sol·licitada a l'IES Manacor.";

                coreRestClient.sendEmailAttachmentPath(alumne.getGsuiteEmail(),"Resolució de convalidació",body,remotePath);

                body = "Còpia del missatge enviat a "+nomAlumne+": <br><br>" + body;

                String[] emailsResolucio = this.notificarResolucionsEmails.split(",");

                for(String email: emailsResolucio){
                    coreRestClient.sendEmailAttachmentPath(email,"Resolució de convalidació",body,remotePath);
                }

                //Notifiguem als tutors
                GrupDto grup = null;
                if(alumne.getGestibGrup()!=null) {
                    ResponseEntity<GrupDto> grupResponse = coreRestClient.getGrupById(Long.parseLong(alumne.getGestibGrup()));
                    grup = grupResponse.getBody();
                } else if(alumne.getGestibGrup2() != null ){
                    ResponseEntity<GrupDto> grupResponse = coreRestClient.getGrupById(Long.parseLong(alumne.getGestibGrup2()));
                    grup = grupResponse.getBody();
                } else if(alumne.getGestibGrup3() != null ){
                    ResponseEntity<GrupDto> grupResponse = coreRestClient.getGrupById(Long.parseLong(alumne.getGestibGrup3()));
                    grup = grupResponse.getBody();
                }

                if(grup != null){
                    List<UsuariDto> tutors = new ArrayList<>();
                    if(grup.getGestibTutor1()!=null){
                        ResponseEntity<UsuariDto> tutorResponse = coreRestClient.getProfile(grup.getGestibTutor1());
                        UsuariDto tutor = tutorResponse.getBody();
                        tutors.add(tutor);
                    }
                    if(grup.getGestibTutor2()!=null){
                        ResponseEntity<UsuariDto> tutorResponse = coreRestClient.getProfile(grup.getGestibTutor2());
                        UsuariDto tutor = tutorResponse.getBody();
                        tutors.add(tutor);
                    }
                    if(grup.getGestibTutor3()!=null){
                        ResponseEntity<UsuariDto> tutorResponse = coreRestClient.getProfile(grup.getGestibTutor3());
                        UsuariDto tutor = tutorResponse.getBody();
                        tutors.add(tutor);
                    }

                    //Enviem el correu al/s tutor/s
                    for(UsuariDto tutor: tutors){
                        coreRestClient.sendEmailAttachmentPath(tutor.getGsuiteEmail(),"Resolució de convalidació",body,remotePath);
                    }
                }

                Notificacio notificacio = new Notificacio();
                notificacio.setNotifyMessage("PDF Signat amb èxit");
                notificacio.setNotifyType(NotificacioTipus.SUCCESS);
                return new ResponseEntity<>(notificacio, HttpStatus.OK);
            } else{
                Notificacio notificacio = new Notificacio();
                notificacio.setNotifyMessage("Error signant el document. Revisi la signatura i/o la contrasenya.");
                notificacio.setNotifyType(NotificacioTipus.ERROR);
                return new ResponseEntity<>(notificacio, HttpStatus.OK);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Error amb la signatura");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }
    }
}