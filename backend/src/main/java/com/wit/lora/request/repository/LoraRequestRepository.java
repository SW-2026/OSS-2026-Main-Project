package com.wit.lora.request.repository;

import com.wit.lora.request.domain.LoraRequest;
import com.wit.lora.request.domain.LoraRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoraRequestRepository extends JpaRepository<LoraRequest, Long> {

    List<LoraRequest> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);

    List<LoraRequest> findByStatusOrderByCreatedAtDesc(LoraRequestStatus status);
}
