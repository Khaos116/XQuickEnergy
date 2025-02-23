@echo off
chcp 65001
setlocal enabledelayedexpansion

rem 设置 APK 文件路径
set apkPath="C:\ProgramData\Jenkins\.jenkins\workspace\58CP\app\build\outputs\apk\zh58\release\app-58-release.apk"

rem 运行 aapt 命令并将结果存储到变量中
for /f "delims=" %%i in ('aapt dump badging %apkPath% ^| findstr "versionName"') do (
    set "apkInfo=%%i"
)

rem 将 apkInfo 的值存储到另一个变量 versionInfo 中
set "versionInfo=!apkInfo!"

echo versionInfo: !versionInfo!
echo.

rem 找到 versionName 的下一个值并存储到 versionValue 变量中
set "nextValue="
set "isNextValue=0"

for %%j in (!versionInfo!) do (
    rem echo 子项: %%j 

    if "!isNextValue!"=="1" (
        set "nextValue=%%j"  rem 记录下一个值
        set "isNextValue=0"  rem 重置标志
    )

    if "%%j"=="versionName" (
        set "isNextValue=1"  rem 设置标志以记录下一个值
    )
)

rem 去掉单引号并输出 versionValue 变量的值
set "nextValue=!nextValue:'=!"
echo versionValue: !nextValue!
echo.

rem 暂停以便查看输出
pause
endlocal
