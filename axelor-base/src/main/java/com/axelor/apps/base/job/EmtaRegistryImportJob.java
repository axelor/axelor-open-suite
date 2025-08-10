package com.axelor.apps.base.job;

import com.axelor.apps.base.db.IndustrySector;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerTurnover;
import com.axelor.apps.base.db.repo.IndustrySectorRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PartnerTurnoverRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;

public class EmtaRegistryImportJob implements Job {

    private static final short TRANSACTION_SIZE = 50;
    private static final short CSV_REGISTRY = 0;
    private static final short CSV_INDUSTRY_SECTOR = 3;
    private static final short CSV_TURNOVER = 7;
    private static final short CSV_EMPLOYEES = 8;
    private static final Logger log = LoggerFactory.getLogger(EmtaRegistryImportJob.class);

    @Inject
    private PartnerRepository partnerRepository;
    @Inject
    private IndustrySectorRepository sectorRepository;
    @Inject
    private PartnerTurnoverRepository turnoverRepository;


    /**
     * Calculates new url from example for current year quarter
     * @param urlExample EMTA url
     * @return Calculated url for current date
     */
    private String getUrl(String urlExample) {

        // Get current date
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        double month = localDate.getMonthValue();
        int year =  localDate.getYear();

        // Calculate current quarter
        int currentQuarter = (int) Math.round(month/3);

        // Convert number to roman string
        String romanNumberString = convertToRoman( currentQuarter - 1 == 0 ? 4 : currentQuarter - 1);

        // Calculate year to put into string
        int yearToPut =  year;
        if(convert(romanNumberString) == 4) {
            yearToPut -= 1;
        }

        String[] a = urlExample.split("_");

        a[2] = String.valueOf(yearToPut);
        a[3] = romanNumberString;

        return String.join("_", a);

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail detail = context.getJobDetail();
        String urlParam = detail.getJobDataMap().getString("url");
        String yearParam = detail.getJobDataMap().getString("year");
        String quarterParam = detail.getJobDataMap().getString("quarter");
        boolean urlAutoUpdateEnabled = detail.getJobDataMap().getBoolean("urlAutoUpdate");

        long startedAt = System.currentTimeMillis();
        log.info("Job started");

        if (urlAutoUpdateEnabled) {
            urlParam = getUrl(urlParam);
        }

        String[] split = urlParam.split("/");
        String fileName = split[split.length - 1];
        split = fileName.split("_");
        int year = yearParam == null ? Integer.parseInt(split[2]) : Integer.parseInt(yearParam);
        int quarter = quarterParam == null ? convert(split[3]) : Integer.parseInt(quarterParam);

        log.info("Url {}", urlParam);
        log.info("Filename {}", fileName);
        log.info("Year {}, quarter: {}", year, quarter);

        List<Partner> transactionPartners = new ArrayList<>();
        List<PartnerTurnover> transactionTurnover = new ArrayList<>();

        try (InputStream inputStream = new URL(urlParam).openConnection().getInputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            int counter = 0;
            for (int line = 1; line <= sheet.getLastRowNum(); line++) {
                XSSFRow row = sheet.getRow(line);

                int offset = row.getPhysicalNumberOfCells() == 10 ? 1 : 0; //workaround for different file format year 2019
                String registry = row.getCell(CSV_REGISTRY).getStringCellValue();
                String sector = row.getCell(CSV_INDUSTRY_SECTOR + offset).getStringCellValue();
                int turnover = parseCell(row.getCell(CSV_TURNOVER + offset));
                int employees = parseCell(row.getCell(CSV_EMPLOYEES + offset));

                Partner partner = partnerRepository.findByRegistrationCode(registry);
                if (partner != null) {

                    if (turnoverRepository.findByPartnerAndYearAndQuarter(partner, year, quarter) == null) {
                        PartnerTurnover partnerTurnover = new PartnerTurnover();
                        partnerTurnover.setPartner(partner);
                        partnerTurnover.setYear(year);
                        partnerTurnover.setQuarter(quarter);
                        partnerTurnover.setSaleTurnover(turnover);
                        transactionTurnover.add(partnerTurnover);
                    }

                    partner.setIndustrySector(getOrNewSector(sector));
                    partner.setNbrEmployees(employees);
                    transactionPartners.add(partner);

                    if (transactionPartners.size() >= TRANSACTION_SIZE) {
                        makeTransaction(transactionPartners, transactionTurnover);
                        counter += transactionPartners.size();

                        transactionPartners.clear();
                        transactionTurnover.clear();

                        if (counter % 1000 == 0) {
                            log.info("Updated {} partners", counter);
                        }
                    }
                }

            }

            if (transactionPartners.size() >= TRANSACTION_SIZE) {
                makeTransaction(transactionPartners, transactionTurnover);
                counter += transactionPartners.size();

                transactionPartners.clear();
                transactionTurnover.clear();
            }

            log.info("Updated {} partners", counter);

        } catch (IOException e) {
            throw new JobExecutionException(e);
        }

        long duration = (System.currentTimeMillis() - startedAt) / 1000;
        log.info("Job finished in {} minutes and {} seconds", duration / 60, duration % 60);
    }

    private void makeTransaction(List<Partner> partners, List<PartnerTurnover> turnovers) {
        JPA.em().getTransaction().begin();
        partners.forEach(p -> JPA.em().merge(p));
        turnovers.forEach(t -> JPA.em().persist(t));
        JPA.em().getTransaction().commit();
        JPA.em().clear();
    }

    private IndustrySector getOrNewSector(String name) {
        if (name.isEmpty()) {
            return null;
        }

        IndustrySector sector = sectorRepository.findByName(name);

        if (sector == null) {
            IndustrySector newSector = new IndustrySector(name);
            JPA.runInTransaction(() -> sectorRepository.save(newSector));
            log.info("Added new IndustrySector [{}]", name);
            return newSector;
        }

        return sector;
    }

    private int parseCell(XSSFCell cell) {
        if (cell.getCellType() == CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).intValue();
        } else {
            String value = cell.getStringCellValue();
            return value.isEmpty() ? 0 : Integer.parseInt(value);
        }
    }

    private int convert(String s) {
        switch (s) {
            case "i":
                return 1;
            case "ii":
                return 2;
            case "iii":
                return 3;
            case "iv":
                return 4;
            default:
                throw new IllegalArgumentException("Failed to convert roman number: " + s);
        }
    }

    private String convertToRoman(int i) {
        switch (i) {
            case 1:
                return "i";
            case 2:
                return "ii";
            case 3:
                return "iii";
            case 4:
                return "iv";
            default:
                throw new IllegalArgumentException("Failed to convert number to roman: " + i);
        }
    }

}
