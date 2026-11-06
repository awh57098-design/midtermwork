# midtermwork

게임 및 혼합현실 중간 프로젝트: 포켓몬을 소재로 한 2D 액션 플랫포머 프로토타입입니다.

## 현재 플레이 버전

숲·수정 동굴·화산 각 테마마다 일반 맵 4개와 독립 보스방 1개를 진행합니다. 일반 맵 12개와 보스방 3개, 총 15구역입니다.

| 테마 | 일반 맵 | 독립 보스방 |
|---|---|---|
| 숲 | 1-1 ~ 1-4 | 1-5 레트라 |
| 수정 동굴 | 2-1 ~ 2-4 | 2-5 아보크 |
| 화산 | 3-1 ~ 3-4 | 3-5 마그마 |

## 맵 구성

각 테마 안에서 네 일반 맵의 지형 구성을 다르게 설계했습니다.

1. TRAIL: 기본 탐험, 발판과 함정. 동굴은 고층 암반 계단을 포함합니다.
2. HIGH ROUTE: 높은 계단을 연속으로 오르내리는 경로와 높은 위치의 적.
3. RAVINES: 네 개의 낭떠러지 구간과 중간 함정 우회 발판.
4. GAUNTLET: 단차가 있는 지상 전투 구간, 넓은 가시와 우회 발판.
5. BOSS CHAMBER: 일반 적과 탐험 길을 제외한 한 화면짜리 보스 전용 공간.

일반 맵은 월드 너비 4500의 횡스크롤 맵이고, 보스방은 너비 1280으로 카메라가 고정됩니다. 일반 맵에는 체크포인트 3곳이 있습니다. 동굴의 낙석과 화산의 분출은 일반 맵에서 작동하며, 보스방에서는 보스의 전투 패턴을 사용합니다.

## 진행과 재도전

- 일반 맵 끝의 깃발에 도착하면 다음 일반 맵으로 이동하며, 네 번째 맵 다음에는 보스방에 들어갑니다.
- 보스방에서는 보스를 쓰러뜨려야 출구를 사용할 수 있습니다. 보스방을 클리어하면 다음 테마의 첫 맵으로 넘어갑니다.
- 3-5 마그마를 처치한 뒤 출구에 도착하면 최종 클리어 화면을 표시합니다.
- 맵 전환은 안내 후 1.4초 뒤 자동 진행되며 Enter로 즉시 이동할 수도 있습니다.
- 레벨·경험치·진화 단계·해금 기술·누적 처치는 유지됩니다. 맵 입장 시 체력과 기술 쿨다운은 회복됩니다.
- 기절하면 Enter로 현재 맵의 입장 시점 성장 상태로 재도전합니다. 실패한 시도의 경험치를 초기화해 반복 파밍을 막습니다.
- R은 전체 모험을 1-1, 레벨 1부터 다시 시작합니다.

## 전투와 성장

일반 적 체력은 테마에 따라 65/90/115, 경험치는 25/40/55입니다. 보스 체력은 260/340/440이며 경험치는 600/1000/1400입니다. 레트라는 2연속 돌진, 아보크는 독탄 연사, 마그마는 화염탄과 경고 후 화염 기둥을 번갈아 사용합니다.

체력은 하트 3개로 시작합니다. 접촉 0.5개, 돌진·함정·낙석·분출 1개, 일반 독탄 1.5개를 잃으며 피격 후 약 1.1초 무적입니다. 보스 투사체는 하트 1개 피해를 줍니다. 레벨업 시 공격력이 오르며 Lv.16 리자드(하트 4개), Lv.36 리자몽(하트 5개) 진화 규칙은 유지합니다. 전체 성장 속도·진화 도달 시점은 추가 밸런스 조정 대상입니다.

## 움직임과 연출

파이리와 꼬렛·아보에 옆모습 대기/걷기/공격/피격 프레임 애니메이션을 적용했습니다. 파이리는 점프 및 착지 자세도 구분합니다. 이동에는 지상·공중 가속과 감속이 적용되며, 타격 순간의 짧은 정지, 스파크, 피격 깜빡임, 먼지 효과와 카메라 완충 이동을 추가했습니다. 리자드·리자몽의 상태별 애니메이션은 후속 작업입니다.

## 숲 맵 그래픽

다층 숲과 구름 배경, 잔디·흙 지형, 이끼 낀 바위 발판, 돌 가시 함정, 꽃·풀·덤불과 체크포인트·도착 표지판을 픽셀아트로 적용했습니다. 배경은 카메라보다 느리게 이동하며, 장식과 충돌 지형은 분리되어 있습니다. 기존 맵의 점프 경로와 함정 피해 판정은 유지합니다.

## 기술 4개

파이리가 배울 수 있는 기술을 선택했습니다. 아래 해금 레벨과 쿨다운, 상태 효과는 이 액션 게임의 테스트 맵용 설정이며 원작의 습득 레벨·턴제 수치를 그대로 옮긴 값은 아닙니다.

