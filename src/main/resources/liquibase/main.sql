--liquibase formatted sql

--changeset tony:create-account
create TABLE ACCOUNT (
  ID BIGSERIAL,
  NUMBER VARCHAR(20) NOT NULL UNIQUE ,
  CREATE_DATE TIMESTAMP,
  BALANCE NUMERIC,
  ACC_NAME VARCHAR(50)
);
