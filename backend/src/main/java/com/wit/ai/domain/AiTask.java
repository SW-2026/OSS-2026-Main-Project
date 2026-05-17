package com.wit.ai.domain;

import com.wit.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AiTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 20)
    private TaskType taskType;

    @Column(name = "target_type", length = 40)
    private String targetType;                // 작업 완료 시 채워짐

    @Column(name = "target_id")
    private Long targetId;                    // 작업 완료 시 채워짐

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent;

    @Column(name = "result_url", length = 500)
    private String resultUrl;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public AiTask(Member member, TaskType taskType, TaskStatus status,
                  Integer progressPercent, String targetType, Long targetId) {
        this.member = member;
        this.taskType = taskType;
        this.status = status;
        this.progressPercent = progressPercent;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    // ===== 상태 갱신 (외부 setter 차단, 메서드로만 전이) =====

    public void markProcessing() {
        this.status = TaskStatus.PROCESSING;
        this.progressPercent = 50;
    }

    public void markCompleted(String targetType, Long targetId, String resultUrl) {
        this.status = TaskStatus.COMPLETED;
        this.progressPercent = 100;
        this.targetType = targetType;
        this.targetId = targetId;
        this.resultUrl = resultUrl;
    }

    public void markFailed(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        // progressPercent 현재값 유지 — 실패 시점의 진행률 보존
    }

    public void updateProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }
}
