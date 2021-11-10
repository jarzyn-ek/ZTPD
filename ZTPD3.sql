--1.
CREATE TABLE DOKUMENTY(
ID           NUMBER(12) PRIMARY KEY,
DOKUMENT CLOB);

--2.
DECLARE
   LOBD CLOB;
   COUNTER INTEGER;
BEGIN
   INSERT INTO DOKUMENTY(ID, DOKUMENT)
   VALUES(1, EMPTY_CLOB());
   
   SELECT DOKUMENT INTO LOBD FROM DOKUMENTY
   WHERE ID=1 FOR UPDATE;
   
   FOR COUNTER IN 1..1000 LOOP
       DBMS_LOB.APPEND(LOBD, 'Oto tekst.');
   end loop;
   
   COMMIT;
   
END;

--3.
--A)
SELECT * FROM DOKUMENTY;
--B)
SELECT UPPER(DOKUMENT) FROM DOKUMENTY;
--C)
SELECT LENGTH(DOKUMENT) FROM DOKUMENTY;
--D)
SELECT DBMS_LOB.GETLENGTH(DOKUMENT) FROM DOKUMENTY;
--E)
SELECT SUBSTR(DOKUMENT, 5, 1000) FROM DOKUMENTY;
--F)
SELECT DBMS_LOB.SUBSTR(DOKUMENT, 1000, 5) FROM DOKUMENTY;

--4.
INSERT INTO DOKUMENTY(ID, DOKUMENT) VALUES(2, EMPTY_CLOB());

--5.
INSERT INTO DOKUMENTY(ID, DOKUMENT) VALUES(3, NULL);
COMMIT;

--6.
SELECT * FROM DOKUMENTY;
SELECT UPPER(DOKUMENT) FROM DOKUMENTY;
SELECT LENGTH(DOKUMENT) FROM DOKUMENTY;
SELECT DBMS_LOB.GETLENGTH(DOKUMENT) FROM DOKUMENTY;
SELECT SUBSTR(DOKUMENT, 5, 1000) FROM DOKUMENTY;
SELECT DBMS_LOB.SUBSTR(DOKUMENT, 1000, 5) FROM DOKUMENTY;

--7.
SELECT * FROM ALL_DIRECTORIES;
--'ZSBD_DIR'

--8.
DECLARE
   lobd clob;
   fil BFILE := BFILENAME('ZSBD_DIR','dokument.txt');
   doffset integer := 1;
   soffset integer := 1;
   langctx integer := 0;
   warn integer := null;
BEGIN
   SELECT DOKUMENT INTO lobd
   FROM DOKUMENTY WHERE ID = 2
   FOR UPDATE;
   dbms_lob.fileopen(fil);
   DBMS_LOB.LOADCLOBFROMFILE(lobd, fil, DBMS_LOB.LOBMAXSIZE,doffset,soffset,873,langctx,warn);
   dbms_lob.fileclose(fil);
   COMMIT;
END;

--9.
UPDATE DOKUMENTY SET DOKUMENT = TO_CLOB(BFILENAME('ZSBD_DIR','dokument.txt'), 873, 'text/xml') where ID=3;

--10.
SELECT * FROM DOKUMENTY;

--11.
SELECT DBMS_LOB.GETLENGTH(DOKUMENT) FROM DOKUMENTY;

--12.
DROP TABLE DOKUMENTY;
COMMIT;

--13.
CREATE OR REPLACE PROCEDURE CLOB_CENSOR (LOBD IN OUT CLOB, TEXT_TO_BE_REPLACED VARCHAR2) 
IS
    IDX INTEGER;
    TEXT_TO_REPLACE VARCHAR2(15);
    COUNTER INTEGER;
BEGIN
    FOR COUNTER IN 1..LENGTH(TEXT_TO_BE_REPLACED) LOOP
       TEXT_TO_REPLACE := TEXT_TO_REPLACE || '.';
    END LOOP;
    
    LOOP
        IDX := DBMS_LOB.INSTR(LOBD, TEXT_TO_BE_REPLACED, 1, 1);
        EXIT WHEN IDX = 0;
        DBMS_LOB.WRITE(LOBD, LENGTH(TEXT_TO_BE_REPLACED), IDX, TEXT_TO_REPLACE);
    END LOOP;
END CLOB_CENSOR;

--14.
CREATE TABLE KOPIA AS SELECT * FROM ZSBD_TOOLS.BIOGRAPHIES;

DECLARE
    LOBD CLOB;
BEGIN
    SELECT BIO INTO LOBD FROM KOPIA WHERE PERSON = 'Jara Cimrman' FOR UPDATE;
    
    CLOB_CENSOR(LOBD, 'Cimrman');
    COMMIT;
END;

--15.
DROP TABLE KOPIA;
