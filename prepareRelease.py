#! /usr/bin/env python3
# LICENSE NOTE: I hereby release this script into public domain
# Should that not be possible under national law, I hereby grant everyone the right to use and modify this script
# to their heart's content. I don't provide any form of guarantee/warranty etc.

import re
import os
import sys
import subprocess


def printIfOk(ok):
    if ok:
        print("OK!")
    else:
        print("Issues found!")
    print("")


def getAllSrcFiles(baseFolder):
    fileList = []
    for root, _, files in os.walk(baseFolder):
        for file in files:
            # print(root + "/" + file)
            fileList.append(root + "/" + file)
    return fileList


def checkVersionTag(versionTag):
    version = re.search("\d+\.\d+\.\d+", versionTag)[0]  # .replace(".")
    print("Expecting version tag " + version + " in build.gradle.kts")
    with open("build.gradle.kts") as f:
        txt = f.read().replace("\"", "")
        regex = ("modVersion = " + version).replace(".", "\.")
        match = re.search(regex, txt)
        if match is None:
            print("Version not found!")
            return False
        print("Version found!")
        return True


def findTodos(folder):
    isOk = True
    srcFiles = getAllSrcFiles(folder)
    for file in srcFiles:
        with open(file, "r") as f:
            txt = f.readlines()
            i = 0
            for line in txt:
                i = i + 1
                todos = re.search("TODO.*", line)
                if todos:
                    isOk = False
                    print("TODO in " + file + ":" + str(i) + ": " + todos[0])
    return isOk


if "__main__" == __name__:
    if len(sys.argv) < 2:
        print("Missing positional argument")
        print("Usage:")
        print("python3 prepareRelease.py <tag>")
        exit()

    print("This script is a wrapper around 'git tag <tag>'")
    print("It will first look for TODOs and version mismatches and then do git tag for you.")
    print("")
    versionTag = sys.argv[1]
    print("Checking for TODOs....")
    todosOk = findTodos("src")
    printIfOk(todosOk)

    print("Checking version")
    versionOk = checkVersionTag(versionTag)
    printIfOk(versionOk)

    if not (todosOk and versionOk):
        print("WARNING! Found issues? Proceed anyways?")
        if not 'y' == input("Type 'y' to proceed, anything else to abort\n"):
            print("Aborting...")
            exit()
        print("Proceeding anyways")

    p = subprocess.Popen("git tag " + versionTag, shell=True)
    p.wait()
