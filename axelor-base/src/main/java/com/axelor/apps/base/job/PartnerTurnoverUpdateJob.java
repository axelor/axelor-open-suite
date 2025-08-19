package com.axelor.apps.base.job;

import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.Query;

public class PartnerTurnoverUpdateJob implements Job {

    private static final short TRANSACTION_SIZE = 50;
    private static final Logger log = LoggerFactory.getLogger(PartnerTurnoverUpdateJob.class);
    @Inject
    private PartnerRepository partnerRepository;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {


        long startedAt = System.currentTimeMillis();
        log.info("Job started");

        String UPDATE_PARTNER_TURNOVER_SQL = "" +
                "WITH reference as (\n" +
                "    SELECT bpt.partner partner_id,\n" +
                "           sum(bpt.sale_turnover) turnover\n" +
                "    FROM base_partner_turnover as bpt\n" +
                "    WHERE bpt.year = date_part('year', now()) - 1\n" +
                "    group by bpt.partner\n" +
                ")\n" +
                "UPDATE base_partner bp\n" +
                "SET sale_turnover = turnover\n" +
                "FROM reference\n" +
                "WHERE bp.id = reference.partner_id";

        JPA.em().getTransaction().begin();


        Query nativeQuery = JPA.em().createNativeQuery(UPDATE_PARTNER_TURNOVER_SQL);
        nativeQuery.executeUpdate();

        JPA.em().getTransaction().commit();
        JPA.em().clear();
        long duration = (System.currentTimeMillis() - startedAt) / 1000;

        log.info("Job finished in {} minutes and {} seconds", duration / 60, duration % 60);
    }



}
