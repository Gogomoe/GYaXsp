DROP TABLE IF EXISTS problem,example;

CREATE TABLE IF NOT EXISTS problem
(
    problem_name VARCHAR(255),
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edit_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (problem_name)
);

CREATE TABLE IF NOT EXISTS example
(
    example_id   INT          NOT NULL AUTO_INCREMENT,
    problem_name VARCHAR(255) NOT NULL,
    username     VARCHAR(255) NOT NULL,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edit_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (example_id),
    FOREIGN KEY (problem_name) REFERENCES problem (problem_name),
    FOREIGN KEY (username) REFERENCES user (username)
);