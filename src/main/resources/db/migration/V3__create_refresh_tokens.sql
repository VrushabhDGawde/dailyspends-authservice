CREATE TABLE refresh_tokens (

    id UUID PRIMARY KEY,

    token VARCHAR(500) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL UNIQUE,

    revoked BOOLEAN NOT NULL,

    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);