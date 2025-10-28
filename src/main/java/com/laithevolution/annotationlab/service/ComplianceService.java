package com.laithevolution.annotationlab.service;

import com.laithevolution.annotationlab.model.Compliance;
import com.laithevolution.annotationlab.reposotory.ComplianceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceRepository complianceRepository;

    @Transactional
    public Compliance createCompliance(Compliance compliance) {
        return complianceRepository.save(compliance);
    }

    @Transactional
    public void deleteCompliance(Long id) {
        complianceRepository.deleteById(id);
    }

    public Optional<Compliance> getCompliance(Long id) {
        return complianceRepository.findById(id);
    }
}
