package com.hoangtien2k3.tax.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.hoangtien2k3.tax.repository.TaxClassRepository;
import com.hoangtien2k3.tax.repository.TaxRateRepository;

@SpringBootTest(classes = TaxRateService.class)
public class TaxServiceTest {

    @MockBean
    private LocationService locationService;

    @MockBean
    private TaxRateRepository taxRateRepository;

    @MockBean
    private TaxClassRepository taxClassRepository;

    @Test
    void  testFindAll_shouldReturnAllTaxRate() {

    }
}
