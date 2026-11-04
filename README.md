# midtermwork

게임 및 혼합현실 중간 프로젝트용 libGDX 프로젝트입니다.

## 현재 상태

- Java / libGDX 1.14.2 / LWJGL3 데스크톱 실행 환경 구성
- 기본 libGDX 로고 샘플 실행 확인
- 과제용 플랫폼 게임 기능은 아직 구현 전

## 실행

Android Studio에서 프로젝트 루트 폴더를 열고 Gradle 동기화를 완료합니다.

- 실행 구성: `midtermwork Desktop`
- Gradle: `lwjgl3 > Tasks > application > run`
- Windows: `run-desktop.bat` 실행
- 명령행: `gradlew.bat lwjgl3:run` (JDK 필요)

실행용 JAR 빌드: `gradlew.bat lwjgl3:jar`

## 폴더

- `core/src/main/java/com/midtermwork/Main.java`: 공통 게임 로직
- `lwjgl3`: 데스크톱 실행기
- `assets`: 이미지 등 리소스
- `.run`: 공유 가능한 Android Studio 실행 구성

## 개발 목표

제공된 과제 안내를 바탕으로 아래 기능을 구현할 예정입니다.

- 서로 구분되는 3개 이상의 스테이지
- 조작에 따라 이동하는 카메라
- 코드로 구현한 독창적인 게임 요소 3개 이상
- 일시정지 및 재개
- 전체 스테이지 클리어 표시 및 처음부터 재시작

## 제출 준비

발표자료, 채점 기준을 설명하는 레포트, 시연 영상 링크와 GitHub 링크를 준비합니다.
현재 저장소는 개발 시작 상태이며 완성된 제출물은 아닙니다.

## 참고 자료

- 프로젝트 생성: gdx-liftoff
- 프레임워크: libGDX
- 수업 예제 `Project2.zip`과 강의 PPT는 참고 자료이며 이 저장소에 포함하지 않습니다.
- 예제 코드나 외부 에셋을 이후 사용할 경우 출처와 이용 조건을 기록합니다.
