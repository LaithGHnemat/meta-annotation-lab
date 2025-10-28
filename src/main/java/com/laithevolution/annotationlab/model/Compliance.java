package com.laithevolution.annotationlab.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compliances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compliance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
    @OneToOne(mappedBy = "compliance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Invoice invoice;
}
