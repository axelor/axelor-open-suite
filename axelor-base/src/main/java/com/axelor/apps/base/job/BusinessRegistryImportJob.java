package com.axelor.apps.base.job;

import com.axelor.apps.base.db.*;
import com.axelor.apps.base.db.repo.*;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.axelor.apps.base.db.repo.PartnerRepository.PARTNER_TYPE_COMPANY;
import static com.axelor.apps.base.db.repo.SequenceRepository.PARTNER;
import static java.lang.String.format;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;

public class BusinessRegistryImportJob implements Job {

    private static final int TRANSACTION_SIZE = 20;
    private static final String KEY = "url";
    private static final Logger log = LoggerFactory.getLogger(BusinessRegistryImportJob.class);
    private static final String SEQUENCE_FORMAT = "P%04d"; // Mimic Axelor's default sequence for partners (letter 'P' + 4 digits)
    private Country DEFAULT_COUNTRY;
    private Currency DEFAULT_CURRENCY;
    private Language DEFAULT_LANGUAGE;
    private SequenceVersion sequenceVersion;
    private Long sequence;

    @Inject
    private PartnerService partnerService;
    @Inject
    private PartnerRepository partnerRepository;
    @Inject
    private AddressService addressService;
    @Inject
    private CurrencyRepository currencyRepository;
    @Inject
    private CountryRepository countryRepository;
    @Inject
    private LanguageRepository languageRepository;
    @Inject
    private SequenceRepository sequenceRepository;
    @Inject
    private SequenceVersionRepository sequenceVersionRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail detail = context.getJobDetail();
        String urlParam = detail.getJobDataMap().getString(KEY);
        String jobName = detail.getKey().getName();

        log.info("Job fired: {}", jobName);

        URL url;
        try {
            url = new URL(urlParam);
        } catch (MalformedURLException e) {
            throw new JobExecutionException(
                    format("Please check the parameter 'url' for the job. Current value is '%s'.", urlParam), e);
        }

        File outputFile;
        try {
            Path dir = createTempDirectory(null);
            dir.toFile().deleteOnExit();
            Path file = createTempFile(dir, "ariregister", ".zip");
            outputFile = file.toFile();
        } catch (IOException e) {
            log.error("Cannot create temp file", e);
            throw new JobExecutionException(e);
        }

        log.info("Downloading file {} into {}.", urlParam, outputFile.getAbsolutePath());

        try (InputStream inputStream = url.openStream();
             ReadableByteChannel readChannel = Channels.newChannel(inputStream);
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             FileChannel writeChannel = outputStream.getChannel()) {
            writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            log.error(format("Cannot download file %s.", urlParam), e);
        }

        log.info("Downloading completed.");

        File unzippedFile = null;
        try {
            log.info("Extracting {}", outputFile.getAbsolutePath());
            unzippedFile = unZip(outputFile);
        } catch (IOException e) {
            log.error(format("Cannot unzip file %s: %s", outputFile.getAbsolutePath(), e.getMessage()), e);
        }

        log.info("Extracting completed. Extracted file: {}", unzippedFile.getAbsolutePath());

        long startedAt = System.currentTimeMillis();
        log.info("Starting import");

        manualImport(unzippedFile);

        long duration = (System.currentTimeMillis() - startedAt) / 1000;
        log.info("Import completed in {} minutes and {} seconds", duration / 60, duration % 60);

