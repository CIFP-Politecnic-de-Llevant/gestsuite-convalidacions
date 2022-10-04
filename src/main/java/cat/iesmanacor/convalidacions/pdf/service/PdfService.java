package cat.iesmanacor.convalidacions.pdf.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@Slf4j
public class PdfService {

    @Value("classpath:static/header_convalidacio.png")
    Resource resourceFile;

    public void helloWorld(){
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);

            //PDFont font = new PDType1Font(FontName.HELVETICA_BOLD);
            PDFont font = PDType1Font.HELVETICA;

            try (PDPageContentStream contents = new PDPageContentStream(doc, page))
            {
                contents.beginText();
                contents.setFont(font, 12);
                contents.newLineAtOffset(100, 700);
                contents.showText("hello world");
                contents.endText();
            }

            doc.save("/tmp/hola.pdf");

            String[] args = {"/Users/joangalmesriera/Downloads/CERTIFICAT.p12","CONTRASENYA","/tmp/hola.pdf"};
            CreateSignature.signDocument(args);

        } catch (IOException | GeneralSecurityException e) {
            System.out.println("Error"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean signDocument(String pathCertificatP12,String password, String pathPDFFile) throws GeneralSecurityException, IOException {
        try {
            String[] args = {pathCertificatP12, password, pathPDFFile};
            CreateSignature.signDocument(args);
            return true;
        } catch (Exception e){
            return false;
        }
    }

}

