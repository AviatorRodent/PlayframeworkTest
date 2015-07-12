# News Items schema

# --- !Ups

CREATE TABLE newsitem (
    title       STRING,
    description STRING,
    link        STRING,
    pubDate     STRING,
    category    STRING,
    enclosure   STRING
);

# --- !Downs

DROP TABLE newsitems;