        log.info("Job execution completed");
    }

    private void manualImport(File file) throws JobExecutionException {
        //default values for new partners
        DEFAULT_COUNTRY = countryRepository.findByCode("EST");
        DEFAULT_CURRENCY = currencyRepository.findByCode("EUR");
        DEFAULT_LANGUAGE = languageRepository.findByCode("et");

        sequenceVersion = sequenceVersionRepository.findEndless(sequenceRepository.findByCodeSelect(PARTNER), LocalDate.now());
        sequence = sequenceVersion.getNextNum();

        int totalInserted = 0;
        int totalUpdated = 0;
        List<Partner> newPartners = new ArrayList<>();

        log.info("Loading existing companies from DB");
//        List<Partner> partners = partnerRepository.all().cacheable().fetch();
        List<PartnerDto> partners = JPA.em().createQuery(
                "select new com.axelor.apps.base.job.PartnerDto(p.id, p.registrationCode, p.name, p.taxNbr, a.addressL4) " +
                        "from Partner p " +
                        "join p.mainAddress a " +
                        "where p.partnerTypeSelect = 1", PartnerDto.class)
                .getResultList();

        log.info("Loaded {} companies", partners.size());

        try (
                Reader reader = Files.newBufferedReader(file.toPath());
                CSVParser csvFile = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';'))
        ) {

            log.info("Comparing with CSV file");
            for (CSVRecord csvRecord : csvFile) {
                String kood = csvRecord.get("ariregistri_kood");
                String name = csvRecord.get("\uFEFFnimi");
                String kmkr = csvRecord.get("kmkr_nr");
                String street = csvRecord.get("asukoht_ettevotja_aadressis");
                String city = csvRecord.get("asukoha_ehak_tekstina");
                String index = csvRecord.get("indeks_ettevotja_aadressis");

                if (street.isEmpty() || name.length() > 200) {
                    continue;
                }

//                Partner partner = partners.stream()
//                        .filter(p -> kood.equals(p.getRegistrationCode()))
//                        .findFirst()
//                        .orElse(null);
                if (partners.stream().anyMatch(
                        p -> Objects.equals(p.getRegistrationCode(), kood)
                                && Objects.equals(p.getName(), name)
                                && Objects.equals(p.getTaxNbr(), kmkr)
                                && Objects.equals(p.getAddressL4(), street))
                ) {
                    continue;
                }

                Partner partner = partners.stream()
                        .filter(p -> kood.equals(p.getRegistrationCode()))
                        .findFirst()
                        .map(p -> partnerRepository.find(p.getId()))
                        .orElse(null);

                if (partner == null) {
                    newPartners.add(foundNewPartner(kood, name, kmkr, street, city, index));

                    if (newPartners.size() >= TRANSACTION_SIZE) {
                        insertInTransaction(newPartners);
                        totalInserted += newPartners.size();
                        newPartners.clear();
                        if (totalInserted % 1000 == 0) {
                            log.info("Saved {} partners", totalInserted);
                        }
                    }

                } else {
                    updateInTransaction(partner, name, kmkr, street, city, index);
                    totalUpdated++;
                }

            }

            if (newPartners.size() > 0) { // for those partners that didn't make transaction
                insertInTransaction(newPartners);
                totalInserted += newPartners.size();
            }

            log.info("Added {} new companies", totalInserted);
            log.info("Updated {} existing companies", totalUpdated);

        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            throw new JobExecutionException(e);
        }
    }

    private void insertInTransaction(List<Partner> partners) {
        JPA.em().getTransaction().begin();
        partners.forEach(partner -> JPA.em().persist(partner));
        sequenceVersion.setNextNum(sequence);
        sequenceVersionRepository.save(sequenceVersion);

        JPA.em().getTransaction().commit();
        JPA.em().clear();

        //restore detached values after clear
        DEFAULT_COUNTRY = countryRepository.findByCode("EST");
        DEFAULT_CURRENCY = currencyRepository.findByCode("EUR");
        DEFAULT_LANGUAGE = languageRepository.findByCode("et");
        sequenceVersion = sequenceVersionRepository.findEndless(sequenceRepository.findByCodeSelect(PARTNER), LocalDate.now());
    }

    private void updateInTransaction(Partner partner, String name, String kmkr, String street, String city, String index) {
        boolean hasChanges = false;

        if (!Objects.equals(partner.getName(), name)) {
            log.debug("{} changed name to [{}]", partner.getName(), name);
            partner.setName(name);
            partnerService.setPartnerFullName(partner);
            hasChanges = true;
        }

        if (!Objects.equals(partner.getTaxNbr(), kmkr)) {
            log.debug("{} changed KMKR [{}] -> [{}]", name, partner.getTaxNbr(), kmkr);
            partner.setTaxNbr(kmkr);
            hasChanges = true;
        }

        Address address = partner.getMainAddress();
        if (address != null && !Objects.equals(address.getAddressL4(), street)) {
            log.debug("{} changed address [{}] -> [{}]", name, address.getAddressL4(), street);
            address.setAddressL4(street);
            address.setAddressL6(city + " " + index);
            address.setAddressL7Country(DEFAULT_COUNTRY);
            address.setFullName(addressService.computeFullName(address));
            hasChanges = true;
        }

        if (hasChanges) {
            JPA.em().getTransaction().begin();
            JPA.save(partner);
            JPA.em().getTransaction().commit();
        }
    }

    private Partner foundNewPartner(String kood, String name, String kmkr, String street, String city, String index) {
        log.trace("New company [{}]", name);

        Partner partner = new Partner();
        partner.setRegistrationCode(kood);
        partner.setName(name);
        partner.setTaxNbr(kmkr);
        partner.setIsContact(false);
        partner.setIsCustomer(false);
        partner.setIsProspect(false);
        partner.setCurrency(DEFAULT_CURRENCY);
        partner.setLanguage(DEFAULT_LANGUAGE);
        partner.setPartnerTypeSelect(PARTNER_TYPE_COMPANY);
        partner.setPartnerSeq(format(SEQUENCE_FORMAT, sequence++));

        Address address = new Address();
        address.setAddressL4(street);
        address.setAddressL6(city + " " + index);
        address.setAddressL7Country(DEFAULT_COUNTRY);
        address.setFullName(addressService.computeFullName(address));

        PartnerAddress partnerAddress = new PartnerAddress();
        partnerAddress.setAddress(address);
        partnerAddress.setIsDefaultAddr(true);
        partnerAddress.setIsDeliveryAddr(true);
        partnerAddress.setIsInvoicingAddr(true);

        partnerService.setPartnerFullName(partner);
        partner.addPartnerAddressListItem(partnerAddress);
        partner.setMainAddress(address);

        return partner;
    }

    private File unZip(File zipFile) throws IOException {
        File file;
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            file = new File(zipFile.getParent(), zipEntry.getName());
            byte[] buffer = new byte[1024];
            int count;

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                while ((count = zipInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, count);
                }
            }

            zipInputStream.closeEntry();
        }
        return file;
    }
}
