package com.besafx.app.service;

import com.besafx.app.entity.SupplierReceipt;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public interface SupplierReceiptService extends PagingAndSortingRepository<SupplierReceipt, Long>, JpaSpecificationExecutor<SupplierReceipt> {
    List<SupplierReceipt> findBySupplierId(Long id);
}

