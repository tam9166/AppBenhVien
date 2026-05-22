package com.hospital.service;

import com.hospital.dto.OutboundReceiptForm;
import com.hospital.entity.OutboundReceipt;

import java.time.LocalDate;
import java.util.List;

public interface OutboundReceiptService {
    OutboundReceipt createReceipt(OutboundReceiptForm form);

    List<OutboundReceipt> getAllReceipts();

    long countReceiptsByMonth(int year, int month);

    long countReceiptsByDate(LocalDate receiptDate);
}
