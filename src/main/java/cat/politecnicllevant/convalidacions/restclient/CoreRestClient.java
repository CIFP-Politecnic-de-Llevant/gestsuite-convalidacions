package cat.politecnicllevant.convalidacions.restclient;

import cat.politecnicllevant.convalidacions.dto.FileUploadDto;
import cat.politecnicllevant.convalidacions.dto.core.gestib.CursAcademicDto;
import cat.politecnicllevant.convalidacions.dto.core.gestib.GrupDto;
import cat.politecnicllevant.convalidacions.dto.core.gestib.UsuariDto;
import cat.politecnicllevant.convalidacions.dto.google.FitxerBucketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@FeignClient(name = "core")
public interface CoreRestClient {

    //USUARIS
    @GetMapping("/usuaris/profile/{id}")
    ResponseEntity<UsuariDto> getProfile(@PathVariable("id") String idUsuari) throws Exception;

    @GetMapping("/usuaris/profile-by-gestib-codi/{id}")
    ResponseEntity<UsuariDto> getUsuariByGestibCodi(@PathVariable("id") String gestibCodi) throws Exception;

    //FITXER BUCKET
    @GetMapping("/fitxerbucket/{id}")
    ResponseEntity<FitxerBucketDto> getFitxerBucketById(@PathVariable("id") Long idfitxerBucket);

    @PostMapping("/fitxerbucket/save")
    ResponseEntity<FitxerBucketDto> save(@RequestBody FitxerBucketDto fitxerBucket) throws IOException;

    @PostMapping("/fitxerbucket/delete")
    void delete(@RequestBody FitxerBucketDto fitxerBucket);

    @PostMapping("/public/fitxerbucket/uploadlocal")
    ResponseEntity<String> handleFileUpload(@RequestPart(value = "file") final MultipartFile uploadfile) throws IOException;

    @PostMapping("/public/fitxerbucket/uploadlocal2")
    ResponseEntity<String> handleFileUpload2(@RequestBody FileUploadDto uploadfile) throws IOException;

    //GOOGLE STORAGE
    @PostMapping(value = "/googlestorage/generate-signed-url")
    ResponseEntity<String> generateSignedURL(@RequestBody String json) throws IOException;

    @PostMapping("/googlestorage/uploadobject")
    ResponseEntity<FitxerBucketDto> uploadObject(@RequestParam("objectName") String objectName, @RequestParam("filePath") String filePath, @RequestParam("contentType") String contentType, @RequestParam("bucket") String bucket) throws IOException, GeneralSecurityException;

    @PostMapping("/googlestorage/delete")
    void deleteObject(@RequestParam("objectName") String objectName, @RequestParam("bucket") String bucket) throws IOException, GeneralSecurityException;

    //GMAIL
    @PostMapping(value="/gsuite/sendemailattachment-path")
    void sendEmailAttachmentPath(@RequestParam("to") String to, @RequestParam("assumpte") String assumpte, @RequestParam("body") String bodyHTML, @RequestParam("path") String filepath) throws IOException, MessagingException, GeneralSecurityException;

    //GRUP
    @GetMapping("/grup/getById/{idgrup}")
    ResponseEntity<GrupDto> getGrupById(@PathVariable("idgrup") Long idgrup);

    @GetMapping("/grup/getByGestibIdentificador/{idgrup}")
    ResponseEntity<GrupDto> getByGestibIdentificador(@PathVariable("idgrup") String idgrup);

    //CURS ACADÈMIC
    @GetMapping("/cursAcademic/findAll")
    ResponseEntity<List<CursAcademicDto>> findAllCursosAcademics();

    @GetMapping("/cursAcademic/actual")
    ResponseEntity<CursAcademicDto> getActualCursAcademic();

    @GetMapping("/cursAcademic/getById/{id}")
    ResponseEntity<CursAcademicDto> getCursAcademicById(@PathVariable("id") Long identificador);
}
