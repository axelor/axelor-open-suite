package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.common.io.Files;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

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
    MetaFile pdfToSign = metaFile;
    String fileType = metaFile.getFileType();

    if (fileType.startsWith("image")) {
      pdfToSign = convertImageToPdfProcess(metaFile);
    }

    return pdfToSign;
  }

  @Override
  public List<MetaFile> convertImageToPdf(List<MetaFile> metaFileList) throws AxelorException {
    List<MetaFile> pdfList = new ArrayList<>();
    for (MetaFile metaFile : metaFileList) {
      pdfList.add(convertImageToPdf(metaFile));
    }
    return pdfList;
  }

  public MetaFile convertImageToPdfProcess(MetaFile metaFile) throws AxelorException {
    try {
      if (metaFile == null) {
        return null;
      }
      File tempPdfFile =
          File.createTempFile(Files.getNameWithoutExtension(metaFile.getFileName()), ".pdf");

      convertImageToPdf(metaFile, tempPdfFile);
      MetaFile resultFile = metaFiles.upload(tempPdfFile);
      resultFile.setFileName(Files.getNameWithoutExtension(metaFile.getFileName()) + ".pdf");
      return resultFile;
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.CONVERT_IMAGE_TO_PDF_ERROR),
          e.getMessage());
    }
  }

  protected void convertImageToPdf(MetaFile metaFile, File tempPdfFile) throws IOException {
    try (PDDocument doc = new PDDocument()) {
      doc.addPage(new PDPage(PDRectangle.A4));
      PDPage page = doc.getPage(0);
      PDImageXObject pdImage =
          PDImageXObject.createFromFile(MetaFiles.getPath(metaFile).toString(), doc);
      drawImageInPdf(doc, page, pdImage);
      doc.save(tempPdfFile);
    }
  }

  protected void drawImageInPdf(PDDocument doc, PDPage page, PDImageXObject pdImage)
      throws IOException {
    try (PDPageContentStream contentStream =
        new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
      int imageWidth = pdImage.getWidth();
      int imageHeight = pdImage.getHeight();
      float a4width = PDRectangle.A4.getWidth() - 40;
      float a4height = PDRectangle.A4.getHeight();
      float scaleDownRatio = computeScaleRatio(a4height, a4width, imageHeight, imageWidth);

      contentStream.drawImage(
          pdImage,
          20,
          a4height - ((imageHeight * scaleDownRatio) + 20),
          imageWidth * scaleDownRatio,
          imageHeight * scaleDownRatio);
    }
  }

  protected float computeScaleRatio(
      float a4height, float a4width, float imageHeight, float imageWidth) {
    float scaleDownRatio = 1f;
    float widthRatio = a4width / imageWidth;
    float heightRatio = a4height / imageHeight;
    if (widthRatio < 1 || heightRatio < 1) {
      scaleDownRatio = Math.min(widthRatio, heightRatio);
    }
    return scaleDownRatio;
  }
}
