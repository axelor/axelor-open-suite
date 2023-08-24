package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class PdfSignatureServiceImpl implements PdfSignatureService {

  protected MetaFiles metaFiles;
  public static final int SIGNATURE_WIDTH = 200;
  public static final int SIGNATURE_HEIGHT = 100;

  @Inject
  public PdfSignatureServiceImpl(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile digitallySignPdf(
      MetaFile metaFile,
      MetaFile certificate,
      String certificatePassword,
      MetaFile imageFile,
      String reason,
      String location)
      throws AxelorException {

    try {
      File tempPdfFile = File.createTempFile(metaFile.getFileName(), ".pdf");

      try (FileOutputStream outStream = new FileOutputStream(tempPdfFile);
          FileInputStream inputStream =
              new FileInputStream(String.valueOf(MetaFiles.getPath(certificate)))) {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(inputStream, certificatePassword.toCharArray());

        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey =
            (PrivateKey) keyStore.getKey(alias, certificatePassword.toCharArray());
        Certificate[] certificateChain = keyStore.getCertificateChain(alias);

        digitalSignature(
            String.valueOf(MetaFiles.getPath(metaFile.getFilePath())),
            outStream,
            certificateChain,
            privateKey,
            imageFile,
            reason,
            location);
      }
      return metaFiles.upload(tempPdfFile);
    } catch (AxelorException | IOException | GeneralSecurityException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SIGNING_PDF_ERROR),
          e.getMessage());
    }
  }

  protected void digitalSignature(
      String sourceFile,
      FileOutputStream outputStream,
      Certificate[] certificateChain,
      PrivateKey privateKey,
      MetaFile imageFile,
      String reason,
      String location)
      throws AxelorException {
    try {
      BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
      Security.addProvider(bouncyCastleProvider);

      try (PdfReader pdfReader = new PdfReader(sourceFile)) {
        PdfSigner pdfSigner = new PdfSigner(pdfReader, outputStream, new StampingProperties());

        // Create the signature appearance
        fillSignatureAppearance(imageFile, reason, location, pdfSigner);

        IExternalSignature iExternalSignature =
            new PrivateKeySignature(
                privateKey, DigestAlgorithms.SHA256, bouncyCastleProvider.getName());
        IExternalDigest iExternalDigest = new BouncyCastleDigest();

        // Sign the document using the detached mode, CMS, or CAdES equivalent.
        pdfSigner.signDetached(
            iExternalDigest,
            iExternalSignature,
            certificateChain,
            null,
            null,
            null,
            0,
            PdfSigner.CryptoStandard.CMS);
      }
    } catch (IOException | GeneralSecurityException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SIGNING_PDF_ERROR),
          e.getMessage());
    }
  }

  protected void fillSignatureAppearance(
      MetaFile imageFile, String reason, String location, PdfSigner pdfSigner)
      throws MalformedURLException {
    Rectangle rectangle = new Rectangle(SIGNATURE_WIDTH, SIGNATURE_HEIGHT);
    PdfSignatureAppearance pdfSignatureAppearance =
        pdfSigner
            .getSignatureAppearance()
            .setPageRect(rectangle)
            .setLocation(location)
            .setReason(reason);

    if (imageFile != null) {
      ImageData data = ImageDataFactory.create(String.valueOf(MetaFiles.getPath(imageFile)));
      pdfSignatureAppearance.setImage(data);
    }

    pdfSignatureAppearance.setRenderingMode(
        PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION);
  }
}
