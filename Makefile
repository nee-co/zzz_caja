REVISION=`git rev-parse HEAD`

build:
	docker build --no-cache --tag caja-application --build-arg REVISION=$(REVISION) .

dev-build:
	docker build --tag caja-application --build-arg REVISION=$(REVISION) .
