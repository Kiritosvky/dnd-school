# MIT License
#
# Copyright (c) 2026 Kiritosvky
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { "$env:LOCALAPPDATA\Android\Sdk" }
$bt = Get-ChildItem "$sdk\build-tools" -Directory | Sort-Object Name -Descending | Select-Object -First 1
$jar = Get-ChildItem "$sdk\platforms" -Directory | Sort-Object Name -Descending | Select-Object -First 1
$androidJar = Join-Path $jar.FullName "android.jar"
$out = Join-Path $root "build"
$gen = Join-Path $out "gen"
$cls = Join-Path $out "classes"
$dex = Join-Path $out "dex"
Remove-Item $out -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $gen, $cls, $dex | Out-Null

& "$($bt.FullName)\aapt2.exe" compile --dir "$root\app\res" -o "$out\res.zip"
& "$($bt.FullName)\aapt2.exe" link -o "$out\base.apk" -I $androidJar `
    --manifest "$root\app\AndroidManifest.xml" "$out\res.zip" --java $gen `
    --min-sdk-version 26 --target-sdk-version 36 --version-code 1 --version-name "1.0"

$src = @(Get-ChildItem "$root\app\src" -Recurse -Filter *.java | ForEach-Object FullName) +
       @(Get-ChildItem $gen -Recurse -Filter *.java | ForEach-Object FullName)
& javac -nowarn -source 11 -target 11 -classpath $androidJar -d $cls $src

$classes = @(Get-ChildItem $cls -Recurse -Filter *.class | ForEach-Object FullName)
& "$($bt.FullName)\d8.bat" --release --min-api 26 --lib $androidJar --output $dex $classes

Copy-Item "$out\base.apk" "$out\app-unsigned.apk"
& jar uf "$out\app-unsigned.apk" -C $dex classes.dex
& "$($bt.FullName)\zipalign.exe" -f 4 "$out\app-unsigned.apk" "$out\app-aligned.apk"

$ks = "$root\keystore.jks"
if (-not (Test-Path $ks)) {
& keytool -genkeypair -keystore $ks -alias dndschool -storepass android -keypass android `
    -dname "CN=Kiritosvky, O=Kiritosvky, C=RU" -keyalg RSA -keysize 2048 -validity 10950 | Out-Null
}
& "$($bt.FullName)\apksigner.bat" sign --ks $ks --ks-pass pass:android --key-pass pass:android `
    --out "$root\DndSchool.apk" "$out\app-aligned.apk"
& "$($bt.FullName)\apksigner.bat" verify --print-certs "$root\DndSchool.apk"
"BUILD OK: $root\DndSchool.apk"
