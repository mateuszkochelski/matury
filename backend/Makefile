DB_NAME=postgres
DB_USER=myuser
DB_PASSWORD=mypass
DB_PORT=5432
be-run:
	./gradlew bootRun

db-run:
	docker start postgres

db-stop:
	docker stop postgres

db-drop:
	docker stop postgres && docker rm postgres

db-create:
	docker run -d \
		--name postgres \
		-e POSTGRES_DB=$(DB_NAME) \
		-e POSTGRES_USER=$(DB_USER) \
		-e POSTGRES_PASSWORD=$(DB_PASSWORD) \
		-p $(DB_PORT):5432 \
		postgres:15

db-logs:
	docker logs -f postgres
