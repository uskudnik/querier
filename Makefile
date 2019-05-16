up:
	@docker-compose up -d

ssh-querier:
	@docker exec -it querier_querier_1 bash

run-tests:
	@docker exec -it querier_querier_1 lein test

