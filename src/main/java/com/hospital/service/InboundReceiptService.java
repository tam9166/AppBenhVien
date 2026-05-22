package com.hospital.service;

import com.hospital.dto.InboundReceiptForm;
import com.hospital.entity.InboundReceipt;

import java.time.LocalDate;
import java.util.List;

public interface InboundReceiptService {
    InboundReceipt createReceipt(InboundReceiptForm form);

    List<InboundReceipt> getAllReceipts();

    long countReceiptsByMonth(int year, int month);

    long countReceiptsByDate(LocalDate receiptDate);
}
