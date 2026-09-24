# Reserve-Hub CI/CD 및 서버 배포 설정 정리

> Reserve-Hub 프로젝트에서 직접 구성한 Oracle Linux 배포 환경과 GitHub
> Actions CI/CD 설정을 정리한 문서입니다.

## 1. 전체 구성

``` text
Developer (Windows / IntelliJ)
        |
        | git push main
        v
GitHub Repository
        |
        +---------------- CI ----------------+
        |                                    |
        v                                    |
GitHub-hosted Runner                         |
  - Java 17 (Temurin)                        |
  - PostgreSQL 17 service                    |
  - Gradle Test                              |
  - Spring Boot bootJar                      |
  - JAR Artifact Upload                      |
        |                                    |
        +--------- test success -------------+
        |
        v
Self-hosted GitHub Actions Runner
(Oracle Linux 10 / OL10-3031424)
        |
        | Artifact Download
        v
/usr/local/bin/deploy-reserve-hub
        |
        +-- 기존 app.jar 백업
        +-- 새 app.jar 설치
        +-- systemctl restart reserve-hub
        +-- 실패 시 rollback
        |
        v
Spring Boot (:8080)
        |
        v
PostgreSQL 17 (Docker)

External Request
        |
        v
firewalld (:80)
        |
        v
Nginx (:80)
        |
        | reverse proxy
        v
Spring Boot (:8080)
```

------------------------------------------------------------------------

## 2. 주요 환경

  항목                 설정
  -------------------- ---------------------------------------
  OS                   Oracle Linux 10
  VM Hostname          `OL10-3031424`
  Host-only IP         `192.168.56.101`
  Application          Spring Boot
  Build target         Java 17
  Server runtime       OpenJDK 21
  Database             PostgreSQL 17 Alpine / Docker
  Reverse Proxy        Nginx 1.26.3
  Application Port     `8080`
  External HTTP Port   `80`
  CI                   GitHub Actions / GitHub-hosted Runner
  CD                   GitHub Actions / Self-hosted Runner
  Application JAR      `/opt/reserve-hub/app.jar`
  Runner               `/opt/actions-runner`

> Spring Boot 프로젝트는 Java 17을 대상으로 빌드하지만, Oracle JDK 17이
> systemd 환경에서 실행될 때 메모리 실행 권한 관련 문제가 발생하여
> 서버의 systemd 서비스는 OpenJDK 21을 사용하도록 구성했습니다.

------------------------------------------------------------------------

## 3. PostgreSQL Docker 설정

PostgreSQL은 Docker 컨테이너로 실행합니다.

``` yaml
services:
  postgres:
    image: postgres:17-alpine
    container_name: reserve-hub-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: testdb
      POSTGRES_USER: testuser
      POSTGRES_PASSWORD: testpass
    ports:
      - "5432:5432"
    volumes:
      - reserve_hub_postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U testuser -d testdb"]
      interval: 5s
      timeout: 3s
      retries: 10

volumes:
  reserve_hub_postgres_data:
```

재시작 정책 확인:

``` bash
docker inspect -f '{{.HostConfig.RestartPolicy.Name}}' reserve-hub-postgres
```

결과:

``` text
unless-stopped
```

------------------------------------------------------------------------

## 4. Spring Boot systemd 서비스

배포 JAR 위치:

``` text
/opt/reserve-hub/app.jar
```

서비스는 systemd에서 관리합니다.

핵심 실행 설정:

``` ini
ExecStart=/usr/lib/jvm/java-21-openjdk/bin/java -jar /opt/reserve-hub/app.jar
```

서비스 관리:

``` bash
systemctl status reserve-hub
systemctl restart reserve-hub
systemctl enable reserve-hub
journalctl -u reserve-hub -n 30 --no-pager
```

정상 배포 후 확인된 상태:

``` text
Active: active (running)
Tomcat started on port 8080
Started ReserveHubApplication
```

또한 PostgreSQL 연결, HikariCP, Flyway, JPA 초기화까지 정상 동작하는
것을 로그에서 확인했습니다.

------------------------------------------------------------------------

## 5. Nginx Reverse Proxy

설정 파일:

``` text
/etc/nginx/conf.d/reserve-hub.conf
```

설정:

