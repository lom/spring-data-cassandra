
CREATE TABLE IF NOT EXISTS post (
    id timeuuid,
    title text,
    body_text text,
    type ascii,
    crypto_value blob,
    crypto boolean,
    crypto_string text,

    PRIMARY KEY (id)
);

INSERT INTO post (body_text,crypto,crypto_string,crypto_value,id,title,type) VALUES ('body',true,'Q0MxBwBrM1x/kdV10ZS5/FObJph5U9SsXKfPjxoYBbMtAAk/mY5h',0x43433107006b335bd1916d47725f8f54af42c20751b6c5ef2284c82cb1d4f1654724a34a8eb560,27b8b880-bb29-11e7-9467-eb5f74bc6fde,'title','TYPE1');

INSERT INTO post (body_text,crypto,crypto_string,crypto_value,id,title,type) VALUES ('body2',false,'Crypto String 2',0x03f5d83a,27b8b880-bb29-11e7-9467-eb5f74bc6fdd,'title2','TYPE2');
