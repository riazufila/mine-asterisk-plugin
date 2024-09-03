DROP DATABASE IF EXISTS `mine-asterisk-test`;

CREATE DATABASE `mine-asterisk-test`;

USE `mine-asterisk-test`;

CREATE TABLE player (
    id INT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE `unique_uuid` (`uuid`)
);

CREATE TABLE permission (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE `unique_name` (`name`)
);

CREATE TABLE access (
    player_id INT NOT NULL,
    permission_id INT NOT NULL,
    CONSTRAINT `foreign_key_access_player_player_id` FOREIGN KEY (player_id)
        REFERENCES player (id),
    CONSTRAINT `foreign_key_access_permission_permission_id` FOREIGN KEY (permission_id)
        REFERENCES permission (id),
    UNIQUE `unique_access` (`player_id`, `permission_id`)
);

INSERT INTO permission (`id`, `name`) VALUES
(1, "team.leader"),
(2, "team.member");

DELIMITER //
CREATE PROCEDURE reset()
BEGIN
	DELETE FROM access;
	DELETE FROM player;
	DELETE FROM permission;

	INSERT INTO permission (`id`, `name`) VALUES
	(1, "team.leader"),
	(2, "team.member");
END //
DELIMITER ;