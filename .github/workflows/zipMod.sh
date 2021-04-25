#!/bin/sh

# USAGE
# Run from within the 
# ./zipMod.sh "folderToZip" "outputFolderName"
#   arg folderToZip: The folder containing mod_info.json.
#     ex: "D:/Code/Persean-Chronicles"
#   arg outputFolderName: Do not append the version, it will be appended.
#     ex: "Persean-Chronicles"

folderToZip=$1
outputFolderName=$2
startingDir=$(pwd)

echo "Executing dir: $startingDir"
echo "Location of folder to zip:" $(realpath $1)

cd $folderToZip
version=$(git describe --tags)
zipName=$outputFolderName-$version.zip

echo "Version: $version"

# Recreate the temp folder if it happens to be present
rm -rf "./$outputFolderName-$version"
mkdir "$outputFolderName-$version"

# 1. List all files in git, which uses gitignore
# 2. Remove any file matching the blacklist (eg afphoto files)
# 3. Copy to a new folder with the mod name and version
echo "Reading blacklist...\n$(cat $startingDir/blacklist.txt)"
git ls-files | grep -Evf $startingDir/blacklist.txt | while read file; do cp --parents "$file" "$outputFolderName-$version"; done

# Zip the folder, then clean it up
zip -r $zipName "./$outputFolderName-$version"
echo "Created zip file at $(realpath $zipName)"
rm -rf "./$outputFolderName-$version"

# Move the zip to the artifacts folder in the executing directory
mkdir -p $startingDir/artifacts
mv ./$zipName $startingDir/artifacts/
echo "Moved zip file to $(realpath $startingDir/artifacts/$zipName)"

# Return to the executing directory
cd $startingDir