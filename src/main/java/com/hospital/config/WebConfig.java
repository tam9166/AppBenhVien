package com.hospital.config;

import com.hospital.entity.Supplier;
import com.hospital.entity.SupplyCategory;
import com.hospital.repository.SupplierRepository;
import com.hospital.repository.SupplyCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình converter để binding dropdown -> entity trong form Thymeleaf.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SupplyCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToSupplyCategoryConverter(categoryRepository));
        registry.addConverter(new StringToSupplierConverter(supplierRepository));
    }

    /**
     * Converter doi id tu form sang entity loai vat tu.
     */
    private static class StringToSupplyCategoryConverter implements Converter<String, SupplyCategory> {

        private final SupplyCategoryRepository categoryRepository;

        private StringToSupplyCategoryConverter(SupplyCategoryRepository categoryRepository) {
            this.categoryRepository = categoryRepository;
        }

        @Override
        public SupplyCategory convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            return categoryRepository.findById(Long.valueOf(source)).orElse(null);
        }
    }

    /**
     * Converter doi id tu form sang entity nha cung cap.
     */
    private static class StringToSupplierConverter implements Converter<String, Supplier> {

        private final SupplierRepository supplierRepository;

        private StringToSupplierConverter(SupplierRepository supplierRepository) {
            this.supplierRepository = supplierRepository;
        }

        @Override
        public Supplier convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            return supplierRepository.findById(Long.valueOf(source)).orElse(null);
        }
    }
}
