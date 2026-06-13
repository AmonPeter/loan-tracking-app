package com.loan.app.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class LoanControllerTests {

    @Test
    void reportsRejectsEndDateBeforeStartDateWithoutCallingService() {
        RecordingLoanService loanService = new RecordingLoanService();
        LoanController controller = new LoanController(loanService, new StubInterestRateService(), new StubLoanTypeService());
        Model model = new ExtendedModelMap();

        String view = controller.reports(0, 12, 2026, 1, 2026, null, null, model);

        assertEquals("reports", view);
        assertEquals("To date cannot be before From date.", model.asMap().get("errorMessage"));
        assertEquals(List.of(), model.asMap().get("rows"));
        assertEquals(0, model.asMap().get("totalRows"));
        assertFalse(loanService.called);
    }

    @Test
    void quarterlyFundPerformanceRejectsEndYearBeforeStartYearWithoutCallingService() {
        RecordingLoanService loanService = new RecordingLoanService();
        LoanController controller = new LoanController(loanService, new StubInterestRateService(), new StubLoanTypeService());
        Model model = new ExtendedModelMap();

        String view = controller.quarterlyFundPerformanceReport(2026, 2025, null, null, model);

        assertEquals("quarterly-fund-performance", view);
        assertEquals("End year must be the same as or later than the start year.", model.asMap().get("errorMessage"));
        assertEquals(List.of(), model.asMap().get("quarterlyRows"));
        assertFalse(loanService.called);
    }

    @Test
    void createLoanUsesCurrentConfiguredInterestRate() {
        RecordingLoanService loanService = new RecordingLoanService();
        LoanController controller = new LoanController(loanService, new StubInterestRateService(), new StubLoanTypeService());
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String redirect = controller.createLoan(
            "Project",
            "First",
            "Last",
            "ID123",
            "0810000000",
            "Khomas",
            "Windhoek",
            "Member",
            "Female",
            1L,
            12,
            redirectAttributes
        );

        assertEquals("redirect:/loans", redirect);
        assertEquals(BigDecimal.TEN, loanService.createdForm.interestRate());
        assertEquals(1L, loanService.createdForm.loanTypeId());
    }

    private static class RecordingLoanService extends LoanService {
        private boolean called;
        private LoanForm createdForm;

        private RecordingLoanService() {
            super(null);
        }

        @Override
        public List<LoanReportRow> reportRows(LocalDate fromDate, LocalDate toDate, Long loanTypeId, String region) {
            called = true;
            return List.of();
        }

        @Override
        public List<QuarterlyFundPerformanceRow> quarterlyFundPerformanceRows(
            int fromYear,
            int toYear,
            Long loanTypeId,
            String region
        ) {
            called = true;
            return List.of();
        }

        @Override
        public void create(LoanForm form) {
            called = true;
            createdForm = form;
        }
    }

    private static class StubInterestRateService extends InterestRateService {
        private StubInterestRateService() {
            super(null);
        }

        @Override
        public BigDecimal requireRateForLoanType(long loanTypeId) {
            return BigDecimal.TEN;
        }
    }

    private static class StubLoanTypeService extends LoanTypeService {
        private StubLoanTypeService() {
            super(null);
        }

        @Override
        public List<LoanTypeView> findActive() {
            return List.of(new LoanTypeView(1L, "Micro loan", BigDecimal.TEN, true, null, null));
        }
    }
}
