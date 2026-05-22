package com.hospital.repository;

import com.hospital.entity.InboundReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;

public interface InboundReceiptRepository extends JpaRepository<InboundReceipt, Long> {

    @Query("""
            select month(r.receiptDate), coalesce(sum(r.totalAmount), 0)
            from InboundReceipt r
            where year(r.receiptDate) = :year
            group by month(r.receiptDate)
            order by month(r.receiptDate)
            """)
    List<Object[]> getMonthlyInboundStatistics(@Param("year") int year);

    @Query("""
            select count(r)
            from InboundReceipt r
            where year(r.receiptDate) = :year and month(r.receiptDate) = :month
            """)
    long countByMonth(@Param("year") int year, @Param("month") int month);

    long countByReceiptDate(LocalDate receiptDate);
}
