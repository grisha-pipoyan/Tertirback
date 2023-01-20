package com.tertir.demo.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.tertir.demo.exception.NotFoundException;
import com.tertir.demo.persistance.qr.QrEntity;
import com.tertir.demo.persistance.qr.QrRepository;
import com.tertir.demo.rest.admin.models.QrCodeModel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QrService {

    private final QrRepository qrRepository;

    @Value("${application.pdf.path}")
    private String pdfPath;

    public QrService(QrRepository qrRepository) {
        this.qrRepository = qrRepository;
    }


    /**
     * Finds Qr entity by id
     *
     * @param id uuid
     * @return bytes of Qr image
     */
    public ByteArrayResource getQrCodeById(UUID id) {
        QrEntity qrEntity = qrRepository.findById(id).orElseThrow(() -> new NotFoundException("Սխալ Qr կոդ"));

        return new ByteArrayResource(Utils.fromBase64(qrEntity.getQrDataBase64()));
    }

    /**
     * Finds Qr entity by id
     *
     * @param id uuid
     * @return QrEntity
     */
    public QrEntity getQrEntityById(UUID id) {
        return qrRepository.findById(id).orElseThrow(() -> new NotFoundException("Սխալ Qr կոդ"));
    }

    /**
     * Finds Qr entity by random code
     *
     * @param randomCode random generated code
     * @return QrEntity
     */
    public QrEntity getQrEntityByRandomCode(Integer randomCode) {
        return qrRepository.findByRandomCode(randomCode).orElseThrow(() -> new NotFoundException(String.format(
                "Նշված %s կոդով Qr կոդ չի գտնվել", randomCode)));
    }

    public List<Integer> getAllQrEntities(){
        return qrRepository.findAllRandomCodeList();
    }

    /**
     * Returns all qr code information
     *
     * @return List of QrCodeModel
     */
    public List<QrCodeModel> getAllQrCodes() {
        return qrRepository.findAll().stream().map(
                        qrEntity -> new QrCodeModel(qrEntity.getId(), qrEntity.getCounter()))
                .collect(Collectors.toList());
    }

    /**
     * Generates QR codes
     *
     * @param number number of qr codes
     */
    public void generateQrCodes(Integer number) throws WriterException, IOException {

        log.info("Starting Qr code generation...");

        List<QrEntity> qrEntities = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            QrEntity qrEntity = new QrEntity();
            qrEntity.setCounter(0);
            qrEntity.setPrinted(false);
            qrEntities.add(qrEntity);
        }

        qrRepository.saveAll(qrEntities);
        qrRepository.flush();

        List<QrEntity> allByQrDataBase64IsNull = qrRepository.findAllByQrDataBase64IsNull();

        QRCodeWriter barcodeWriter = new QRCodeWriter();
        for (QrEntity qr :
                allByQrDataBase64IsNull) {

            log.info(String.format("QR code generation for Id: %s", qr.getId()));

            BitMatrix bitMatrix = barcodeWriter.encode(
                    qr.getId().toString(),
                    BarcodeFormat.QR_CODE,
                    100, 100);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);

            qr.setQrDataBase64(Utils.toBase64(byteArrayOutputStream.toByteArray()));

            qrRepository.save(qr);
        }

        log.info(String.format("Successfully generated and save %d Qr codes", number));

    }
    /**
     * Generates PDF file from qr codes
     *
     * @return PDF file encoded in base64
     * @throws IOException when can not read file
     */
    public ByteArrayResource generatePDFFromAllNonPrintedQrCodes() throws IOException {

        //String currenPath = filePath + UUID.randomUUID() + ".pdf";
        String currenPath = pdfPath + UUID.randomUUID() + ".pdf";
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(currenPath));
        Document document = new Document(pdfDocument);
        document.setMargins(10,10,10,10);

        List<QrEntity> qrEntities = qrRepository.findAllByPrintedFalseAndQrDataBase64IsNotNull();
        System.out.printf("Found %o Qr non printed Qr codes%n", qrEntities.size());
        Table signatureTable = new Table(UnitValue.createPercentArray(6));
        signatureTable.setMargins(3,3,3,3);
        for (QrEntity qr :
                qrEntities) {
            System.out.printf("Found Qr code for ID %s%n",qr.getId().toString());
            byte[] imageBytes = Utils.fromBase64(qr.getQrDataBase64());
            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image image = new Image(imageData);
            image.setWidth(pdfDocument.getDefaultPageSize().getWidth() - 50);
            image.setAutoScaleHeight(true);

            Cell cell = new Cell().add(image).setBorder(Border.NO_BORDER);

            signatureTable.addCell(cell);
            //signatureTable.addCell(new Cell().add(image).setBorder(Border.NO_BORDER));
            //signatureTable.addCell(new Cell().add(new Paragraph(qr.getId().toString())));
            qr.setPrinted(true);
        }

        document.add(signatureTable);
        document.add(new Paragraph(LocalDateTime.now().toString()));
        document.close();
        pdfDocument.close();

        qrRepository.saveAll(qrEntities);

        DSSDocument dssDocument = new FileDocument(currenPath);
        byte[] pdfBytes;
        try (InputStream inputStream = dssDocument.openStream()) {
            pdfBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            String error = String.format("Error while reading file: %s", e.getMessage());
            log.error(error);
            throw new IOException(error);
        }

        File file = new File(currenPath);
        if (!file.delete()) {
            String error = String.format("Can not delete file: %s", currenPath);
            log.error(error);
            throw new IOException(error);
        }

        return new ByteArrayResource(pdfBytes);
    }

    public void saveQr(QrEntity update){
        qrRepository.save(update);
    }

    /**
     * Finds sold papers count
     * @return count
     */
    public Integer getSoldPapersCount() {
        return qrRepository.findAllByUserIsNotNull().size();
    }
}
