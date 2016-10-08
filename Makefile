REVISION=`git rev-parse HEAD`

.PHONY: image dev-image up_db up_app volumes networks

image:
	docker build --no-cache --tag caja-application:$(REVISION) .

dev-image:
	docker build --tag caja-application:$(REVISION) .

up_db:
	docker-compose up -d caja-database

up_app:
	docker-compose up -d caja-application

volumes:
	@docker volume create --name neeco_caja || true

networks:
	@docker network create neeco_caja || true
	@docker network create neeco_caja-cuenta || true
