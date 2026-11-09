param(
    [string]$RuntimeHome = 'C:\Program Files\Android\Android Studio\jbr',
    [string]$OutputRoot = '',
    [switch]$SkipBuild
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (!$OutputRoot) { $OutputRoot = Join-Path $projectRoot ('dist\windows-' + (Get-Date -Format 'yyyyMMdd-HHmmss')) }
$OutputRoot = [IO.Path]::GetFullPath($OutputRoot)
if (!(Test-Path -LiteralPath (Join-Path $RuntimeHome 'bin\java.exe'))) { throw 'Runtime java.exe is missing.' }
if (!$SkipBuild) {
    $env:JAVA_HOME = $RuntimeHome
    Push-Location $projectRoot
    try { & '.\gradlew.bat' core:test lwjgl3:jarWin; if ($LASTEXITCODE -ne 0) { throw 'Gradle build failed.' } }
    finally { Pop-Location }
}
$jar = Join-Path $projectRoot 'lwjgl3\build\libs\midtermwork-1.0.0-win.jar'
if (!(Test-Path -LiteralPath $jar)) { throw 'Windows game JAR is missing.' }
$package = Join-Path $OutputRoot '포켓몬_불꽃의_여정_Windows'
if (Test-Path -LiteralPath $package) { throw 'Use a new output directory to preserve previous packages.' }
New-Item -ItemType Directory -Path (Join-Path $package 'app') -Force | Out-Null
Copy-Item -LiteralPath $RuntimeHome -Destination (Join-Path $package 'runtime') -Recurse
Copy-Item -LiteralPath $jar -Destination (Join-Path $package 'app\midtermwork.jar')
Copy-Item -LiteralPath (Join-Path $projectRoot 'assets\CREDITS.md') -Destination (Join-Path $package 'ASSET-CREDITS.md')
$compiler = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
& $compiler /nologo /target:winexe /platform:x64 /optimize+ /reference:System.Windows.Forms.dll "/out:$package\포켓몬_불꽃의_여정.exe" (Join-Path $PSScriptRoot 'PortableLauncher.cs')
if ($LASTEXITCODE -ne 0) { throw 'Launcher compilation failed.' }
$guide = @'
포켓몬 불꽃의 여정 Windows 현장 플레이 배포본

실행 방법
1. ZIP을 우클릭하고 '압축 풀기' 또는 '모두 압축 풀기'를 선택합니다.
2. 포켓몬_불꽃의_여정_Windows 폴더의 포켓몬_불꽃의_여정.exe를 더블클릭합니다.
3. runtime, app 폴더는 실행 파일과 함께 보관합니다. exe만 옮기면 실행되지 않습니다.

Android Studio, 별도 Java 설치, Gradle, 인터넷 연결은 실행에 필요하지 않습니다.
Windows 10/11 64비트용입니다. OpenGL 지원 그래픽 드라이버가 필요합니다.
압축 파일 내부에서 직접 실행하지 마세요. 현장 노트북에서도 미리 실행해 주세요.

조작
방향키: 이동 / Space: 점프 / 아래 방향키+Space: 발판 내려가기
J 또는 Z: 기본 공격 / X C A S: 기술 4칸 (기본 배치)
K: 배운 기술 다시 장착 / T: 기술머신 / F2: 기술 키 설정
Esc: 일시정지와 재개 / F8: 효과음 켜기와 끄기
R: 처음부터 / 기술에 R을 지정했다면 Ctrl+R: 처음부터
기절 후 Enter: 현재 구역 재도전
숫자 키 또는 마우스: 학습/보상 선택 / 도구·기술 위 마우스: 효과 설명

현장 시연
처음에는 X 할퀴기와 C 울음소리를 사용할 수 있습니다.
적을 처치하며 경험치를 얻고, 배울 기술을 선택해 장착합니다.
일반 맵 끝에서 보상을 고르면 다음 구역으로 이동합니다.
테마마다 일반 맵 4개와 보스방 1개이며 총 15구역입니다.
키 설정은 해당 Windows 사용자에 저장됩니다. F2에서 기본값으로 복원할 수 있습니다.
캠페인 진행은 게임 종료 후 저장되지 않습니다.

문제가 있을 때
실행 로그: %LOCALAPPDATA%\midtermwork\logs
실행 파일이 아니라 폴더 전체가 있는지, ZIP을 완전히 풀었는지 먼저 확인합니다.
소스 코드: https://github.com/awh57098-design/midtermwork

Java 런타임의 저작권 및 라이선스 문서는 runtime\legal에 포함됩니다.
게임 자산의 출처는 ASSET-CREDITS.md와 게임 JAR 내부의 credits 파일을 참고하세요.
'@
[IO.File]::WriteAllText((Join-Path $package '먼저읽기.txt'), $guide, [Text.UTF8Encoding]::new($true))
$metadata = [ordered]@{
    displayName = '포켓몬 불꽃의 여정'
    builtAt = (Get-Date).ToString('o')
    platform = 'windows-x64'
    gameJarSha256 = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash
    runtime = (Get-Content -LiteralPath (Join-Path $RuntimeHome 'release') | Select-Object -First 1)
}
$metadata | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $package 'build-info.json') -Encoding utf8
$zip = Join-Path $OutputRoot '포켓몬_불꽃의_여정_Windows.zip'
Compress-Archive -LiteralPath $package -DestinationPath $zip -CompressionLevel Optimal
Write-Output "PACKAGE=$package"
Write-Output "ZIP=$zip"
