package com.laithevolution.annotationlab.model;

import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    private String status;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compliance_id")
    private Compliance compliance;
}
