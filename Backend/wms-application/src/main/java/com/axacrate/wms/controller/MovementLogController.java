package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.MovementLogResponseDTO;
import com.axacrate.wms.service.MovementLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementLogController {

    private final MovementLogService movementLogService;

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<MovementLogResponseDTO>>> getRecentMovements(
            @RequestParam(defaultValue = "20") int limit
    ) {
        if (limit < 1) {
            limit = 20;
        }

        List<MovementLogResponseDTO> movements = movementLogService.getRecentMovements(limit);

        return ResponseEntity.ok(
                ApiResponse.success(movements, "Recent movements retrieved successfully")
        );
    }
}
