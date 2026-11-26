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

param([int]$Port = 8765)

$dir = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "logs"
New-Item -ItemType Directory -Force $dir | Out-Null
$file = Join-Path $dir ("log-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".txt")

$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $Port)
$listener.Start()
"Listening on 0.0.0.0:$Port -> $file"

while ($true) {
    $client = $listener.AcceptTcpClient()
    try {
        $reader = [System.IO.StreamReader]::new($client.GetStream())
        while ($null -ne ($line = $reader.ReadLine())) {
            $line | Tee-Object -FilePath $file -Append
        }
    } catch {
        $_.Exception.Message | Tee-Object -FilePath $file -Append
    } finally {
        $client.Close()
    }
}
