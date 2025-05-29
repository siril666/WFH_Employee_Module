package com.wfo_exception_tracker.wfh_exception.controller

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dtos.PriorityLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.entity.ApprovalWorkflow
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeInfoRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeMasterRepository
import com.wfo_exception_tracker.wfh_exception.service.WfhRequestService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDate

@RestController
@RequestMapping("/wfh-requests")
@CrossOrigin(origins = ["*"]) // Adjust as needed for your frontend
class WfhRequestController(
    private val wfhRequestService: WfhRequestService,
    private val employeeInfoRepository: EmployeeInfoRepository,
    private val employeeMasterRepository: EmployeeMasterRepository,
    private val approvalWorkflowRepository: ApprovalWorkflowRepository
) {

    //1. API to submit a new WFH request
    @PostMapping("/submit")
    fun submitRequest(
        @RequestParam ibsEmpId: Long,
        @RequestParam requestedStartDate: LocalDate,
        @RequestParam requestedEndDate: LocalDate,
        @RequestParam employeeReason: String,
        @RequestParam categoryOfReason: String,
        @RequestParam teamOwnerId: Long?,
        @RequestParam dmId: Long?,
        @RequestParam termDuration: String,
        @RequestParam priority: PriorityLevel,
        @RequestParam location: String,
        @RequestParam("attachment") attachment: MultipartFile?
    ): ResponseEntity<String> {
        var fileUrl: String? = null

        // 1. Save the attachment if it's not null or empty
        if (attachment != null && !attachment.isEmpty) {
            val uploadDir = Paths.get("uploads")  // Directory in project root

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir)
            }

            val filename = "${System.currentTimeMillis()}_${attachment.originalFilename}"
            val file = uploadDir.resolve(filename)
            Files.copy(attachment.inputStream, file, StandardCopyOption.REPLACE_EXISTING)

            fileUrl = "/uploads/$filename"  // This URL will be accessible via http://localhost:8080
        }

        // 2. Create the request with the web-accessible URL
        val request = WfhRequest(
            ibsEmpId = ibsEmpId,
            requestedStartDate = requestedStartDate,
            requestedEndDate = requestedEndDate,
            employeeReason = employeeReason,
            categoryOfReason = categoryOfReason,
            status = RequestStatus.PENDING,
            teamOwnerId = teamOwnerId,
            dmId = dmId,
            termDuration = termDuration,
            priority = priority,
            currentLocation = location,
            attachmentPath = fileUrl  // Now stores a web URL instead of filesystem path
        )

        val saved = wfhRequestService.createWfhRequest(request)
        return ResponseEntity.ok("Request Submitted! ID: ${saved.requestId}")
    }

    //2. API to update an existing WFH request
    @GetMapping("/{requestId}")
    fun getRequestById(@PathVariable requestId: Long): ResponseEntity<WfhRequest?> {
        val request = wfhRequestService.getRequestById(requestId)
        return if (request != null) ResponseEntity.ok(request) else ResponseEntity.notFound().build()
    }


    //3. API to fetch and show all details of the employee and request from EmployeeInfo and EmployeeMaster
    @GetMapping("/employee-details")
    fun getEmployeeDetails(): ResponseEntity<Map<String, Any?>> {

        val ibsEmpId = AuthUtils.getCurrentUserId()
        val info = employeeInfoRepository.findByIbsEmpId(ibsEmpId)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Employee not found"))
        val master = employeeMasterRepository.findByIbsEmpId(ibsEmpId)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Team details not found"))

        return ResponseEntity.ok(
            mapOf(
                "employeeName" to info.userName,
                "ibsEmpId" to info.ibsEmpId,
                "teamOwnerName" to master.teamOwner,
                "teamOwnerId" to master.teamOwnerId,
                "dmId" to master.dmId,
                "dmName" to master.dm,
                "currentLocation" to master.location
            )
        )
    }

    //4.
    @PostMapping("/update")
    fun updateRequest(
        @RequestParam requestId: Long,
        @RequestParam ibsEmpId: Long,
        @RequestParam requestedStartDate: LocalDate,
        @RequestParam requestedEndDate: LocalDate,
        @RequestParam teamOwnerId: Long,
        @RequestParam employeeReason: String,
        @RequestParam categoryOfReason: String,
        @RequestParam dmId: Long,
        @RequestParam termDuration: String,
        @RequestParam priority: PriorityLevel,
        @RequestParam location: String,
        @RequestParam status: RequestStatus,
        @RequestParam("attachment", required = false) attachment: MultipartFile?
    ): ResponseEntity<String> {

        // Fetch existing request by requestId
        val existingRequest = wfhRequestService.getRequestById(requestId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Request ID $requestId not found.")

        // Update fields (you can also add attachment file saving logic if needed)
        val updatedRequest = existingRequest.copy(
            ibsEmpId = ibsEmpId,
            requestedStartDate = requestedStartDate,
            requestedEndDate = requestedEndDate,
            employeeReason = employeeReason,
            categoryOfReason = categoryOfReason,
            dmId = dmId,
            teamOwnerId = teamOwnerId,
            termDuration = termDuration,
            priority = priority,
            currentLocation = location,
            status = status,
            attachmentPath = existingRequest.attachmentPath // update if file saved
        )

        // Save updated entity
        val savedRequest = wfhRequestService.updateWfhRequest(updatedRequest)

        return ResponseEntity.ok("Request updated successfully! ID: ${savedRequest.requestId}")
    }

//    @DeleteMapping("/{requestId}")
//    fun cancelRequest(@PathVariable requestId: Long){
//        wfhRequestService.cancelWfhRequest(requestId)
//
//    }


    @DeleteMapping("/{requestId}")
    fun cancelRequest(@PathVariable requestId: Long): ResponseEntity<String> {
        // Check if the request has any approvals
        val approvals = approvalWorkflowRepository.findByRequestId(requestId)

        // If any approval exists with status APPROVED, return error
        if (approvals.any { it.status == WorkflowStatus.APPROVED }) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Cannot cancel request that has been approved")
        }

        try {
            wfhRequestService.cancelWfhRequest(requestId)
            return ResponseEntity.ok("Request cancelled successfully")
        } catch (e: Exception) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to cancel request: ${e.message}")
        }
    }


}




