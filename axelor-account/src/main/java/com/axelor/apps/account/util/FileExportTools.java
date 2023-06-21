package com.axelor.apps.account.util;

import com.axelor.app.AppSettings;
import com.axelor.common.FileUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

public class FileExportTools {

  // TODO: Move it in axelor-utils

  /**
   * Utils class to deal with exporting file to web client, say <code>response.setExportFile(Path)
   * </code>
   */
  protected static final String DEFAULT_EXPORT_DIR = "{java.io.tmpdir}/axelor/data-export";

  protected static final String EXPORT_PATH =
      AppSettings.get().getPath("data.export.dir", DEFAULT_EXPORT_DIR);

  private static final String[] OUTPUT_NAME_SEARCH_LIST =
      new String[] {"*", "\"", "/", "\\", "?", "%", ":", "|", "<", ">", "#"};
  private static final String[] OUTPUT_NAME_REPLACEMENT_LIST =
      new String[] {"_", "'", "_", "_", "_", "_", "_", "_", "_", "_", "_"};

  /**
   * An utility method to zip a list of files or metaFiles.
   *
   * @param zipFileName the name of the final zip file.
   * @param fileList the list of files.
   * @return a zip file containing all the given list.
   */
  public static Optional<Path> zip(String zipFileName, List<File> fileList) {
    try {
      if (CollectionUtils.isEmpty(fileList)) {
        return Optional.empty();
      }
      File zipFile = FileExportTools.getExportFile(zipFileName + ".zip");

      try (ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {

        for (File file : fileList) {
          zout.putNextEntry(new ZipEntry(file.getName()));
          zout.write(IOUtils.toByteArray(Files.newInputStream(file.toPath())));

          zout.closeEntry();
        }
      }
      return Optional.of(FileExportTools.relativeToExport(zipFile.toPath()));

    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static String formatFileName(String fileName) {
    return org.apache.commons.lang3.StringUtils.replaceEach(
        fileName, OUTPUT_NAME_SEARCH_LIST, OUTPUT_NAME_REPLACEMENT_LIST);
  }

  public static File getExportFile(String fileName) {
    return FileUtils.getFile(EXPORT_PATH, formatFileName(fileName));
  }

  public static Path relativeToExport(Path path) {
    return Paths.get(EXPORT_PATH).relativize(path);
  }
}
