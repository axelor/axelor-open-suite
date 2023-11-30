package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentFamily;
import com.axelor.apps.intervention.db.repo.EquipmentLineRepository;
import com.axelor.apps.intervention.exception.IExceptionMessage;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.utils.CSVImportProcessParams;
import com.axelor.apps.intervention.utils.CSVImportTool;
import com.axelor.apps.intervention.utils.FileExportTools;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EquipmentServiceImpl implements EquipmentService {

  protected static final Logger LOG = LoggerFactory.getLogger(EquipmentService.class);
  protected final EquipmentRepository equipmentRepository;
  protected final EquipmentLineRepository equipmentLineRepository;

  protected AppInterventionService appInterventionService;

  protected final List<String> processedFields =
      Arrays.asList(
          "sequence",
          "code",
          "name",
          "typeSelect",
          "inService",
          "commissioningDate",
          "contract",
          "comments",
          "parentEquipment",
          "equipmentFamily");

  @Inject
  public EquipmentServiceImpl(
      EquipmentRepository equipmentRepository,
      EquipmentLineRepository equipmentLineRepository,
      AppInterventionService appInterventionService) {
    this.equipmentRepository = equipmentRepository;
    this.equipmentLineRepository = equipmentLineRepository;
    this.appInterventionService = appInterventionService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeEquipment(Equipment equipment) throws AxelorException {
    try {
      equipmentRepository.remove(equipment);
    } catch (IllegalArgumentException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }

  @Override
  public Path importEquipments(Long partnerId, MetaFile metaFile)
      throws AxelorException, IOException {
    Path path = Files.createTempFile("importLog-", ".log");
    setPermissionsSafe(path);
    try (CSVImportTool csvImportTool = new CSVImportTool(MetaFiles.getPath(metaFile), path)) {
      csvImportTool.parseAllLines(
          true, 20, it -> parseEquipmentLine(it, Mapper.of(Equipment.class), partnerId));
    } catch (IllegalArgumentException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(I18n.get(IExceptionMessage.CSV_IMPORT_ERROR), metaFile.getFileName()));
    }
    LOG.debug("Exporting log file...");
    return FileExportTools.toExport(path.toFile().getName(), path, true);
  }

  protected String getFieldTitle(Mapper mapper, int index) {
    return I18n.get(mapper.getProperty(processedFields.get(index)).getTitle());
  }

  @Transactional(rollbackOn = Exception.class)
  protected Equipment parseEquipmentLine(
      CSVImportProcessParams params, Mapper mapper, Long partnerId) {
    Partner partner = JpaRepository.of(Partner.class).find(partnerId);
    String sequence =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 0)),
            null,
            false,
            null,
            null,
            params.getLoggerTool(),
            s -> s);
    String code =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 1)),
            null,
            true,
            I18n.get(IExceptionMessage.EQUIP_IMPORT_2),
            null,
            params.getLoggerTool(),
            s -> s);
    String name =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 2)),
            null,
            true,
            I18n.get(IExceptionMessage.EQUIP_IMPORT_3),
            null,
            params.getLoggerTool(),
            s -> s);
    String typeSelect =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 3)),
            null,
            true,
            I18n.get(IExceptionMessage.EQUIP_IMPORT_4),
            null,
            params.getLoggerTool(),
            s -> s);
    boolean inService =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 4)),
            false,
            false,
            null,
            null,
            params.getLoggerTool(),
            this::parseBoolean);
    LocalDate commissioningDate =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 5)),
            null,
            false,
            null,
            null,
            params.getLoggerTool(),
            s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")));

    Contract contract =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 6)),
            null,
            false,
            null,
            String.format(
                I18n.get(IExceptionMessage.EQUIP_IMPORT_7),
                params.getParamValue(getFieldTitle(mapper, 6))),
            params.getLoggerTool(),
            s ->
                JPA.all(Contract.class)
                    .filter("self.contractId = :contractId")
                    .bind("contractId", s)
                    .fetchOne());
    String comments =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 7)),
            null,
            false,
            null,
            null,
            params.getLoggerTool(),
            s -> s);
    Equipment parentEquipment =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 8)),
            null,
            false,
            null,
            String.format(
                I18n.get(IExceptionMessage.EQUIP_IMPORT_8),
                params.getParamValue(getFieldTitle(mapper, 8))),
            params.getLoggerTool(),
            s ->
                JPA.all(Equipment.class)
                    .filter("self.sequence = :sequence")
                    .bind("sequence", s)
                    .fetchOne());
    EquipmentFamily equipmentFamily =
        CSVImportTool.processField(
            params.getParamValue(getFieldTitle(mapper, 9)),
            null,
            false,
            null,
            String.format(
                I18n.get(IExceptionMessage.EQUIP_IMPORT_9),
                params.getParamValue(getFieldTitle(mapper, 9))),
            params.getLoggerTool(),
            s ->
                JPA.all(EquipmentFamily.class)
                    .filter("self.code = :code")
                    .bind("code", s)
                    .fetchOne());

    Equipment equipment;
    if (StringUtils.notBlank(sequence)) {
      equipment =
          JPA.all(Equipment.class)
              .filter("self.sequence = :sequence")
              .bind("sequence", sequence)
              .fetchOne();
      if (equipment == null) {
        throw new IllegalArgumentException(
            String.format(I18n.get(IExceptionMessage.EQUIP_IMPORT_1), sequence));
      }
    } else {
      equipment = new Equipment();
    }
    equipment.setPartner(partner);
    equipment.setCode(code);
    equipment.setName(name);
    equipment.setTypeSelect(typeSelect);
    equipment.setInService(inService);
    equipment.setCommissioningDate(commissioningDate);
    equipment.setContract(contract);
    equipment.setComments(comments);
    equipment.setParentEquipment(parentEquipment);
    equipment.setEquipmentFamily(equipmentFamily);

    return equipmentRepository.save(equipment);
  }

  boolean parseBoolean(String bool) {
    return bool != null && (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("vrai"));
  }

  @Override
  public MetaFile loadFormatFile() {
    return appInterventionService.getAppIntervention().getAssetsImportFormat();
  }

  @Override
  public String getProcessedFields() {
    Mapper mapper = Mapper.of(Equipment.class);
    return processedFields.stream()
        .map(it -> I18n.get(mapper.getProperty(it).getTitle()))
        .collect(Collectors.joining(", "));
  }

  private void setPermissionsSafe(Path filePath) throws IOException {
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
}
