package com.axelor.apps.intervention.utils;

import com.axelor.app.AppSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utils class to deal with exporting file to web client, say <code>response.setExportFile(Path)
 * </code>
 */
public class FileExportTools {

  protected static final String DEFAULT_EXPORT_DIR = "{java.io.tmpdir}/axelor/data-export";
  protected static final String EXPORT_PATH =
      AppSettings.get().getPath("data.export.dir", DEFAULT_EXPORT_DIR);

  protected static final CopyOption[] OPTIONS = {StandardCopyOption.REPLACE_EXISTING};

  private FileExportTools() {}

  /**
   * For a given path and fileName, return a Path ready to be downloaded.
   *
   * @param fileName the file name
   * @param path file path
   * @return path
   * @throws IOException
   */
  public static Path toExport(String fileName, Path path, boolean move) throws IOException {
    File exportFile = com.axelor.common.FileUtils.getFile(EXPORT_PATH, fileName);
    if (Files.notExists(Paths.get(EXPORT_PATH))) {
      Files.createDirectories(Paths.get(EXPORT_PATH));
    }
    if (move) {
      Files.move(path, exportFile.toPath(), OPTIONS);
    } else {
      Files.copy(path, exportFile.toPath(), OPTIONS);
    }

    return relativeToExport(exportFile.toPath());
  }

  public static File getExportFile(String fileName) {
    return com.axelor.common.FileUtils.getFile(EXPORT_PATH, FileTools.formatFileName(fileName));
  }

  public static Path relativeToExport(Path path) {
    return Paths.get(EXPORT_PATH).relativize(path);
  }
}
