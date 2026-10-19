package com.kavindu.stagepass.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "payments")
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Orders order;

    @Column(name = "provider",nullable = false,length = 20)
    private String provider;

    @Column(name = "provider_payment_id",length = 100)
    private String providerPaymentId;


}
