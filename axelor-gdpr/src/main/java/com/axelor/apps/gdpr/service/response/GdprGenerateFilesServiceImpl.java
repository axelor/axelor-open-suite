/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.base.db.repo.AdvancedExportLineRepository;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.gdpr.db.GDPRDataToExcludeConfig;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.service.app.AppGdprService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.Inflector;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.GdprDmsFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdprGenerateFilesServiceImpl implements GdprGenerateFilesService {

  public static final String OUTPUT_FILE_NAME = "data";
  public static final String OUTPUT_EXT = ".zip";
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected AppGdprService appGdprService;
  protected MetaFiles metaFiles;
  protected MetaModelRepository metaModelRepository;
  protected MetaFieldRepository metaFieldRepository;
  protected GdprDmsFileRepository gdprDmsFileRepository;
  protected GdprResponseService gdprResponseService;
  protected AdvancedExportService advancedExportService;
  protected AdvancedExportLineRepository advancedExportLineRepository;
  protected AdvancedExportRepository advancedExportRepository;
  protected GdprDataToExcludeService dataToExcludeService;

  @Inject
  public GdprGenerateFilesServiceImpl(
      AppBaseService appBaseService,
      AppGdprService appGdprService,
      MetaFiles metaFiles,
      MetaModelRepository metaModelRepository,
      MetaFieldRepository metaFieldRepository,
      GdprDmsFileRepository gdprDmsFileRepository,
      GdprResponseService gdprResponseService,
      AdvancedExportService advancedExportService,
      AdvancedExportLineRepository advancedExportLineRepository,
      AdvancedExportRepository advancedExportRepository,
      GdprDataToExcludeService gdprDataToExcludeService) {
    this.appBaseService = appBaseService;
    this.appGdprService = appGdprService;
    this.metaFiles = metaFiles;
    this.metaModelRepository = metaModelRepository;
    this.metaFieldRepository = metaFieldRepository;
    this.gdprDmsFileRepository = gdprDmsFileRepository;
    this.gdprResponseService = gdprResponseService;
    this.advancedExportService = advancedExportService;
    this.advancedExportLineRepository = advancedExportLineRepository;
    this.advancedExportRepository = advancedExportRepository;
    this.dataToExcludeService = gdprDataToExcludeService;
  }

  @Override
  public MetaFile generateAccessResponseFile(
      GDPRRequest gdprRequest,
      Class<?> modelSelectKlass,
      MetaModel metaModel,
      AuditableModel selectedModel)
      throws AxelorException, ClassNotFoundException, IOException {
    List<File> files = new ArrayList<>();

    files.addAll(generateRelatedObjectsCSV(gdprRequest));

    files.addAll(generateCSVForThePerson(gdprRequest, modelSelectKlass, metaModel, selectedModel));

    files.addAll(searchDMSFile(gdprRequest));
    String exportFileName =
        OUTPUT_FILE_NAME
            .concat(
                appBaseService
                    .getTodayDateTime()
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
            .concat(OUTPUT_EXT);

    File zip = new File(exportFileName);
    MetaFile dataMetaFile = new MetaFile();
    dataMetaFile.setFileName(exportFileName);

    addFilesToZip(zip, files);
    metaFiles.upload(zip, dataMetaFile);

    return dataMetaFile;
  }

  /**
   * generate csv file for related objects
   *
   * @param gdprRequest
   * @return
   * @throws ClassNotFoundException
   * @throws AxelorException
   */
  @SuppressWarnings("unchecked")
  public List<File> generateRelatedObjectsCSV(GDPRRequest gdprRequest)
      throws ClassNotFoundException, AxelorException {
    List<File> generatedFiles = new ArrayList<>();

    List<MetaField> metaFields = gdprResponseService.selectMetaFields(gdprRequest);

    for (MetaField metaField : metaFields) {
      Class<? extends AuditableModel> klass =
          (Class<? extends AuditableModel>) Class.forName(metaField.getMetaModel().getFullName());
      List<? extends AuditableModel> records;

      if (!"OneToMany".equals(metaField.getRelationship())) {
        records =
            Query.of(klass).filter(metaField.getName() + " = " + gdprRequest.getModelId()).fetch();

      } else {
        records =
            Query.of(klass)
                .filter(gdprRequest.getModelId() + " MEMBER OF " + metaField.getName())
                .fetch();
      }

      if (!records.isEmpty()) {
        LOG.debug("records : {}", records);
        Optional.ofNullable(createCSV(records, metaField.getMetaModel()))
            .ifPresent(generatedFiles::add);
      }
    }
    return generatedFiles;
  }

  /**
   * generate csv file for chosen model (lead/partner)
   *
   * @param gdprRequest
   * @return
   * @throws AxelorException
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public List<File> generateCSVForThePerson(
      GDPRRequest gdprRequest,
      Class<?> modelSelectKlass,
      MetaModel metaModel,
      AuditableModel selectedModel)
      throws AxelorException, ClassNotFoundException, IOException {
    List<File> generatedFiles = new ArrayList<>();

    List<AuditableModel> list = new ArrayList<>();

    Mapper mapper = Mapper.of(selectedModel.getClass());

    list.add(selectedModel);
    generatedFiles.add(createCSV(list, metaModel));

    List<Property> binaryProperties =
        Arrays.stream(mapper.getProperties())
            .filter(property -> PropertyType.BINARY.equals(property.getType()))
            .collect(Collectors.toList());

    // check for pictures
    for (Property property : binaryProperties) {
      generatePicturesFiles(
          gdprRequest, modelSelectKlass, selectedModel, generatedFiles, mapper, property);
    }

    return generatedFiles;
  }

  /**
   * add linked dmsFiles
   *
   * @param gdprRequest
   * @return
   */
  protected List<File> searchDMSFile(GDPRRequest gdprRequest) {

    List<DMSFile> dmsFiles =
        gdprDmsFileRepository.findByModel(gdprRequest.getModelId(), gdprRequest.getModelSelect());

    return dmsFiles.stream()
        .map(dmsFile -> MetaFiles.getPath(dmsFile.getMetaFile()).toFile())
        .filter(File::exists)
        .collect(Collectors.toList());
  }

  /**
   * add files to archive
   *
   * @param source
   * @param files
   * @throws IOException
   */
  public void addFilesToZip(File source, List<File> files) throws IOException {
    MetaFiles.createTempFile(null, OUTPUT_EXT);

    File tmpZip = File.createTempFile(source.getName(), null);

    byte[] buffer = new byte[1024];

    try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(tmpZip.toPath()))) {
      try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(source.toPath()))) {
        for (File f : files) {
          try (InputStream in = Files.newInputStream(f.toPath())) {
            out.putNextEntry(new ZipEntry(f.getName()));
            for (int read = in.read(buffer); read > -1; read = in.read(buffer)) {
              out.write(buffer, 0, read);
            }
            out.closeEntry();
          }
        }

        for (ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()) {
          out.putNextEntry(ze);
          for (int read = zin.read(buffer); read > -1; read = zin.read(buffer)) {
            out.write(buffer, 0, read);
          }
          out.closeEntry();
        }

        Files.delete(tmpZip.toPath());
      }
    }
  }

  /**
   * add pictures in arhive convert MetaFile to File
   *
   * @param gdprRequest
   * @param modelSelectKlass
   * @param selectedModel
   * @param generatedFiles
   * @param mapper
   * @param property
   * @throws IOException
   */
  protected void generatePicturesFiles(
      GDPRRequest gdprRequest,
      Class<?> modelSelectKlass,
      AuditableModel selectedModel,
      List<File> generatedFiles,
      Mapper mapper,
      Property property)
      throws IOException {
    String fileName =
        modelSelectKlass.getSimpleName()
            + "_"
            + property.getName()
            + "_"
            + gdprRequest.getModelId()
            + ".png";
    Object value = mapper.get(selectedModel, property.getName());
    if (Objects.nonNull(value)) {
      String base64Img = new String((byte[]) value);
      String base64ImgData = base64Img.split(",")[1];
      byte[] img = Base64.getDecoder().decode(base64ImgData);
      ByteArrayInputStream inImg = new ByteArrayInputStream(img);

      MetaFile picture = metaFiles.upload(inImg, fileName);

      File file = MetaFiles.getPath(picture).toFile();

      generatedFiles.add(file);
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public File createCSV(List<? extends AuditableModel> references, MetaModel metaModel)
      throws ClassNotFoundException, AxelorException {
    AdvancedExport advancedExport = new AdvancedExport();
    advancedExport.setMetaModel(metaModel);
    advancedExport.setIncludeArchivedRecords(true);

    List<AdvancedExportLine> fieldsToExport = getAllFields(advancedExport);

    if (fieldsToExport.isEmpty()) {
      return null;
    }

    advancedExport.setAdvancedExportLineList(fieldsToExport);
    advancedExportRepository.save(advancedExport);
    File file;
    List<Long> recordsIds = new ArrayList<>();

    for (Object reference : references) {
      recordsIds.add((Long) Mapper.of(reference.getClass()).get(reference, "id"));
    }

    file = advancedExportService.export(advancedExport, recordsIds, AdvancedExportService.CSV);

    return file;
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public List<AdvancedExportLine> getAllFields(AdvancedExport advancedExport)
      throws ClassNotFoundException {

    // search dataToExclude configuration
    List<GDPRDataToExcludeConfig> dataToExcludeConfig =
        appGdprService.getAppGDPR().getDataToExcludeConfig();

    boolean excludeModel =
        dataToExcludeService.isModelExcluded(dataToExcludeConfig, advancedExport.getMetaModel());
    List<MetaField> metaFieldsToExclude = new ArrayList<>();

    if (excludeModel) {
      // if empty, means do not export full Model
      metaFieldsToExclude =
          dataToExcludeService.getFieldsToExclude(
              dataToExcludeConfig, advancedExport.getMetaModel());
    }

    // if model is excluded but no field is selected, exclude all object
    if (advancedExport.getMetaModel() == null || (excludeModel && metaFieldsToExclude.isEmpty())) {
      return Collections.emptyList();
    }

    List<AdvancedExportLine> advancedExportLineList = new ArrayList<>();
    List<MetaField> metaModelFieldsList = advancedExport.getMetaModel().getMetaFields();
    // remove fields that are configured in app gdpr
    metaModelFieldsList.removeAll(metaFieldsToExclude);

    Inflector inflector = Inflector.getInstance();

    for (MetaField field : metaModelFieldsList) {
      Optional.ofNullable(getAdvancedExportLineFromField(advancedExport, inflector, field))
          .ifPresent(advancedExportLineList::add);
    }
    return advancedExportLineList;
  }

  /**
   * generate AdvancedExportLine from the given field
   *
   * @param advancedExport
   * @param inflector
   * @param field
   * @return
   * @throws ClassNotFoundException
   */
  protected AdvancedExportLine getAdvancedExportLineFromField(
      AdvancedExport advancedExport, Inflector inflector, MetaField field)
      throws ClassNotFoundException {
    AdvancedExportLine advancedExportLine = new AdvancedExportLine();
    advancedExportLine.setCurrentDomain(advancedExport.getMetaModel().getName());

    Class<?> modelClass = Class.forName(advancedExport.getMetaModel().getFullName());
    Mapper modelMapper = Mapper.of(modelClass);

    if (modelMapper.getProperty(field.getName()) == null
        || modelMapper.getProperty(field.getName()).isTransient()) {
      return null;
    }

    if (!Strings.isNullOrEmpty(field.getRelationship())) {
      MetaModel metaModel =
          metaModelRepository.all().filter("self.name = ?", field.getTypeName()).fetchOne();

      Class<?> klass = Class.forName(metaModel.getFullName());
      Mapper mapper = Mapper.of(klass);
      String fieldName = mapper.getNameField() == null ? "id" : mapper.getNameField().getName();
      MetaField metaField =
          metaFieldRepository
              .all()
              .filter("self.name = ?1 AND self.metaModel = ?2", fieldName, metaModel)
              .fetchOne();
      advancedExportLine.setMetaField(metaField);
      advancedExportLine.setTargetField(field.getName() + "." + metaField.getName());
    } else {
      advancedExportLine.setMetaField(field);
      advancedExportLine.setTargetField(field.getName());
    }

    if (Strings.isNullOrEmpty(field.getLabel())) {
      advancedExportLine.setTitle(inflector.humanize(field.getName()));
    } else {
      advancedExportLine.setTitle(field.getLabel());
    }
    advancedExportLine.setAdvancedExport(advancedExport);
    advancedExportLineRepository.save(advancedExportLine);
    return advancedExportLine;
  }
}