| 키 | 기술 | 게임 해금 레벨 | 쿨다운 | 효과 |
|---|---|---|---|---|
| X | 불꽃세례 / Ember | 1 | 1.4초 | 전방 불꽃 투사체, 한 대상 적중 후 화상 |
| C | 연막 / Smokescreen | 2 | 6초 | 3초간 연막, 적 공격 준비 방해·둔화·독 투사체 차단 |
| A | 용의숨결 / Dragon Breath | 3 | 3.8초 | 관통 투사체, 대상별 1회 피해·2.5초 둔화 |
| S | 불꽃엄니 / Fire Fang | 4 | 4.8초 | 강한 전방 근접 범위 공격·짧은 화상, 회복 중인 적에게 반격 보너스 |

X는 시작부터 사용 가능하며 C/A/S는 레벨업으로 순서대로 해금됩니다. 기존 A 이동은 스킬 충돌을 피하기 위해 방향키 이동으로 통일했습니다. 하단 스킬창에 잠금 레벨·사용 가능 여부·남은 쿨다운을 표시합니다. 시전 도중 피격되면 취소되며, 일시정지 중에는 시전·쿨다운이 멈춥니다.

기술 습득 목록 참고: https://pokemondb.net/pokedex/charmander/moves/9

## 조작

| 키 | 동작 |
|---|---|
| 좌우 방향키 | 이동 |
| Space | 지상에서 점프 |
| J 또는 Z | 기본공격 (누르고 있으면 반복) |
| X / C / A / S | 기술 4개 (한 번씩 누르기) |
| Enter | 다음 구역으로 즉시 이동 / 기절 시 현재 맵 재도전 |
| ESC | 일시정지 / 재개 |
| R | 처음부터 재시작 |

## 실행

Android Studio에서 프로젝트 루트를 열고 Gradle 동기화를 완료합니다.

- 실행 구성: `midtermwork Desktop`
- Gradle: `lwjgl3 > Tasks > application > run`
- Windows: `run-desktop.bat`
- 명령행: `gradlew.bat lwjgl3:run` (JDK 필요)
- 실행용 JAR 빌드: `gradlew.bat lwjgl3:jar`
- 게임 규칙 검사: `gradlew.bat core:test`

Java / libGDX 1.14.2 / LWJGL3 / Gradle Wrapper를 사용합니다.

## 코드

- `core/src/main/java/com/midtermwork/MapLayout.java`: 일반 맵·보스방 지형과 적 배치 카탈로그
- `core/src/main/java/com/midtermwork/GameWorld.java`: 이동, 충돌, 전투, 성장, 15구역 진행 규칙
- `core/src/main/java/com/midtermwork/Main.java`: 입력, 게임 화면, HUD
- `core/src/test/java/com/midtermwork/GameWorldChecks.java`: 게임 규칙 검증
- `lwjgl3`: 데스크톱 실행기
- `assets`: 리소스 및 `CREDITS.md` 출처
- `.run`: 공유 Android Studio 실행 구성

## 앞으로의 개발 목표

진화 연출, 성장·전투 밸런스, 포켓몬 도구와 사운드를 보완합니다.

과제 발표자료, 채점 기준 설명 레포트, 시연 영상과 GitHub 링크는 최종 구현에 맞춰 준비합니다.

## 참고 자료

프로젝트는 gdx-liftoff로 생성했습니다. 포켓몬 스프라이트의 출처와 권리 표시는 `assets/CREDITS.md`를 참고하세요.
수업의 `Project2.zip`과 강의 PPT는 별도 참고 자료이며 저장소에 포함하지 않았습니다.

## Enemy loot and held items

Normal enemies drop one item with 65% probability; bosses always drop one. Walk close to collect. The upper-left inventory displays owned held tools, with a count beside each icon. Hover for the current stacked effect and explanation. Hover also works while paused (ESC) and after resizing, including letterboxed windows.

Held tools have no collection cap. Every copy contributes; copies are no longer converted to healing. Rare Candy and Sitrus Berry remain immediate consumables. All effects below are custom rules for this platformer.

| Item | Drop weight | Effect per copy |
| --- | ---: | --- |
| Rare Candy | 40% | Gain one level, heal one heart |
| Sitrus Berry | 10% | Heal one heart |
| Multi Lens | 4% | One delayed secondary hit, +50% original damage per copy |
| Wide Lens | 4% | Homing; extra copies add 100 range and faster steering |
| Charcoal | 5% | Fire skill direct damage +20% |
| Dragon Fang | 5% | Dragon skill damage +20% |
| Focus Sash | 4% | One charge saves a lethal hit with half a heart |
| Leftovers | 3% | Heal half a heart every 12 active seconds |
| Muscle Band | 4% | Basic attacks and Fire Fang +15% |
| Wise Glasses | 4% | Ember and Dragon Breath +15% |
| Shell Bell | 3% | +1 healing charge per direct hit; 6 charge heals half a heart |
| Scope Lens | 3% | Critical chance = 1 - 0.9^copies; critical damage x1.5 |
| Quick Claw | 3% | Cooldown multiplier = 1 / (1 + 0.12 * copies) |
| Silk Scarf | 3% | Basic attack damage +20% |
| Lucky Egg | 3% | Enemy EXP +25% |
| Expert Belt | 2% | Direct damage against bosses +20% |

