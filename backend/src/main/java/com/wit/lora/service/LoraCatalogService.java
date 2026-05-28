package com.wit.lora.service;

import com.wit.lora.dto.LoraCatalogResponse;
import com.wit.lora.repository.LoraCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoraCatalogService {

    private final LoraCatalogRepository loraCatalogRepository;

    public List<LoraCatalogResponse> findAll() {
        return loraCatalogRepository.findAll().stream()
                .map(LoraCatalogResponse::from)
                .toList();
    }
}
