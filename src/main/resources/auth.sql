DROP TABLE IF EXISTS user,user_roles,roles_perms,user_info;

CREATE TABLE IF NOT EXISTS user
(
    username      VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,

    PRIMARY KEY (username)
);

CREATE TABLE IF NOT EXISTS user_info
(
    username VARCHAR(255) NOT NULL,
    avatar   VARCHAR(255),

    FOREIGN KEY (username) REFERENCES user (username)
);

CREATE TABLE IF NOT EXISTS roles_perms
(
    role VARCHAR(255) NOT NULL,
    perm VARCHAR(255) NOT NULL,

    PRIMARY KEY (role, perm)
);

CREATE TABLE IF NOT EXISTS user_roles
(
    username VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,

    PRIMARY KEY (username, role),
    FOREIGN KEY (username) REFERENCES user (username)
);

INSERT INTO gyaxsp.user
VALUES ('admin',
        '684DFB2F3D9BF7426CFD86B4F3A5969E15DABD4D9CA7DCA5C9037BB4F23D17A17FDAC723AF44CE6944264396E104AFB987479CF26F3AC6EA6EC07E368628FEC3',
        'E5403A42B9D490BA17EEB80700894401106E754BEFB8ECCCF2DB9F1034FBC4A1');

INSERT INTO gyaxsp.roles_perms
VALUES ('admin', 'admin');

INSERT INTO gyaxsp.user_roles
VALUES ('admin', 'admin');

INSERT INTO gyaxsp.user_info
VALUES ('admin', null);
