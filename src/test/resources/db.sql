
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

CREATE TABLE IF NOT EXISTS comments (
    post_id timeuuid,
    comment_id timeuuid,

    body_text text,
    field_blob blob,
    field_map map<text, uuid>,
    field_set set<text>,
    fielt_list list<int>,
    field_inet inet,
    field_timestamp timestamp,
    field_double double,
    PRIMARY KEY (post_id,comment_id)
);

INSERT INTO comments (post_id, comment_id , body_text , field_blob , field_map , field_set , fielt_list , field_inet , field_double , field_timestamp ) VALUES ( now(), now(), 'body text', 0xaabbcc11, {'a':27b8b880-bb29-11e7-9467-eb5f74bc6fde}, {'c','d','e'}, [1,2,3], '192.168.0.1', 1.2, '2015-05-03 13:30:54' );