Weights are conditional on a drop occurring, totaling 100%. Burn ticks and Multi Lens echoes do not create further echoes or Shell Bell charges. Tool counts carry into the next map; retry restores inventory and growth present at map entry. R clears the adventure. Icons use PokeAPI item sprites; see assets/CREDITS.md. The icon/count/hover presentation is inspired by PokeRogue (https://github.com/pagefaultgames/pokerogue); no PokeRogue code or assets are copied.
## 한국어 UI 및 발판 조작
화면 안내, 기술, 적 상태, 도구 이름과 설명을 한국어로 표시합니다. 발판 위에서 아래 방향키를 누른 채 Space를 누르면 해당 높이의 발판을 통과합니다. 아래 발판과 단단한 바닥에는 정상적으로 착지합니다. 좁은 틈에서는 몸 전체가 아닌 발 중심 14px로 착지를 판정합니다. 일반 Space 점프는 그대로 사용할 수 있습니다.

## 화면 크기와 도구 마우스 판정
게임 창은 모니터 작업 영역 안에서 16:9로 시작하며, 창 크기를 바꾸면 여백을 두고 화면 전체를 맞춥니다. HUD 전용 뷰포트를 사용하고 마우스도 같은 영역의 좌표로 변환합니다. 도구 아이콘은 투명 여백을 제거해 표시하며, 커서를 올린 아이콘에는 테두리가 표시됩니다. 오른쪽 위 공격력은 레벨과 기본 공격용 도구를 반영한 값이며, 보스 배율·치명타·적의 빈틈 배율은 타격 순간에 적용합니다.

## 가장자리 충돌 및 현재 커서 좌표
플레이어 충돌은 이전 위치와 목표 위치 사이에서 실제로 통과한 면을 기준으로 판정합니다. 발판에서 떨어지는 도중 몸 일부가 지형에 겹쳐도 지형 반대편 끝으로 밀어내지 않습니다. Windows에서는 GetPhysicalCursorPos로 물리 좌표를 읽고, 같은 DPI 인식 컨텍스트에서 ScreenToClient와 GetClientRect로 창 내부 좌표와 실제 크기를 구한 뒤 게임의 논리 크기에 맞춥니다. 스레드 DPI 컨텍스트는 읽기가 끝나면 복원합니다. 다른 플랫폼 및 실패 시에는 GLFW 커서 좌표를 사용합니다.

도구 설명은 실제 아이콘의 32x32 표시 영역 안에서 불투명한 이미지 픽셀 위에 커서가 있을 때만 표시합니다. 이미지의 투명한 부분, 수량 텍스트, 테두리 및 아래 빈 영역은 마우스 판정에서 제외합니다.

## 실제 렌더링 위치에 따른 도구 판정
도구를 그릴 때 SpriteBatch의 실제 변환 행렬과 GL 뷰포트에서 화면 픽셀 사각형을 기록합니다. 마우스를 프레임버퍼 좌표로 바꿔 이 사각형과 이미지 알파 마스크로 판정하며, 슬롯의 논리 좌표는 마우스 판정에 사용하지 않습니다. 고해상도 버퍼 비율과 화면 여백을 함께 반영합니다.

회귀 확인 도구: tools/FramebufferHoverCheck.java. assets 폴더에서 Java 소스 실행 기능으로 완성 JAR를 classpath에 넣고 창의 가로·세로 크기를 인자로 실행합니다. 실제 프레임버퍼에서 아이콘이 있는/없는 화면을 비교해 보이는 픽셀을 찾고, 그 픽셀에서만 설명이 뜨는지 확인합니다. 테스트 스크린샷은 build/hover-check에 저장합니다.

## 게임 내 십자 커서
데스크톱 게임 창 안에서는 OS 화살표를 숨기고 십자 커서를 표시합니다. 십자는 HUD 논리 좌표를 다시 투영하지 않고, 도구 판정에 사용하는 동일한 프레임버퍼 픽셀 좌표에 마지막으로 그립니다. 맵 카메라와 독립적으로 움직이며 창 밖 또는 포커스 해제 시 일반 화살표를 복원합니다. 표시/입력 배율 차이의 근본 원인은 별도로 확인 중이며, 게임 내 보이는 커서와 도구 판정을 일치시키는 방식입니다.
