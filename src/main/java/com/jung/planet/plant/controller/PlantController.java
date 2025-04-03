package com.jung.planet.plant.controller;

import com.jung.planet.common.dto.ApiResponseDTO;
import com.jung.planet.exception.ErrorMessages;
import com.jung.planet.exception.PermissionDeniedException;
import com.jung.planet.exception.UnauthorizedActionException;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.dto.request.PlantFormDTO;
import com.jung.planet.plant.dto.response.PlantResponseDTO;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Plant", description = "식물 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/plants")
@SecurityRequirement(name = "bearerAuth")
public class PlantController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    @Operation(summary = "식물 목록 조회", description = "모든 식물의 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping
    public ApiResponseDTO<PlantResponseDTO> getPlants(
            @Parameter(description = "정렬 방식 (recent: 최신순, popular: 인기순, heart: 좋아요한 식물)", schema = @Schema(type = "string", defaultValue = "recent", allowableValues = {"recent", "popular", "heart"}))
            @RequestParam(defaultValue = "recent") String type,
            @Parameter(description = "페이지 번호 (0부터 시작)", schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<PlantSummaryDTO> plants = switch (type) {
            case "popular" -> plantService.getPlantsByPopularity(page);
            case "heart" -> plantService.getHeartedPlantsByUser(userDetails.getUserId(), page);
            default -> plantService.getPlantsByRecent(page);
        };

        return ApiResponseDTO.success(PlantResponseDTO.forPlantList(plants));
    }

    @Operation(summary = "식물 상세 조회", description = "ID로 특정 식물의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "식물을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/{id}")
    public ApiResponseDTO<PlantDetailDTO> getPlant(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "조회할 식물 ID", required = true, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable("id") Long plantId) {
        Long userId = customUserDetails != null ? customUserDetails.getUserId() : -1;
        PlantDetailDTO plant = plantService.getPlantDetailsByPlantId(userId, plantId);
        return ApiResponseDTO.success(plant);
    }

    @Operation(summary = "식물 추가", description = "새로운 식물을 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 추가 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PostMapping("/add")
    public ApiResponseDTO<PlantResponseDTO> addPlant(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "추가할 식물 정보", required = true)
            @RequestBody PlantFormDTO plantFormDTO) {
        logger.info("Request to add plant: {}", plantFormDTO);

        plantFormDTO.setUserId(customUserDetails.getUserId());
        Plant newPlant = plantService.addPlant(plantFormDTO);
        logger.info("Plant added: {}", newPlant);

        return ApiResponseDTO.success(PlantResponseDTO.forSinglePlant(newPlant.getId()), "식물이 성공적으로 추가되었습니다.");
    }

    @Operation(summary = "식물 수정", description = "기존 식물 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 수정 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "식물에 대한 권한이 없음",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "식물을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PostMapping("/edit/{id}")
    public ApiResponseDTO<PlantResponseDTO> editPlant(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "수정할 식물 정보", required = true)
            @RequestBody PlantFormDTO plantFormDTO,
            @Parameter(description = "수정할 식물 ID", required = true, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable("id") Long plantId) {
        Long userId = customUserDetails.getUserId();
        logger.debug("userId :: {}", userId);
        
        // 식물 소유권 확인
        if (!plantService.isOwnerOfPlant(userId, plantId)) {
            throw new PermissionDeniedException("Plant", plantId);
        }

        try {
            plantFormDTO.setUserId(userId);
            Plant newPlant = plantService.editPlant(plantFormDTO, plantId);
            logger.info("Plant edited: {}", newPlant);
            return ApiResponseDTO.success(PlantResponseDTO.forSinglePlant(newPlant.getId()), "식물이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            logger.error("Error editing plant: {}", e.getMessage());
            return ApiResponseDTO.<PlantResponseDTO>error(ErrorMessages.SERVER_ERROR, PlantResponseDTO.builder().success(false).build());
        }
    }

    @Operation(summary = "식물 삭제", description = "ID로 특정 식물을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 삭제 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "식물에 대한 권한이 없음",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "식물을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/remove/{id}")
    public ApiResponseDTO<PlantResponseDTO> deletePlant(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "삭제할 식물 ID", required = true, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable("id") Long plantId) {
        Long userId = customUserDetails.getUserId();
        String userEmail = customUserDetails.getUsername();

        if (!plantService.isOwnerOfPlant(userId, plantId) && !customUserDetails.getUserRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedActionException(ErrorMessages.PLANT_PERMISSION_DENIED);
        }

        plantService.removePlant(plantId, userEmail);
        logger.info("Plant deleted");
        return ApiResponseDTO.success(PlantResponseDTO.forSinglePlant(plantId), "식물이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "내 식물 목록 조회", description = "현재 로그인한 사용자의 식물 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/my")
    public ApiResponseDTO<PlantResponseDTO> getMyPlants(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        List<PlantSummaryDTO> plants = plantService.getPlantsByUserId(userId);

        return ApiResponseDTO.success(PlantResponseDTO.forPlantList(plants));
    }

    @Operation(summary = "랜덤 식물 목록 조회", description = "랜덤으로 선택된 식물 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "식물 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/random")
    public ApiResponseDTO<PlantResponseDTO> getRandomPlants() {
        List<PlantSummaryDTO> randomPlants = plantService.getRandomPlants();
        return ApiResponseDTO.success(PlantResponseDTO.forPlantList(randomPlants));
    }
}
