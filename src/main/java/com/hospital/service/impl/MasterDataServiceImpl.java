package com.hospital.service.impl;

import com.hospital.entity.Supplier;
import com.hospital.entity.SupplyCategory;
import com.hospital.repository.SupplierRepository;
import com.hospital.repository.SupplyCategoryRepository;
import com.hospital.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterDataServiceImpl implements MasterDataService {

    private final SupplyCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public List<SupplyCategory> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAllByOrderByNameAsc();
    }
}
