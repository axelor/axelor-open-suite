/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.intervention.utils;

import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTools {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String[] OUTPUT_NAME_SEARCH_LIST =
      new String[] {"*", "\"", "/", "\\", "?", "%", ":", "|", "<", ">", "#"};
  private static final String[] OUTPUT_NAME_REPLACEMENT_LIST =
      new String[] {"_", "'", "_", "_", "_", "_", "_", "_", "_", "_", "_"};
  private final MetaFiles metaFiles;

  @Inject
  public FileTools(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  public static String formatFileName(String fileName) {
    return StringUtils.replaceEach(fileName, OUTPUT_NAME_SEARCH_LIST, OUTPUT_NAME_REPLACEMENT_LIST);
  }

  /**
   * An utility method to zip a list of files or metaFiles.
   *
   * @param zipFileName the name of the final zip file.
   * @param tClass the class of files (File or MetaFile).
   * @param files the list of files.
   * @param <T> the generic parameter.
   * @return a zip file containing all the given list.
   */
  @SafeVarargs
  public static <T> Optional<Path> zip(String zipFileName, Class<T> tClass, T... files) {
    return zip(zipFileName, tClass, Arrays.asList(files));
  }

  /**
   * An utility method to zip a list of files or metaFiles.
   *
   * @param zipFileName the name of the final zip file.
   * @param tClass the class of files (File or MetaFile).
   * @param fileList the list of files.
   * @param <T> the generic parameter.
   * @return a zip file containing all the given list.
   */
  public static <T> Optional<Path> zip(String zipFileName, Class<T> tClass, List<T> fileList) {
    try {
      if (CollectionUtils.isEmpty(fileList)
          || (!tClass.equals(File.class) && !tClass.equals(MetaFile.class))) {
        return Optional.empty();
      }
      File zipFile = FileExportTools.getExportFile(zipFileName + ".zip");
      try (ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
        for (T file : fileList) {
          zout.putNextEntry(
              new ZipEntry(
                  file instanceof MetaFile
                      ? ((MetaFile) file).getFileName()
                      : ((File) file).getName()));
          zout.write(
              IOUtils.toByteArray(
                  Files.newInputStream(
                      ((file instanceof MetaFile)
                              ? MetaFiles.getPath((MetaFile) file).toFile()
                              : (File) file)
                          .toPath())));
          zout.closeEntry();
        }
      }
      return Optional.of(FileExportTools.relativeToExport(zipFile.toPath()));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static String convertToBase64Uri(MetaFile metaFile) throws IOException {
    Path path = MetaFiles.getPath(metaFile);
    return String.format(
        "data:%s;base64,%s",
        Files.probeContentType(path), Base64.getEncoder().encodeToString(Files.readAllBytes(path)));
  }

  public static void setPermissionsSafe(Path filePath) throws IOException {
    Set<PosixFilePermission> perms = new HashSet<>();
    // user permission
    perms.add(PosixFilePermission.OWNER_READ);
    perms.add(PosixFilePermission.OWNER_WRITE);
    perms.add(PosixFilePermission.OWNER_EXECUTE);
    // group permissions
    perms.add(PosixFilePermission.GROUP_READ);
    perms.add(PosixFilePermission.GROUP_EXECUTE);
    // others permissions removed
    perms.remove(PosixFilePermission.OTHERS_READ);
    perms.remove(PosixFilePermission.OTHERS_WRITE);
    perms.remove(PosixFilePermission.OTHERS_EXECUTE);

    Files.setPosixFilePermissions(filePath, perms);
  }

  public MetaFile createFromBase64(String base64) {
    try {
      String header = base64.contains(",") ? base64.split(",")[0] : null;
      String dataStr = base64.contains(",") ? base64.split(",")[1] : base64;
      String extension = "";
      if (header != null) {
        String dataType = header.split(";")[0].split(":")[1];
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        MimeType jpeg = allTypes.forName(dataType);
        extension = jpeg.getExtension();
      }
      byte[] data = Base64.getDecoder().decode(dataStr);
      Path path = Files.createTempFile(null, extension);
      Files.write(path, data);
      return metaFiles.upload(path.toFile());
    } catch (Exception e) {
      return null;
    }
  }

  private static Comparator<File> fileComparator() {
    return (f1, f2) -> {
      if (f1.isFile() && f2.isDirectory()) {
        return -1;
      } else if (f1.isDirectory() && f2.isFile()) {
        return 1;
      } else {
        return f1.getName().compareTo(f2.getName());
      }
    };
  }

  private static File findZipFile(File dir, String zipFileName) {
    File[] files = dir.listFiles();
    if (files == null) {
      return null;
    }
    Arrays.sort(files, fileComparator());
    for (File file : files) {
      if (file.isDirectory()) {
        File zipFile = findZipFile(file, zipFileName);
        if (zipFile != null) {
          return zipFile;
        }
      } else if (file.getName().equals(zipFileName)) {
        return file;
      }
    }
    return null;
  }

  public static ZipFile openZipFile(String path, String zipFileName) {
    File root = new File(path);
    File zipFile = findZipFile(root, zipFileName);

    if (zipFile == null) {
      throw new IllegalArgumentException(I18n.get("No valid zip file found."));
    }

    try {
      ZipFile zf = new ZipFile(zipFile);
      log.info("Found zip file: {}", zipFile.getAbsolutePath());
      return zf;
    } catch (ZipException e) {
      throw new IllegalStateException(
          String.format(
              I18n.get("Zip file %s is corrupted: %s"), zipFile.getAbsolutePath(), e.getMessage()));
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format(
              I18n.get("Error opening zip file %s: %s"),
              zipFile.getAbsolutePath(),
              e.getMessage()));
    }
  }

  public static void processFiles(ZipFile zip, BiConsumer<String, InputStream> consumer) {
    Enumeration<? extends ZipEntry> entries = zip.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.isDirectory()) {
        continue;
      }
      try {
        InputStream inputStream = zip.getInputStream(entry);
        consumer.accept(entry.getName(), inputStream);
      } catch (Exception e) {
        log.error("Error processing zip file entry: {}", entry.getName(), e);
      }
    }
  }

  public static long getFilesSize(File directory) {
    long size = 0;
    if (directory.isDirectory()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isFile()) {
            size += file.length();
          } else {
            size += getFilesSize(file);
          }
        }
      }
    }
    return size;
  }

  public static String getHumanReadableSize(File directory) {
    long bytes = getFilesSize(directory);
    BigDecimal size = BigDecimal.valueOf(bytes);
    if (bytes < 1024) {
      return size.setScale(2, RoundingMode.HALF_UP) + " octets";
    } else if (bytes < 1024 * 1024) {
      return size.divide(BigDecimal.valueOf(1024L), 2, RoundingMode.HALF_UP) + " Ko";
    } else if (bytes < 1024 * 1024 * 1024) {
      return size.divide(BigDecimal.valueOf(1024L * 1024L), 2, RoundingMode.HALF_UP) + " Mo";
    } else {
      return size.divide(BigDecimal.valueOf(1024L * 1024L * 1024L), 2, RoundingMode.HALF_UP)
          + " Go";
    }
  }
}
