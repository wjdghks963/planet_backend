package com.jung.planet.plant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jung.planet.plant.dto.PlantDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
@Transactional
public class PlantServiceTest {

    @MockBean
    private PlantRepository plantRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private PlantService plantService;

    private PlantDTO plantDTO;
    private User user;
    private Plant plant;

    @BeforeEach
    void setUp() {
        // User
        user = User.builder().email("example@example.com").name("USUS").build();

        // PlantDTO
        plantDTO = new PlantDTO();
        plantDTO.setUserId(user.getId());
        plantDTO.setNickName("Green Plant");
        plantDTO.setScientificName("Plantae Greenus");
        plantDTO.setImgUrl("");

        // Plant
        plant = Plant.builder().nickName(plantDTO.getNickName()).scientificName(plantDTO.getScientificName()).imgUrl(plantDTO.getImgUrl()).user(user).build();

    }

    @Test
    void whenAddPlant_withValidUserId_thenPlantShouldBeSaved() {
        // UserRepository가 특정 ID로 User를 찾을 때, 미리 정의된 User 객체를 반환
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // PlantRepository의 save 메소드가 호출될 때, 미리 정의된 Plant 객체를 반환
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        // 실제 서비스 메소드를 호출합니다.
        Plant savedPlant = plantService.addPlant(plantDTO);

        // 검증: 생성된 Plant 객체가 null이 아닌지 확인
        assertNotNull(savedPlant);

        // 검증: 반환된 Plant 객체가 예상된 Plant 객체와 동일한지 확인
        assertEquals(plant, savedPlant);

        // 검증: PlantRepository의 save 메소드가 실제로 호출되었는지 확인
        verify(plantRepository).save(any(Plant.class));
    }

    @Test
    void whenAddPlant_withInvalidUserId_thenThrowException() {
        // UserRepository가 특정 ID로 User를 찾을 때, 빈 Optional을 반환하도록 설정
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // 예외가 발생하는지 확인합니다.
        assertThrows(RuntimeException.class, () -> {
            plantService.addPlant(plantDTO);
        });
    }
}
