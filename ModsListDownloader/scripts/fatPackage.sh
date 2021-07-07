if test -f "mcDownloader.jar"; then
    rm mcDownloader.jar
fi

pwd
LIBS=$( cd objs/ && ls ../lib/*.jar)
echo $LIBS
cd objs

for lib in ${LIBS}; do
    jar -xf $lib
done

rm META-INF -r

jar cfm ../mcDownloader.jar ../MANIFEST.MF *
