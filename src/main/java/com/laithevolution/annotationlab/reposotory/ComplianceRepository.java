package com.laithevolution.annotationlab.reposotory;

import com.laithevolution.annotationlab.model.Compliance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceRepository extends JpaRepository<Compliance, Long> {
}
