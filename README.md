# planet_backend


## 소개

이 프로젝트는 스프링 부트를 기반으로 한 웹 애플리케이션으로, 사용자 관리, 구독 서비스, 식물 및 일기 관리 기능을 제공합니다. 

[프론트 및 기능에 대한 UI는 플러터 프로젝트로](https://github.com/wjdghks963/planet)

## 기능

**사용자 관리** 

- 사용자 생성, 삭제 및 업그레이드 기능을 포함합니다.

**프리미엄 구독**

- 사용자가 프리미엄 서비스를(이미지 분석) 이용할 수 있게 합니다.
- 사용자의 최대 식물 관리 수 3->6 상향합니다.
  
**어드민 기능**

- 관리자가 사용자를 관리할 수 있는 기능을 제공합니다.
- 식물 및 일기 신고를 받고 삭제 기능을 제공합니다.

  
**식물 및 일기 관리** 

- 사용자가 식물과 일기를 추가, 수정, 삭제할 수 있습니다.
- 다른 유저들의 식물과 일기를 조회할 수 있습니다.
- 다른 유저의 식물에 관심을 표하고 나중에 조회할 수 있습니다.
- 최신 및 인기 식물에 대하여 페이지네이션을 제공합니다.
  
**슬랙 알림** 

- 백엔드에서 중요한 이벤트에 대해 슬랙을 통해 알림을 받습니다.


# 아키텍처


![스크린샷 2023-11-27 오후 10 51 58](https://github.com/wjdghks963/planet_backend/assets/74060017/86bfbbf9-99c6-4fc0-adf0-f8bb7121e6ae)



CloudFlare : DNS, CDN 

장점 : 무료(DNS, CDN, DDOS Shield) 트래픽 대쉬보드


CloudFlare R2 : Object Storage

장점 : Get의 아웃 바운드 비용 x , 저장 공간 비용 저렴

단점 : 수정에 대해선 비용이 약간 더 비쌈, CDN 자동 적용이라 캐시 관리 신경 써야함, 자동으로 meta-data의 key가 소문자로 변경됨(소문자 강제)


   
AWS EC2, RDS : 클라우드 컴퓨팅

장점 : 프리 티어로 공짜로 사용, 안전 만약 프리티어 끝난다해도 다른 메이저급 서비스들과 가격 차이가 별로 없음, 인텔리제이에서 가동되고 있는 ec2를 점프박스로 사용해 RDS 직접 접근 가능함

단점 : 트래픽이 늘어난다면 컴퓨팅과 달리 아웃바운드 비용이 생길 수 있지만 테라급 아니기에 상관없다 판단

 
SLACK : web hook api 사용해 controlleradvice 에 잡히는 에러와 신고 기능 등 모니터링(신고, 에러 확인)이 필요한 경우 사용함



# 기술 스택


### Spring Boot: 백엔드 프레임워크로 사용.

MVC 패턴으로 VIEW 대신 json 반환, 최대한 restful 하도록 uri 설계

exception 처리 controller advice 사용해 처리

### Spring Security & JWT: 사용자 인증 및 보안을 위해 사용. RESTful API 보안 강화.

JWT subject -> user email, id, role을 담아 controller, service에서 처리

Spring Config에서 인증이 필요한 uri 설정

### JPA/Hibernate: 데이터베이스 연동 및 객체 관계 매핑을 위해 사용.

entity 생성, repository 사용해 ORM으로 쿼리 추상화

### AWS EC2, RDS: 애플리케이션 호스팅과 데이터베이스 관리.


### CloudFlare, CloudFlare R2: 이미지 저장 및 캐싱 관리.

메타 태그 사용해 해시 값이 같다면 이미지 수정시 같은 사진 올리면 저장하지 않도록함 다른 사진이라면 해당 uri 캐싱 삭제하고 바로 바뀐 사진 보일 수 있도록함

sub-domain 사용해 자원 위치 분리

### Slack API: 에러 로깅 및 알림을 위해 사용.

controller advice에서 잡힌 EntityNotFound 같은 예외 외에 다른 예기치 못한 에러 발생시 슬랙으로 알람이 오도록함




# DB

![스크린샷 2023-11-29 오후 1 07 30](https://github.com/wjdghks963/planet_backend/assets/74060017/44795329-440b-4530-a110-b0f2d9608f97)


[io](https://dbdocs.io/chsw000/planet)




# 슬랙

<img width="774" alt="스크린샷 2023-11-29 오후 12 51 13" src="https://github.com/wjdghks963/planet_backend/assets/74060017/8f3bb13b-23de-442a-979f-c5db90ed91eb">
<img width="724" alt="스크린샷 2023-11-29 오후 12 50 59" src="https://github.com/wjdghks963/planet_backend/assets/74060017/2b31326f-768c-401b-82a4-9b7d85805c95">

