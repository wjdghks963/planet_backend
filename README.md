# planet_backend


# 아키텍처


![스크린샷 2023-11-27 오후 10 51 58](https://github.com/wjdghks963/planet_backend/assets/74060017/86bfbbf9-99c6-4fc0-adf0-f8bb7121e6ae)



CloudFlare : DNS, CDN 사용 무료 모드에서도 CDN 자동 적용 및 DDOS 공격 방어 기능 존재함

CloudFlare R2 : Get의 아웃 바운드 비용 x , 저장 공간 비용 저렴   대신 수정에 대해선 비용이 약간 더 비쌈

AWS EC2, RDS : 컴퓨터와 db는 궁합이 잘 맞아야한다고 생각했고 프리티어로도 충분히 수용 가능하다 판단 인텔리제이에서 가동되고 있는 ec2를 점프박스로 사용해 직접 접근 가능함

SLACK : web hook api 사용해 controlleradvice 에 잡히는 에러와 신고 기능 등 모니터링이 필요한 경우 사용함



# Spring boot

