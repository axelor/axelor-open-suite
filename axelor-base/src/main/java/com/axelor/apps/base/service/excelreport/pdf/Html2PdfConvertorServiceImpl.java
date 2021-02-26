/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.excelreport.pdf;

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaScanner;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

public class Html2PdfConvertorServiceImpl implements Html2PdfConvertorService {

  @Override
  public File toPdf(File htmlFile, String resourcePath)
      throws IOException, ParserConfigurationException, SAXException, AxelorException {
    File pdfFile =
        MetaFiles.createTempFile(FilenameUtils.removeExtension(htmlFile.getName()), ".pdf")
            .toFile();
    Path fontDirectory = Files.createTempDirectory("font");

    try (OutputStream output = new FileOutputStream(pdfFile)) {
      setLogging();
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.useFastMode();
      builder.withProducer("Axelor Reports");
      String html = parseHtml(htmlFile);

      builder.withHtmlContent(html, resourcePath);

      moveFontsToTempDir(fontDirectory);
      List<CSSFont> fonts = FontFactory.findFontsInDirectory(fontDirectory);
      FontFactory.toBuilder(builder, fonts);

      builder.useUriResolver(
          (baseUri, uri) -> {
            if (uri == null || uri.isEmpty()) return null;
            if (URI.create(uri).isAbsolute()) return uri;

            if (baseUri == null) { // WARN: can't resolve relative url without base url
              return null;
            }

            return URI.create(baseUri).resolve(uri).toString();
          });
      builder.testMode(true);
      builder.toStream(output);
      builder.run();
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    } finally {
      Files.walk(fontDirectory)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }

    return pdfFile;
  }

  protected void moveFontsToTempDir(Path fontDirectory) {
    List<URL> files = MetaScanner.findAll("axelor-base", "fonts", "(.*?)\\.ttf");
    ExecutorService executorService = Executors.newCachedThreadPool();
    for (URL file : files) {
      Runnable task =
          () -> {
            String[] parts = file.getFile().split("/");
            String name = parts[parts.length - 1];
            File tempFile = new File(fontDirectory.toString(), name);
            try (InputStream is = file.openStream();
                OutputStream os = new FileOutputStream(tempFile)) {
              IOUtils.copy(is, os);
            } catch (IOException e) {
              TraceBackService.trace(e);
            }
          };
      executorService.submit(task);
    }
    executorService.shutdown();
    while (true) {
      if (executorService.isTerminated()) {
        break;
      }
    }
  }

  private void setLogging() {
    XRLog.listRegisteredLoggers()
        .forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.OFF));
  }

  protected static String parseHtml(File htmlFile) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add(
        0,
        "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>"
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
            + "		        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
    lines.addAll(Files.readAllLines(htmlFile.toPath()));
    return lines.stream().collect(Collectors.joining("\n"));
  }
}
