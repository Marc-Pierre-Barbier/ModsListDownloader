ALL:
	scripts/clean.sh;
	scripts/build.sh;
	scripts/package.sh;

FAT:
	scripts/clean.sh;
	scripts/build.sh;
	scripts/fatPackage.sh;

CLEAN:
	scripts/clean.sh;
