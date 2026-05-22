package com.hospital.service;

import com.hospital.entity.Supplier;
import com.hospital.entity.SupplyCategory;

import java.util.List;

public interface MasterDataService {
    List<SupplyCategory> getAllCategories();

    List<Supplier> getAllSuppliers();
}
