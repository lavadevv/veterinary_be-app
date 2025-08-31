package ext.vnua.veterinary_beapp.modules.audits.controller;

import ext.vnua.veterinary_beapp.modules.audits.dto.request.AuditLogSearchRequest;
import ext.vnua.veterinary_beapp.modules.audits.service.AuditService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {
    // TODO: Implement audit log controller
    private final AuditService auditService;

    @GetMapping("")
    @ApiOperation(value = "Lấy danh sách audit log")
    public ResponseEntity<?> getAllAuditLogs(@Valid @ModelAttribute AuditLogSearchRequest request) {
        return ResponseEntity.ok(auditService.getAllAuditLogs(request, PageRequest.of(request.getStart(), request.getLimit())));
    }
}
