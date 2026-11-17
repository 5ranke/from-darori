package com.daroring.fromdarori.domain.category.repository;

import com.daroring.fromdarori.domain.category.entity.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
