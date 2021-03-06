CREATE CACHED TABLE EVENT_HISTORY(
    ID VARCHAR(36) PRIMARY KEY,
    NAME VARCHAR(255),
    VALUE VARCHAR(255),
    DESCRIPTION_TEXT VARCHAR(255),
    DISPLAYED BOOLEAN,
    DISPLAY_NAME VARCHAR(255),
    IS_STATE_CHANGE BOOLEAN,
    UNIT VARCHAR(50),
    DATA CLOB(10K),
    DATE TIMESTAMP,
    SOURCE VARCHAR(10),
    SOURCE_ID VARCHAR(36),
    IS_DIGITAL BOOLEAN
);

CREATE INDEX EVENT_HISTORY_SOURCEID_SOURCE_NAME_DATE_INDEX ON EVENT_HISTORY (SOURCE_ID, SOURCE, NAME, DATE);
