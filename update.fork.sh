#!/bin/bash
#pr.sh

# It is assumed that you forked this repo!
# Saves some typing to keep your fork synced with master upstream

git checkout master

#Ensure upstream is added to your fork
#This will not be a problem if it already exists
git remote add upstream https://github.com/smallrye/smallrye-open-api.git

#Update fork of project to current master
git fetch -v upstream
git checkout master
git merge -v upstream/master
git push -v
