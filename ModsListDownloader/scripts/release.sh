if [ -z "${VER}" ]
then
	VER="v0.2"	
fi
-rm release-${VER}.zip 2> /dev/null
mv mcDownloader.jar ModPackDl.jar
echo "java -jar ModPackDl.jar -t -v" > run.sh
echo "java -jar ModPackDl.jar -t -v" > run.bat
echo "pause" >> run.bat
zip release-${VER}.zip ModPackDl.jar run.sh run.bat
rm run.sh
rm run.bat
rm ModPackDl.jar
