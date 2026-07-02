package com.novaerp.gl.config;

import com.novaerp.common.sak.AccountingPeriod;
import com.novaerp.gl.repository.AccountingPeriodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/** Seeds SAK standard accounts and default accounting periods on startup. */
@Configuration
public class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);
    private static final String DEFAULT_TENANT = "default";

    @Bean
    CommandLineRunner sakSeedRunner(com.novaerp.gl.service.ChartOfAccountService coaService,
                                   AccountingPeriodRepository periodRepo) {
        return args -> {
            // Seed SAK accounts (idempotent — skips if table not empty)
            int seededAccounts = coaService.seedSakAccounts();
            log.info("SAK seed: {} accounts", seededAccounts);

            // Seed default accounting periods for the current year
            int seededPeriods = 0;
            java.time.YearMonth now = java.time.YearMonth.now();
            
            // Seed last month + next 12 months
            for (int i = -1; i <= 12; i++) {
                java.time.YearMonth ym = now.plusMonths(i);
                String periodKey = ym.toString();
                
                if (periodRepo.findByTenantIdAndPeriod(DEFAULT_TENANT, periodKey).isEmpty()) {
                    LocalDate startDate = ym.atDay(1);
                    LocalDate endDate = ym.atEndOfMonth();
                    
                    AccountingPeriod p = AccountingPeriod.of(periodKey, startDate, endDate);
                    String monthName = switch (ym.getMonth().getValue()) {
                        case 1 -> "Januari"; case 2 -> "Februari"; case 3 -> "Maret";
                        case 4 -> "April"; case 5 -> "Mei"; case 6 -> "Juni";
                        case 7 -> "Juli"; case 8 -> "Agustus"; case 9 -> "September";
                        case 10 -> "Oktober"; case 11 -> "November"; default -> "Desember";
                    };
                    p.setDisplayNameId(monthName + " " + ym.getYear());
                    p.setTenantId(DEFAULT_TENANT);
                    periodRepo.save(p);
                    seededPeriods++;
                }
            }
            
            log.info("SAK seed: {} default periods created", seededPeriods);
        };
    }
}
