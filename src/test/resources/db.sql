
CREATE TABLE IF NOT EXISTS post (
    id timeuuid,
    title text,
    body_text text,
    type ascii,

    PRIMARY KEY (id)
);
