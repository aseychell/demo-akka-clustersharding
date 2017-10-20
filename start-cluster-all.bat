@ECHO off
start "Seed" start-cluster-seed.bat

start "One" start-cluster-normal-node.bat
start "Two" start-cluster-normal-node.bat
