INSERT INTO CUSTOMER (id, name, surname, credit_limit, used_credit_limit) VALUES (1, 'ADMIN', 'ADMIN', 100000, 0);
INSERT INTO CUSTOMER (id, name, surname, credit_limit, used_credit_limit) VALUES (2, 'CUSTOMER', 'CUSTOMER', 100000, 0);

INSERT INTO USER_INFO (id, username, password, role, customer_id) VALUES (1, 'admin', '$2a$12$UUT2vmALX1BZYoOP8ijGm.j2Psn7fDgs2X47bea/IxYP4acMO4KWG', 'ROLE_ADMIN', 1);
INSERT INTO USER_INFO (id, username, password, role, customer_id) VALUES (2, 'customer', '$2a$12$LPMMsTRzOfm1BI9P5wECA.vp/dv.hkrmgv.3oj2VR6So7YZ0vZf22', 'ROLE_CUSTOMER', 2);

COMMIT;