``` nginx
server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Oracle Linux 기본 Nginx 설정에 이미 `listen 80`인 default server가
존재하여 충돌이 발생했기 때문에 기본 server block은 비활성화하고
`conf.d`의 Reserve-Hub 설정을 사용했습니다.

------------------------------------------------------------------------

## 6. firewalld

외부에서 Nginx의 HTTP 포트로 접근할 수 있도록 HTTP 서비스를
허용했습니다.

``` bash
firewall-cmd --permanent --add-service=http
firewall-cmd --reload
firewall-cmd --list-services
```

현재 허용 서비스에는 다음이 포함됩니다.

``` text
cockpit dhcpv6-client http https ssh
```

> `https`가 firewalld에서 허용되어 있더라도 현재 Nginx TLS 인증서 설정을
> 별도로 구성한 것은 아니므로 HTTPS 서비스가 실제 구성된 것을 의미하지는
> 않습니다.

------------------------------------------------------------------------

## 7. SELinux - Nginx에서 Spring Boot 접근

SELinux는 `Enforcing` 상태로 유지했습니다.

초기에는 Nginx가 `127.0.0.1:8080`의 Spring Boot에 접근할 때 SELinux가
연결을 차단하여 `502 Bad Gateway`가 발생했습니다.

Audit 로그에서 다음 유형의 차단을 확인했습니다.

``` text
avc: denied { name_connect }
comm="nginx"
dest=8080
```

해결:

``` bash
setsebool -P httpd_can_network_connect 1
```

이후 Nginx → Spring Boot 연결이 정상화되었습니다.

------------------------------------------------------------------------

## 8. 외부 접속 검증

Windows에서 VM의 Host-only IP를 통해 API를 호출했습니다.

``` powershell
curl.exe -i http://192.168.56.101/api/reservations/31a18023-df6f-4ac9-8853-a09472c2ec8b
```

응답:

``` text
HTTP/1.1 200
Server: nginx/1.26.3
Content-Type: application/json
```

예시 JSON:

``` json
{
  "id": "31a18023-df6f-4ac9-8853-a09472c2ec8b",
  "resourceId": "00000000-0000-0000-0000-000000000001",
  "memberId": "00000000-0000-0000-0000-000000000002",
  "startAt": "2026-10-01T09:00:00",
  "endAt": "2026-10-01T11:00:00",
  "status": "CONFIRMED"
}
```

따라서 다음 전체 요청 경로가 정상임을 확인했습니다.

``` text
Windows
 -> firewalld :80
 -> Nginx :80
 -> Spring Boot :8080
 -> JPA
 -> PostgreSQL Docker
 -> HTTP 200
