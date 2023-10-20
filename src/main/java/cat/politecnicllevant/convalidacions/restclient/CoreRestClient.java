package cat.politecnicllevant.convalidacions.restclient;

import cat.politecnicllevant.convalidacions.dto.core.gestib.GrupDto;
import cat.politecnicllevant.convalidacions.dto.core.gestib.UsuariDto;
import cat.politecnicllevant.convalidacions.dto.google.FitxerBucketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

@FeignClient(name = "core")
public interface CoreRestClient {

    //USUARIS
    @GetMapping("/usuaris/profile/{id}")
    ResponseEntity<UsuariDto> getProfile(@PathVariable("id") String idUsuari) throws Exception;

    //FITXER BUCKET
    @GetMapping("/fitxerbucket/{id}")
    ResponseEntity<FitxerBucketDto> getFitxerBucketById(@PathVariable("id") Long idfitxerBucket);

    @PostMapping("/fitxerbucket/save")
    ResponseEntity<FitxerBucketDto> save(@RequestBody FitxerBucketDto fitxerBucket) throws IOException;

    @PostMapping("/fitxerbucket/delete")
    void delete(@RequestBody FitxerBucketDto fitxerBucket);

    //GOOGLE STORAGE
    @PostMapping(value = "/googlestorage/generate-signed-url")
    ResponseEntity<String> generateSignedURL(@RequestBody String json) throws IOException;

    @PostMapping("/googlestorage/uploadobject")
    ResponseEntity<FitxerBucketDto> uploadObject(@RequestParam("objectName") String objectName, @RequestParam("filePath") String filePath, @RequestParam("bucket") String bucket) throws IOException, GeneralSecurityException;

    @PostMapping(value = "/googlestorage/uploadobjectfile", consumes = "multipart/form-data")
    ResponseEntity<FitxerBucketDto> uploadObjectFile(@RequestParam("objectName") String objectName, @RequestParam("bucket") String bucket, @RequestPart(value = "file") final File uploadfile) throws IOException, GeneralSecurityException;

    @PostMapping("/googlestorage/delete")
    void deleteObject(@RequestParam("objectName") String objectName, @RequestParam("bucket") String bucket) throws IOException, GeneralSecurityException;

    //GMAIL
    @PostMapping(value="/gsuite/sendemailattachment")
    void sendEmail(@RequestParam("to") String to, @RequestParam("assumpte") String assumpte, @RequestParam("body") String bodyHTML, @RequestParam File file) throws IOException, MessagingException, GeneralSecurityException;

    //GRUP
    @GetMapping("/grup/getById/{idgrup}")
    ResponseEntity<GrupDto> getGrupById(@PathVariable("idgrup") Long idgrup);
}
