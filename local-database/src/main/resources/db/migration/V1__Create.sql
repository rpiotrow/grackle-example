CREATE TABLE country
(
    code          VARCHAR NOT NULL PRIMARY KEY,
    name          VARCHAR NOT NULL,
    currency_code VARCHAR NOT NULL
);

COPY country (code, name, currency_code) FROM stdin DELIMITER ',' CSV;
PL,Poland,PLN
US,United States,USD
CAN,Canada,CAD
GB,United Kingdom,GBP
AUS,Australia,AUD
JP,Japan,JPY
\.
