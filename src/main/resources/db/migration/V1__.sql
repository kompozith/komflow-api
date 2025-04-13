CREATE SEQUENCE IF NOT EXISTS seq_id START WITH 1 INCREMENT BY 50;

CREATE TABLE cnt_contact_tags
(
    cnt_contact_id BIGINT NOT NULL,
    cnt_tag_id     BIGINT NOT NULL
);

CREATE TABLE cnt_contats
(
    id              BIGINT       NOT NULL,
    email           VARCHAR(255),
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    phone_number    VARCHAR(255),
    whatsapp_number VARCHAR(255),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_cnt_contats PRIMARY KEY (id)
);

CREATE TABLE cnt_tags
(
    id          BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    color_code  VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_cnt_tags PRIMARY KEY (id)
);

CREATE TABLE gen_files
(
    id   BIGINT       NOT NULL,
    name VARCHAR(255) NOT NULL,
    url  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_gen_files PRIMARY KEY (id)
);

CREATE TABLE msg_email_cc
(
    cnt_contact_id BIGINT NOT NULL,
    msg_message_id BIGINT NOT NULL
);

CREATE TABLE msg_email_cci
(
    cnt_contact_id BIGINT NOT NULL,
    msg_message_id BIGINT NOT NULL
);

CREATE TABLE msg_message_attachments
(
    gen_file_id    BIGINT NOT NULL,
    msg_message_id BIGINT NOT NULL
);

CREATE TABLE msg_messages
(
    id               BIGINT NOT NULL,
    object           VARCHAR(255),
    cnt_contact_from BIGINT,
    cnt_contact_to   BIGINT,
    preview          VARCHAR(255),
    content          TEXT   NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_msg_messages PRIMARY KEY (id)
);

ALTER TABLE msg_message_attachments
    ADD CONSTRAINT uc_msg_message_attachments_gen_file UNIQUE (gen_file_id);

ALTER TABLE msg_messages
    ADD CONSTRAINT FK_MSG_MESSAGES_ON_CNT_CONTACT_FROM FOREIGN KEY (cnt_contact_from) REFERENCES cnt_contats (id);

ALTER TABLE msg_messages
    ADD CONSTRAINT FK_MSG_MESSAGES_ON_CNT_CONTACT_TO FOREIGN KEY (cnt_contact_to) REFERENCES cnt_contats (id);

ALTER TABLE cnt_contact_tags
    ADD CONSTRAINT fk_cntcontag_on_contact FOREIGN KEY (cnt_contact_id) REFERENCES cnt_contats (id);

ALTER TABLE cnt_contact_tags
    ADD CONSTRAINT fk_cntcontag_on_tag FOREIGN KEY (cnt_tag_id) REFERENCES cnt_tags (id);

ALTER TABLE msg_email_cc
    ADD CONSTRAINT fk_msgemacc_on_contact FOREIGN KEY (cnt_contact_id) REFERENCES cnt_contats (id);

ALTER TABLE msg_email_cc
    ADD CONSTRAINT fk_msgemacc_on_message FOREIGN KEY (msg_message_id) REFERENCES msg_messages (id);

ALTER TABLE msg_email_cci
    ADD CONSTRAINT fk_msgemacci_on_contact FOREIGN KEY (cnt_contact_id) REFERENCES cnt_contats (id);

ALTER TABLE msg_email_cci
    ADD CONSTRAINT fk_msgemacci_on_message FOREIGN KEY (msg_message_id) REFERENCES msg_messages (id);

ALTER TABLE msg_message_attachments
    ADD CONSTRAINT fk_msgmesatt_on_file FOREIGN KEY (gen_file_id) REFERENCES gen_files (id);

ALTER TABLE msg_message_attachments
    ADD CONSTRAINT fk_msgmesatt_on_message FOREIGN KEY (msg_message_id) REFERENCES msg_messages (id);