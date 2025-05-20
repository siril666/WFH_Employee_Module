package com.wfo_exception_tracker.wfh_exception.dtos

enum class RequestStatus {
    PENDING, APPROVED, REJECTED
}

enum class PriorityLevel {
    HIGH, MODERATE, LOW
}

enum class ApprovalLevel {
    TEAM_MANAGER, SDM, HR_MANAGER
}

enum class WorkflowStatus {
    APPROVED, REJECTED
}
