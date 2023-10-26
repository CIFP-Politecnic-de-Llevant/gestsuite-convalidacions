package cat.politecnicllevant.convalidacions.pdf.service;

import cat.politecnicllevant.convalidacions.pdf.service.pdfbox.CreateSignature;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

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

            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            //PDFont font = PDType1Font.HELVETICA;

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
            //CreateSignature.signDocument(args);

        } catch (IOException e) {
            System.out.println("Error"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean signDocument(String pathCertificatP12,String password, String pathPDFFile) {
        /*
        System.out.println("testCreatePDFA");
        String pdfaFilename = outDir + "/PDFA.pdf";
        String signedPdfaFilename = outDir + "/PDFA_signed.pdf";
        String keystorePath = "src/test/resources/org/apache/pdfbox/examples/signature/keystore.p12";
        String message = "The quick brown fox jumps over the lazy dog äöüÄÖÜß @°^²³ {[]}";
        String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
        String fontfile = dir + "LiberationSans-Regular.ttf";
        CreatePDFA.main(new String[] { pdfaFilename, message, fontfile });

        // sign PDF - because we want to make sure that the signed PDF is also PDF/A-1b
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), "123456".toCharArray());
        CreateSignature signing = new CreateSignature(keystore, "123456".toCharArray());
        signing.signDetached(new File(pdfaFilename), new File(signedPdfaFilename));
         */
        try {
            System.out.println(pathCertificatP12+"---"+password+"---"+pathPDFFile);

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(pathCertificatP12), password.toCharArray());

            String signedPdfaFilename = "/tmp/arxiu_signed.pdf";

            CreateSignature sign = new CreateSignature(ks,password.toCharArray());
            sign.signDetached(new File(pathPDFFile), new File(signedPdfaFilename));
            return true;
        } catch (Exception e){
            return false;
        }
    }

}

