/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PfxCertificateCheckService;
import com.axelor.apps.base.service.signature.SignatureService;
import com.axelor.common.FileUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

public class PdfSignatureServiceImpl implements PdfSignatureService {

  protected MetaFiles metaFiles;
  protected SignatureService signatureService;
  protected PfxCertificateCheckService pfxCertificateCheckService;

  @Inject
  public PdfSignatureServiceImpl(
      MetaFiles metaFiles,
      SignatureService signatureService,
      PfxCertificateCheckService pfxCertificateCheckService) {
    this.metaFiles = metaFiles;
    this.signatureService = signatureService;
    this.pfxCertificateCheckService = pfxCertificateCheckService;
  }

  @Override
  public MetaFile digitallySignPdf(MetaFile metaFile, PfxCertificate pfxCertificate, String reason)
      throws AxelorException {

    File signedPdfFile =
        digitallySignPdf(new File(MetaFiles.getPath(metaFile).toString()), pfxCertificate, reason);
    try {
      String baseName = FileUtils.stripExtension(FileUtils.safeFileName(metaFile.getFileName()));
      MetaFile resultFile = metaFiles.upload(signedPdfFile);
      resultFile.setFileName(baseName + ".pdf");
      return resultFile;
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SIGNING_PDF_ERROR),
          e.getMessage());
    }
  }

  @Override
  public File digitallySignPdf(File pdfFile, PfxCertificate pfxCertificate, String reason)
      throws AxelorException {

    pfxCertificateCheckService.checkValidity(pfxCertificate);
    MetaFile certificate = pfxCertificate.getCertificate();
    String certificatePassword = pfxCertificate.getPassword();
    try {
      File signedPdfFile = Files.createTempFile(null, ".pdf").toFile();
      try (FileOutputStream outStream = new FileOutputStream(signedPdfFile);
          FileInputStream inputStream =
              new FileInputStream(String.valueOf(MetaFiles.getPath(certificate)))) {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
          getCertificateAndSign(certificatePassword, reason, inputStream, doc, outStream);
        }
      }
      return signedPdfFile;
    } catch (AxelorException | IOException | GeneralSecurityException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SIGNING_PDF_ERROR),
          e.getMessage());
    }
  }

  @Override
  public void removeSignatureFields(File pdfFile) throws AxelorException {
    try {
      byte[] pdfBytes = java.nio.file.Files.readAllBytes(pdfFile.toPath());
      try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        if (acroForm == null) {
          return;
        }
        List<PDField> fields = acroForm.getFields();
        Set<COSDictionary> signatureWidgets =
            fields.stream()
                .filter(PDSignatureField.class::isInstance)
                .flatMap(field -> field.getWidgets().stream())
                .map(PDAnnotationWidget::getCOSObject)
                .collect(Collectors.toSet());
        if (signatureWidgets.isEmpty()) {
          return;
        }
        for (PDPage page : doc.getPages()) {
          List<PDAnnotation> annotations = page.getAnnotations();
          if (annotations.stream()
              .anyMatch(annotation -> signatureWidgets.contains(annotation.getCOSObject()))) {
            page.setAnnotations(
                annotations.stream()
                    .filter(annotation -> !signatureWidgets.contains(annotation.getCOSObject()))
                    .collect(Collectors.toList()));
          }
        }
        acroForm.setFields(
            fields.stream()
                .filter(field -> !(field instanceof PDSignatureField))
                .collect(Collectors.toList()));
        doc.save(pdfFile);
      }
    } catch (IOException e) {
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
      throws KeyStoreException,
          IOException,
          NoSuchAlgorithmException,
          CertificateException,
          UnrecoverableKeyException,
          AxelorException {
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
