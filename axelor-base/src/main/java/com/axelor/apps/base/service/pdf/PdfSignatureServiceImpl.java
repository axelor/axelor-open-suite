package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.signature.SignatureService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.ZoneId;
import java.util.Calendar;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

public class PdfSignatureServiceImpl implements PdfSignatureService {

  protected MetaFiles metaFiles;
  protected SignatureService signatureService;

  @Inject
  public PdfSignatureServiceImpl(MetaFiles metaFiles, SignatureService signatureService) {
    this.metaFiles = metaFiles;
    this.signatureService = signatureService;
  }

  @Override
  public MetaFile digitallySignPdf(
      MetaFile metaFile, MetaFile certificate, String certificatePassword, String reason)
      throws AxelorException {

    try {
      File tempPdfFile = File.createTempFile(metaFile.getFileName(), ".pdf");
      try (FileOutputStream outStream = new FileOutputStream(tempPdfFile);
          FileInputStream inputStream =
              new FileInputStream(String.valueOf(MetaFiles.getPath(certificate)))) {
        try (PDDocument doc = Loader.loadPDF(new File(MetaFiles.getPath(metaFile).toString()))) {
          getCertificateAndSign(certificatePassword, reason, inputStream, doc, outStream);
        }
      }
      MetaFile resultFile = metaFiles.upload(tempPdfFile);
      resultFile.setFileName(Files.getNameWithoutExtension(metaFile.getFileName()) + ".pdf");
      return resultFile;
    } catch (AxelorException | IOException | GeneralSecurityException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SIGNING_PDF_ERROR),
          e.getMessage());
    }
  }

  protected void getCertificateAndSign(
      String certificatePassword,
      String reason,
      FileInputStream inputStream,
      PDDocument doc,
      FileOutputStream outStream)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
          UnrecoverableKeyException, AxelorException {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(inputStream, certificatePassword.toCharArray());
    String alias = keyStore.aliases().nextElement();
    PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, certificatePassword.toCharArray());
    Certificate[] certificateChain = keyStore.getCertificateChain(alias);
    signDetached(doc, outStream, certificateChain, privateKey, reason);
  }

  protected void signDetached(
      PDDocument document,
      OutputStream output,
      Certificate[] certificateChain,
      PrivateKey privateKey,
      String reason)
      throws IOException, AxelorException {

    PDSignature signature = getSignature(reason);
    document.addSignature(signature);
    setExternalSigning(document, output, certificateChain, privateKey);
  }

  protected void setExternalSigning(
      PDDocument document,
      OutputStream output,
      Certificate[] certificateChain,
      PrivateKey privateKey)
      throws IOException, AxelorException {
    ExternalSigningSupport externalSigning = document.saveIncrementalForExternalSigning(output);
    byte[] cmsSignature =
        signatureService.sign(externalSigning.getContent(), certificateChain, privateKey);
    externalSigning.setSignature(cmsSignature);
  }

  protected PDSignature getSignature(String reason) {
    PDSignature signature = new PDSignature();
    signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
    signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
    signature.setSignDate(Calendar.getInstance());
    signature.setLocation(ZoneId.systemDefault().getId());
    if (StringUtils.notEmpty(reason)) {
      signature.setReason(reason);
    }

    return signature;
  }
}
