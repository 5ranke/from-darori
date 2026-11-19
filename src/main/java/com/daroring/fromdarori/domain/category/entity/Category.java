package com.daroring.fromdarori.domain.category.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "display_order", nullable = false, unique = true)
    private Integer displayOrder;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}
