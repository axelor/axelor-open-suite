/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2020 Axelor (<http://axelor.com>).
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
package com.axelor.gradle.tasks;

import com.axelor.gradle.tasks.changelog.ChangelogEntry;
import com.axelor.gradle.tasks.changelog.ChangelogEntryParser;
import com.axelor.gradle.tasks.changelog.Release;
import com.axelor.gradle.tasks.changelog.ReleaseGenerator;
import com.axelor.gradle.tasks.changelog.ReleaseProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

public class GenerateAosChangelog extends DefaultTask {

  private static final String CHANGELOG_PATH = "CHANGELOG.md";

  private final String version = getProject().getVersion().toString();

  private boolean preview;

  @Option(option = "preview", description = "Don’t actually write/delete anything, just print")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  @InputFiles @SkipWhenEmpty private FileTree files;

  public FileTree getFiles() {
    return files;
  }

  public void setFiles(FileTree files) {
    this.files = files;
  }

  @TaskAction
  public void generate() throws IOException {
    List<ChangelogEntry> entries = getChangelogEntries();

    if (entries.isEmpty()) {
      getLogger().lifecycle("No unreleased change log entries to process");
      return;
    }

    String newChangelog = generate(entries);

    if (preview) {
      getLogger().lifecycle("Generated change log : ");
      getLogger().lifecycle("--------------------");
      getLogger().lifecycle(newChangelog);
      getLogger().lifecycle("--------------------");
      return;
    }

    write(newChangelog);
    clean();
  }

  private List<ChangelogEntry> getChangelogEntries() throws IOException {
    getLogger().lifecycle("Processing unreleased change log entries");
    ChangelogEntryParser parser = new ChangelogEntryParser();
    List<ChangelogEntry> entries = new ArrayList<>();
    for (File file : getFiles()) {
      getLogger().debug("Processing {}", file);
      entries.add(parser.parse(file));
    }
    return entries;
  }

  private String generate(List<ChangelogEntry> entries) {
    ReleaseProcessor processor = new ReleaseProcessor();
    Release release = processor.process(entries, version);

    ReleaseGenerator generator = new ReleaseGenerator();
    return generator.generate(release);
  }

  private void write(String newChangelog) throws IOException {
    getLogger().lifecycle("Generating new CHANGELOG.md file");

    String bottomLink = "";
    Optional<String> previousVersion = computePreviousVersion();
    if (previousVersion.isPresent()) {
      bottomLink = computeBottomLink(previousVersion.get());
    }

    File changelogFile = new File(CHANGELOG_PATH);

    StringBuilder contentBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(changelogFile))) {

      String sCurrentLine;
      while ((sCurrentLine = br.readLine()) != null) {
        if (previousVersion.isPresent()
            && sCurrentLine.startsWith("[" + previousVersion.get() + "]")) {
          contentBuilder.append(bottomLink).append(System.lineSeparator());
        }
        contentBuilder.append(sCurrentLine).append(System.lineSeparator());
      }
    }

    changelogFile.delete();

    try (FileOutputStream fos = new FileOutputStream(changelogFile)) {
      fos.write((newChangelog + contentBuilder.toString()).getBytes());
      fos.flush();
    }
  }

  private void clean() {
    getLogger().lifecycle("Clean up unreleased change log entries");
    for (File file : getFiles()) {
      try {
        getLogger().lifecycle("Deleting {}", file);
        Files.delete(file.toPath());
      } catch (IOException ex) {
        throw new GradleException("Could not delete file: " + file, ex);
      }
    }
  }

  private String computeBottomLink(String previousVersion) {
    return String.format(
        "[%s]: https://github.com/axelor/axelor-open-suite/compare/v%s...v%s",
        version, previousVersion, version);
  }

  /**
   * Only compute the previous version if this is a correction version (e.g. if version is 6.0.6
   * then will return 6.0.7) But if the last number of the version is 0 we return {@link
   * Optional#empty}
   */
  private Optional<String> computePreviousVersion() {
    String[] versionArray = version.split("\\.");
    if (versionArray.length < 2) {
      return Optional.empty();
    }
    int lastNumber = Integer.parseInt(versionArray[versionArray.length - 1]);
    if (lastNumber <= 0) {
      return Optional.empty();
    } else {
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < versionArray.length - 1; i++) {
        stringBuilder.append(versionArray[i]);
        stringBuilder.append(".");
      }
      stringBuilder.append(lastNumber - 1);
      return Optional.of(stringBuilder.toString());
    }
  }
}
