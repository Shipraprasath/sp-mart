@echo off
set GIT="C:\Users\sprda\.gemini\antigravity\scratch\MinGit\cmd\git.exe"

%GIT% init
%GIT% config user.name "SP Mart Developer"
%GIT% config user.email "sprda@spmart.com"
%GIT% add .
%GIT% commit -m "Initial commit: SP MART v1.0 - Full-Stack Electronics E-Commerce Web Application with AI and PWA"
%GIT% branch -M main
%GIT% status
%GIT% log -1 --oneline
