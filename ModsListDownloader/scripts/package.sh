if test -f "mcDownloader.jar"; then
    rm mcDownloader.jar
fi

cd objs && jar cfm ../mcDownloader.jar ../MANIFEST.MF downloader/
