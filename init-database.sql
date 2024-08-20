DROP DATABASE IF EXISTS `mine-asterisk`;
CREATE DATABASE `mine-asterisk`;

USE `mine-asterisk`;

CREATE TABLE entity (
    id INT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE permission (
    id INT NOT NULL,
    value VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE access (
    entity_id INT NOT NULL,
    permission_id INT NOT NULL,
    CONSTRAINT `foreign_key_access_entity_entity_id` FOREIGN KEY (entity_id)
        REFERENCES entity (id),
    CONSTRAINT `foreign_key_access_permission_permission_id` FOREIGN KEY (permission_id)
        REFERENCES permission (id),
    UNIQUE `unique_access` (`entity_id` , `permission_id`)
);

INSERT INTO permission VALUES
(1, "team.owner"),
(2, "team.member");
