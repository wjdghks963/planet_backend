package com.jung.planet.r2;

import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.user.entity.User;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

@Getter
@Component
public class CloudflareR2Uploader {

    private static final Logger logger = LoggerFactory.getLogger(CloudflareR2Uploader.class);


    private final CloudFlareR2Utils cloudFlareR2Utils;
    private final CloudFlarePurgeCache cloudFlarePurgeCache;

    private final S3Client s3Client;

    private final String bucketName;
    private final String endPointUri;
    private final String storagePointUri;


    @Autowired
    public CloudflareR2Uploader(CloudFlarePurgeCache cloudFlarePurgeCache, @Value("${cloudflareR2.access_id}") String access_id, @Value("${cloudflareR2.secret_key}") String secret_key, @Value("${cloudflareR2.end_point}") String end_point, @Value("${cloudflareR2.storage_point}") String storage_point, @Value("${cloudflareR2.bucket_name}") String bucket_name) {
        this.cloudFlareR2Utils = new CloudFlareR2Utils();
        this.bucketName = bucket_name;
        this.endPointUri = end_point;
        this.storagePointUri = storage_point;
        this.cloudFlarePurgeCache = cloudFlarePurgeCache;


        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(access_id, secret_key);

        this.s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(end_point))
                .region(Region.of("us-east-1")) // Cloudflare R2는 리전이 필요하지 않지만, AWS SDK가 요구함
                .build();
    }

    public void uploadPlantImage(User user, Plant plant, ByteBuffer imageBuffer) {
        String imageHash = cloudFlareR2Utils.calculateImageHash(imageBuffer);

        String imageType = "thumbnail";
        String filePath = user.getEmail() + "/" + plant.getId()
                + "/" + imageType + "/image.jpg";

        String fileName = storagePointUri + filePath;
        plant.setImgUrl(fileName);
        logger.info("img URL :: {}", plant.getImgUrl());

        putObjectToR2(imageBuffer, imageHash, filePath);
    }

    public void editPlantImage(User user, Plant plant, ByteBuffer imageBuffer) {
        String imageType = "thumbnail";
        String filePath = user.getEmail() + "/" + plant.getId() + "/" + imageType + "/image.jpg";
        String fileName = storagePointUri + filePath;


        String imageHash = cloudFlareR2Utils.calculateImageHash(imageBuffer);

        String existingImageHash = retrieveImageHashFromR2(filePath);

        // 이미지 해시 비교
        if (!imageHash.equals(existingImageHash)) {
            logger.info("Image hash Changed now : {}, exist : {}", imageHash, existingImageHash);

            // 이미지 다르면 캐시 삭제
            uploadPlantImage(user, plant, imageBuffer);
            cloudFlarePurgeCache.purgeCache(fileName);

        }
    }


    public void deletePlant(Long plantId, String userEmail) {

        try {
            String filePath = userEmail + "/" + plantId;

            ListObjectsRequest listObjects = ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .prefix(filePath) // userEmail/PlantId/
                    .build();

            ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjects);
            for (S3Object object : listObjectsResponse.contents()) {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(object.key())
                        .build();
                s3Client.deleteObject(deleteRequest);
            }


        } catch (S3Exception e) {
            // 로그 기록, 예외 처리 로직
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }


    // Diary
    public void uploadDiaryImage(String userEmail, Diary diary, ByteBuffer imageBuffer) {
        String imageHash = cloudFlareR2Utils.calculateImageHash(imageBuffer);
        Plant plant = diary.getPlant();

        String imageType = "diary";

        String filePath = userEmail + "/" + plant.getId() + "/" + imageType + "/" + diary.getCreatedAt()
                + "/image.jpg";

        String fileName = storagePointUri + filePath;
        diary.setImgUrl(fileName);
        logger.info("img URL :: {}", diary.getImgUrl());

        putObjectToR2(imageBuffer, imageHash, filePath);
    }


    public void editDiaryImage(String userEmail, Diary diary, ByteBuffer imageBuffer) {
        String imageType = "diary";
        Plant plant = diary.getPlant();

        String filePath = userEmail + "/" + plant.getId() + "/" + imageType + "/" + diary.getCreatedAt()
                + "/image.jpg";

        String fileName = storagePointUri + filePath;


        String imageHash = cloudFlareR2Utils.calculateImageHash(imageBuffer);

        String existingImageHash = retrieveImageHashFromR2(filePath);

        // 이미지 해시 비교
        if (!imageHash.equals(existingImageHash)) {
            // 이미지 다르면 캐시 삭제
            uploadDiaryImage(userEmail, diary, imageBuffer);
            cloudFlarePurgeCache.purgeCache(fileName);

        }
    }

    public void deleteDiary(Diary diary, String userEmail) {
        Plant plant = diary.getPlant();
        String imageType = "diary";


        try {
            String filePath = userEmail + "/" + plant.getId() + "/" + imageType + "/" + diary.getCreatedAt()
                    + "/image.jpg";

            logger.info("deleted uri : {}", filePath);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName).key(filePath)
                    .build();
            s3Client.deleteObject(deleteRequest);


        } catch (S3Exception e) {
            // 로그 기록, 예외 처리 로직
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    // User 삭제
    public void deleteUser(String userEmail) {

        try {
            String filePath = userEmail;

            ListObjectsRequest listObjects = ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .prefix(filePath) // userEmail/
                    .build();

            ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjects);
            for (S3Object object : listObjectsResponse.contents()) {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(object.key())
                        .build();
                s3Client.deleteObject(deleteRequest);
            }


        } catch (S3Exception e) {
            // 로그 기록, 예외 처리 로직
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }


    private void putObjectToR2(ByteBuffer imageBuffer, String imageHash, String filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .contentType("image/jpeg") // MIME 타입 설정
                .metadata(Map.of("Content-Disposition", "inline", "image-hash", imageHash))
                .key(filePath).acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromByteBuffer(imageBuffer));
    }


    public String retrieveImageHashFromR2(String filePath) {
        try {
            HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build());

            return response.metadata().get("image-hash");
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to retrieve image hash from R2", e);
        }
    }
}