```

------------------------------------------------------------------------

# 9. GitHub Actions CI

파일:

``` text
.github/workflows/ci.yml
```

CI에서는 GitHub-hosted Ubuntu Runner를 사용합니다.

주요 동작:

1.  Repository checkout
2.  Java 17 설정
3.  PostgreSQL 17 테스트 서비스 실행
4.  Gradle 테스트
5.  `bootJar` 생성
6.  JAR artifact 업로드

설정:

``` yaml
name: Reserve Hub CI

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:17-alpine
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        ports:
          - 5432:5432
        options: >-
          --health-cmd="pg_isready -U testuser -d testdb"
          --health-interval=5s
          --health-timeout=3s
          --health-retries=10

    env:
      DB_URL: jdbc:postgresql://localhost:5432/testdb
      DB_USERNAME: testuser
      DB_PASSWORD: testpass

    steps:
      - name: Checkout source
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: gradle

      - name: Grant execute permission
        run: chmod +x gradlew

      - name: Run tests
        run: ./gradlew clean test

      - name: Build JAR
        run: ./gradlew bootJar

      - name: Upload JAR artifact
        uses: actions/upload-artifact@v4
        with:
          name: reserve-hub-jar
          path: build/libs/*.jar
```

------------------------------------------------------------------------

# 10. GitHub Actions Self-hosted Runner

CD를 위해 Oracle Linux VM 자체를 GitHub Actions Self-hosted Runner로
등록했습니다.

Runner 설치 위치:

``` text
/opt/actions-runner
```

Runner 정보:

``` text
Runner name: OL10-3031424
Labels:
- self-hosted
- Linux
- X64
```

## Runner systemd 서비스

Runner는 터미널에서 `./run.sh`를 계속 실행하는 대신 systemd 서비스로
등록했습니다.

``` bash
cd /opt/actions-runner

./svc.sh install deploy
./svc.sh start
./svc.sh status
```

정상 상태:

``` text
Active: active (running)

RunnerService.js
Runner.Listener run --startuptype service
```

따라서 VM 재부팅 후에도 Runner가 자동으로 실행됩니다.

------------------------------------------------------------------------

# 11. Runner 구성 중 발생한 문제 1 - 시스템 시간

Runner 등록은 성공했지만 다음 오류로 session 생성이 실패했습니다.

``` text
Failed to create a session.
The runner registration has been deleted from the server.
```

Runner `_diag` 로그를 확인한 결과 실제 원인은 OAuth token 만료였습니다.

``` text
The token expired ...
Current server time ...
```

`chronyc tracking` 결과 VM 시간이 NTP보다 약 57,004초(약 15시간 50분)
느린 상태였습니다.

``` text
System time : 57004... seconds slow of NTP time
```

NTP 서비스인 `chronyd` 자체는 실행 중이었습니다.

``` bash
systemctl status chronyd
chronyc sources -v
```

root 권한으로 시간을 즉시 보정했습니다.

``` bash
chronyc makestep
```

정상화 후:

``` text
System time : 0.000000001 seconds slow of NTP time
Leap status : Normal
```

그 후 Runner를 다시 등록하여 다음 상태까지 정상 진입했습니다.

``` text
Connected to GitHub
Current runner version: '2.337.0'
Listening for Jobs
```

### 교훈

표면적인 오류 메시지만 보지 않고 Runner의 `_diag` 로그를 확인하여 실제
root cause인 시스템 시간 불일치를 찾아냈습니다.

------------------------------------------------------------------------

# 12. Runner 구성 중 발생한 문제 2 - SELinux

Runner를 처음에는 다음 위치에 설치했습니다.

``` text
/home/deploy/actions-runner
```

수동 실행:

``` bash
./run.sh
```

은 성공했지만 systemd 서비스로 실행하면 다음 오류가 발생했습니다.

``` text
status=203/EXEC
Permission denied
```

파일 자체에는 실행 권한이 있었습니다.

``` text
-rwxr-xr-x deploy deploy runsvc.sh
```

SELinux audit 로그 확인:

``` bash
ausearch -m AVC -ts recent
```

결과:

``` text
avc: denied { execute }
scontext=system_u:system_r:init_t:s0
tcontext=unconfined_u:object_r:user_home_t:s0
```

즉 systemd의 `init_t` domain에서 home directory의 `user_home_t` 파일
실행이 SELinux에 의해 차단되고 있었습니다.

Runner를 `/opt`로 이동했습니다.

``` bash
mv /home/deploy/actions-runner /opt/actions-runner
chown -R deploy:deploy /opt/actions-runner
restorecon -RFv /opt/actions-runner
```

SELinux context 확인:

``` bash
ls -Z /opt/actions-runner/runsvc.sh
```

결과:

``` text
system_u:object_r:usr_t:s0 /opt/actions-runner/runsvc.sh
```

이후 systemd 서비스 실행이 정상화되었습니다.

### 교훈

`chmod 777`이나 `setenforce 0`으로 우회하지 않고 SELinux를 Enforcing
상태로 유지하면서 실행 위치와 보안 컨텍스트를 정상화했습니다.

------------------------------------------------------------------------

# 13. 배포 전용 스크립트

GitHub Actions Runner는 `deploy` 사용자로 실행됩니다.

Runner에 전체 root 권한을 주지 않고 배포에 필요한 동작만 root 권한으로
실행하기 위해 전용 스크립트를 만들었습니다.

파일:

``` text
/usr/local/bin/deploy-reserve-hub
```

내용:

``` bash
#!/bin/bash

set -euo pipefail

SOURCE_JAR="${1:-}"
TARGET_DIR="/opt/reserve-hub"
TARGET_JAR="$TARGET_DIR/app.jar"
BACKUP_JAR="$TARGET_DIR/app.jar.bak"
SERVICE="reserve-hub.service"

if [ -z "$SOURCE_JAR" ]; then
    echo "ERROR: JAR file path is required."
    exit 1
fi

if [ ! -f "$SOURCE_JAR" ]; then
    echo "ERROR: JAR file not found: $SOURCE_JAR"
    exit 1
fi

echo "=== Reserve-Hub deployment started ==="

if [ -f "$TARGET_JAR" ]; then
    cp "$TARGET_JAR" "$BACKUP_JAR"
    echo "Backup created: $BACKUP_JAR"
fi

install -o root -g root -m 0644 "$SOURCE_JAR" "$TARGET_JAR"

echo "New JAR installed."

systemctl restart "$SERVICE"

if systemctl is-active --quiet "$SERVICE"; then
    echo "Reserve-Hub is running."
else
    echo "ERROR: Reserve-Hub failed to start."

    if [ -f "$BACKUP_JAR" ]; then
        echo "Rolling back..."
        cp "$BACKUP_JAR" "$TARGET_JAR"
        systemctl restart "$SERVICE"
    fi

    exit 1
fi

echo "=== Deployment completed successfully ==="
```

권한:

``` bash
chmod 755 /usr/local/bin/deploy-reserve-hub
chown root:root /usr/local/bin/deploy-reserve-hub
```

------------------------------------------------------------------------

# 14. 최소 sudo 권한

Self-hosted Runner의 `deploy` 사용자에게 전체 sudo 권한을 부여하지 않고
배포 스크립트만 허용했습니다.

파일:

``` text
/etc/sudoers.d/reserve-hub-deploy
```

설정:

``` text
deploy ALL=(root) NOPASSWD: /usr/local/bin/deploy-reserve-hub *
```

sudoers 파일 권한:

``` bash
chmod 440 /etc/sudoers.d/reserve-hub-deploy
chown root:root /etc/sudoers.d/reserve-hub-deploy
```

문법 검증:

``` bash
visudo -c
```

결과:

``` text
/etc/sudoers: parsed OK
/etc/sudoers.d/reserve-hub-deploy: parsed OK
```

권한 확인:

``` bash
sudo -l
```

허용되는 명령:

``` text
(root) NOPASSWD: /usr/local/bin/deploy-reserve-hub *
```

------------------------------------------------------------------------

# 15. CD Job

CI의 `test` job이 성공한 후에만 `deploy` job이 실행됩니다.

``` yaml
  deploy:
    needs: test

    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    runs-on: [self-hosted, Linux, X64]

    steps:
      - name: Download JAR artifact
        uses: actions/download-artifact@v4
        with:
          name: reserve-hub-jar
          path: deploy-artifact

      - name: Deploy Reserve Hub
        run: |
          JAR_FILE=$(find deploy-artifact -name "*.jar" -type f | head -1)
          sudo /usr/local/bin/deploy-reserve-hub "$JAR_FILE"
```

`needs: test`를 통해 CI 테스트가 실패하면 배포되지 않습니다.

또한 다음 조건을 사용합니다.

``` yaml
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```

따라서 Pull Request에서는 실제 배포 서버의 Self-hosted Runner를 사용하지
않고, `main` branch에 push된 경우에만 CD가 실행됩니다.

------------------------------------------------------------------------

# 16. 최종 CI/CD 동작

현재 최종 흐름:

``` text
1. Windows에서 코드 수정
        |
2. git push main
        |
3. GitHub Actions CI
        |
        +-- PostgreSQL test service
        +-- Java 17
        +-- ./gradlew clean test
        +-- ./gradlew bootJar
        +-- JAR artifact upload
        |
4. CI 성공
        |
5. Self-hosted Runner가 deploy job 수신
        |
6. JAR artifact 다운로드
        |
7. sudo deploy-reserve-hub
        |
8. 기존 app.jar -> app.jar.bak
        |
9. 새 JAR -> /opt/reserve-hub/app.jar
        |
10. systemctl restart reserve-hub
        |
11. Spring Boot 시작
        |
12. Nginx를 통한 외부 API 요청
        |
13. HTTP 200 확인
```

GitHub Actions에서 최종적으로:

``` text
test    SUCCESS
  |
deploy  SUCCESS
```

를 확인했습니다.

------------------------------------------------------------------------

# 17. 장애 대응 과정 요약

  -----------------------------------------------------------------------------------------------------
  문제              원인              확인 방법            해결
  ----------------- ----------------- -------------------- --------------------------------------------
  Runner session    VM 시스템 시간이  `_diag` Runner 로그, `chronyc makestep`
  생성 실패         약 15시간 50분    `chronyc tracking`   
                    느림                                   

  Runner systemd    SELinux가 home    `ausearch -m AVC`    Runner를 `/opt/actions-runner`로 이동 후
  `203/EXEC`        directory                              `restorecon`
                    스크립트 실행                          
                    차단                                   

  Nginx 404         기본 Nginx server Nginx 설정 확인      기본 server block 비활성화
                    block과 설정 충돌                      

  Nginx 502         SELinux가 Nginx   SELinux AVC 로그     `setsebool -P httpd_can_network_connect 1`
                    -\> 8080 연결                          
                    차단                                   

  CD Artifact not   `bootJar` 단계    GitHub Actions       `./gradlew bootJar` 단계 추가
  found             누락              deploy 로그          

  sudoers 권한 경고 sudoers 파일      `visudo -c`          `chmod 440`
                    mode가 0440이                          
                    아님                                   
  -----------------------------------------------------------------------------------------------------

------------------------------------------------------------------------

# 18. 현재 완료 상태

-   [x] PostgreSQL Docker 구성
-   [x] Docker restart policy 설정
-   [x] Spring Boot systemd 서비스
-   [x] Nginx Reverse Proxy
-   [x] firewalld HTTP 허용
-   [x] SELinux Enforcing 유지
-   [x] 외부 Windows -\> Nginx -\> Spring Boot -\> PostgreSQL API 통신
-   [x] GitHub Actions CI
-   [x] Java 17 CI build/test
-   [x] JAR Artifact 생성
-   [x] Oracle Linux Self-hosted Runner
-   [x] Runner systemd 자동 시작
-   [x] 최소 sudo 배포 권한
-   [x] 기존 JAR 백업
-   [x] 배포 실패 시 rollback 로직
-   [x] main push 기반 자동 CD
-   [x] CI/CD 실제 배포 성공
-   [x] 배포 후 외부 API HTTP 200 검증

------------------------------------------------------------------------

## 19. 포트폴리오 설명 예시

> Reserve-Hub 프로젝트의 배포 환경을 Oracle Linux 기반으로 구성하고,
> Nginx Reverse Proxy와 systemd를 이용해 Spring Boot 애플리케이션을
> 운영했습니다. PostgreSQL은 Docker로 분리하여 관리했습니다.
>
> GitHub Actions에서는 GitHub-hosted Runner를 이용해 Java 17 기반 테스트
> 및 빌드를 수행하고, 성공한 JAR을 Artifact로 생성하도록 CI를
> 구성했습니다. 이후 Oracle Linux 서버에 Self-hosted Runner를 구축하여
> CI에서 검증된 Artifact만 실제 서버에 배포하도록 CD 파이프라인을
> 연결했습니다.
>
> 배포 과정에서는 Runner에 전체 root 권한을 부여하지 않고 전용 배포
> 스크립트에 대해서만 제한적인 sudo 권한을 허용했으며, 기존 JAR 백업 및
> 실패 시 rollback 구조를 추가했습니다.
>
> 구축 과정에서 VM의 NTP 시간 불일치로 인한 GitHub OAuth 인증 실패와
> SELinux 정책에 의한 systemd 실행 차단 문제를 로그 기반으로 분석하고
> 해결했습니다. SELinux를 비활성화하지 않고 올바른 파일 위치와 보안
> 컨텍스트를 적용하여 운영 환경의 보안 설정을 유지했습니다.

------------------------------------------------------------------------

## 20. 주요 운영 확인 명령어

``` bash
# Reserve-Hub
systemctl status reserve-hub --no-pager
journalctl -u reserve-hub -n 30 --no-pager

# Nginx
systemctl status nginx
nginx -t

# PostgreSQL Docker
docker ps
docker inspect -f '{{.HostConfig.RestartPolicy.Name}}' reserve-hub-postgres

# GitHub Actions Runner
cd /opt/actions-runner
./svc.sh status

# NTP
chronyc tracking
chronyc sources -v

# SELinux
getenforce
ausearch -m AVC -ts recent

# Firewall
firewall-cmd --list-services
```

Windows 외부 확인:

``` powershell
curl.exe -i http://192.168.56.101/api/reservations/<reservation-id>
```

------------------------------------------------------------------------

**작성 기준:** Reserve-Hub의 실제 구축 및 트러블슈팅 과정\
**상태:** CI/CD 및 외부 API 배포 검증 완료
