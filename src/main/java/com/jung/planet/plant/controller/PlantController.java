package com.jung.planet.plant.controller;

import com.jung.planet.exception.UnauthorizedActionException;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.request.PlantFormDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Plant", description = "식물 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/plants")
public class PlantController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    @Operation(summary = "식물 목록 조회", description = "모든 식물의 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PlantSummaryDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<?> getPlants(@RequestParam(defaultValue = "recent") String type,
                                       @RequestParam(defaultValue = "0") int page,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<PlantSummaryDTO> plants = switch (type) {
            case "popular" -> plantService.getPlantsByPopularity(page);
            case "heart" -> plantService.getHeartedPlantsByUser(userDetails.getUserId(), page);
            default -> plantService.getPlantsByRecent(page);
        };

        return ResponseEntity.ok(plants);
    }

    @Operation(summary = "식물 상세 조회", description = "ID로 특정 식물의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = PlantDetailDTO.class))),
        @ApiResponse(responseCode = "404", description = "식물을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlantDetailDTO> getPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long plantId) {
        Long userId = customUserDetails != null ? customUserDetails.getUserId() : -1;
        PlantDetailDTO plant = plantService.getPlantDetailsByPlantId(userId, plantId);
        return ResponseEntity.ok(plant);
    }

    @Operation(summary = "식물 추가", description = "새로운 식물을 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 추가 성공",
            content = @Content(schema = @Schema(implementation = Plant.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/add")
    public ResponseEntity<?> addPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody PlantFormDTO plantFormDTO) {
        logger.info("Request to add plant: {}", plantFormDTO);

        plantFormDTO.setUserId(customUserDetails.getUserId());
        Plant newPlant = plantService.addPlant(plantFormDTO);
        logger.info("Plant added: {}", newPlant);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody PlantFormDTO plantFormDTO, @PathVariable("id") Long plantId) {
        Long userId = customUserDetails.getUserId();
        logger.debug("userId :: {}", userId);
        // 식물 소유권 확인
        if (!plantService.isOwnerOfPlant(userId, plantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("식물의 주인이 아닙니다.");
        }


        try {
            plantFormDTO.setUserId(userId);
            Plant newPlant = plantService.editPlant(plantFormDTO, plantId);
            logger.info("Plant edited: {}", newPlant);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            logger.error("Error editing plant: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while editing the plant.");
        }
    }

    @Operation(summary = "식물 삭제", description = "ID로 특정 식물을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "식물에 대한 권한이 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> deletePlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long plantId) {
        Long userId = customUserDetails.getUserId();
        String userEmail = customUserDetails.getUsername();

        if (!plantService.isOwnerOfPlant(userId, plantId) && !customUserDetails.getUserRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedActionException("식물에 대한 권한이 없습니다.");
        }

        plantService.removePlant(plantId, userEmail);
        logger.info("Plant deleted");
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PlantSummaryDTO>> getPlants(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        List<PlantSummaryDTO> plants = plantService.getPlantsByUserId(userId);

        return ResponseEntity.ok(plants);
    }

    @GetMapping("/random")
    public ResponseEntity<List<PlantSummaryDTO>> getRandomPlants() {
        List<PlantSummaryDTO> randomPlants = plantService.getRandomPlants();
        return ResponseEntity.ok(randomPlants);
    }
}
