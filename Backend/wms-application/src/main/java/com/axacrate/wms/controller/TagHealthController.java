package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.ReplaceTagRequestDTO;
import com.axacrate.wms.dto.TagHealthResponseDTO;
import com.axacrate.wms.entity.AppUser;
import com.axacrate.wms.service.TagHealthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag-health")
@RequiredArgsConstructor
public class TagHealthController {

    private final TagHealthService tagHealthService;

    // Get health status of all tags
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagHealthResponseDTO>>> getAllTagHealth() {
        return ResponseEntity.ok(
                ApiResponse.success(tagHealthService.getAllTagHealthStatuses(), "Tag health statuses retrieved")
        );
    }

    // Staff replaces unhealthy tag — transfers inventory + resolves alert
    // @AuthenticationPrincipal gets the logged in staff member from JWT automatically
    @PostMapping("/replace")
    public ResponseEntity<ApiResponse<TagHealthResponseDTO>> replaceTag(
            @Valid @RequestBody ReplaceTagRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(
                ApiResponse.success(tagHealthService.replaceTag(request, currentUser), "Tag replaced successfully")
        );
    }
}
