#!/bin/bash
export SPRING_DATASOURCE_URL=jdbc:h2:file:./data/taskdb;DB_CLOSE_DELAY=-1
export SPRING_DATASOURCE_USERNAME=sa
export SPRING_DATASOURCE_PASSWORD=
export SPRING_JPA_HIBERNATE_DDL_AUTO=update

mvn spring-boot:run