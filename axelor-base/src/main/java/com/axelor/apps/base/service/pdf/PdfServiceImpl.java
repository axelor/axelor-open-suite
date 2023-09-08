package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.persist.Transactional;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.inject.Inject;

public class PdfServiceImpl implements PdfService {

  protected MetaFiles metaFiles;
  protected MetaFileRepository metaFileRepository;

  @Inject
  public PdfServiceImpl(MetaFiles metaFiles, MetaFileRepository metaFileRepository) {
    this.metaFiles = metaFiles;
    this.metaFileRepository = metaFileRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public MetaFile convertImageToPdf(MetaFile metaFile) throws AxelorException {
    MetaFile pdfToSign = null;
    String fileType = metaFile.getFileType();

    if (fileType.startsWith("image")) {
      pdfToSign = convertImageToPdfProcess(metaFile);
    }

    if (fileType.contains("pdf")) {
      pdfToSign = metaFile;
    }
    return pdfToSign;
  }

  public MetaFile convertImageToPdfProcess(MetaFile metaFile) throws AxelorException {
    try {
      if (metaFile == null) {
        return null;
      }
      File tempPdfFile = File.createTempFile(metaFile.getFileName(), ".pdf");
      try (FileOutputStream outStream = new FileOutputStream(tempPdfFile);
          PdfWriter pdfWriter = new PdfWriter(outStream);
          PdfDocument pdfDocument = new PdfDocument(pdfWriter);
          Document document = new Document(pdfDocument)) {
        ImageData data = ImageDataFactory.create(String.valueOf(MetaFiles.getPath(metaFile)));
        Image image = new Image(data);
        document.add(image);
      }
      metaFiles.delete(metaFile);
      return metaFiles.upload(tempPdfFile);
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.CONVERT_IMAGE_TO_PDF_ERROR),
          e.getMessage());
    }
  }
}
