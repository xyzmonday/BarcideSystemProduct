create table IF not exists T_EXTRA_HEADER
(
  id               VARCHAR2(32) PRIMARY KEY NOT NULL,
  config_type      TEXT
);

create table IF not exists T_EXTRA_LINE
(
   id               VARCHAR2(32) PRIMARY KEY NOT NULL,
   ref_code_id      VARCHAR2(32),
   config_type      TEXT

);

create table IF not exists T_EXTRA_CW
(
 id               VARCHAR2(32) PRIMARY KEY NOT NULL,
 ref_line_id      VARCHAR2(32),
 config_type      TEXT
);

create table IF not exists T_TRANSACTION_EXTRA_HEADER
(
   id               VARCHAR2(32) PRIMARY KEY NOT NULL,
   config_type      TEXT
);


create table IF not exists T_TRANSACTION_EXTRA_LINE
(
   id               VARCHAR2(32) PRIMARY KEY NOT NULL,
   trans_line_id    VARCHAR2(32),
   config_type      TEXT
);

create table IF not exists T_TRANSACTION_EXTRA_CW
(
   id               VARCHAR2(32) PRIMARY KEY NOT NULL,
   trans_loc_id     VARCHAR2(32),
   config_type      TEXT
);

create table BASE_MATERIAL_BATCH
(
  id               VARCHAR2(32) PRIMARY KEY not null,
  material_id      VARCHAR2(32),
  work_id          VARCHAR2(32),
  batch_flag       VARCHAR2(32),
  status           VARCHAR2(10),
  created_by       VARCHAR2(32),
  creation_date    DATE,
  last_updated_by  VARCHAR2(32),
  last_update_date DATE
);

create table MTL_IMAGES
(
  id            VARCHAR2(32) PRIMARY KEY NOT NULL,
  ref_num       TEXT,
  ref_code_id   VARCHAR2(32),
  ref_line_id   VARCHAR2(32),
  ins_id        VARCHAR2(32),
  ins_line_id   VARCHAR2(32),
  image_dir     TEXT,
  image_name    TEXT,
  created_by    TEXT,
  local_flag    TEXT,
  biz_type      TEXT,
  ref_type      TEXT,
  take_photo_type    INTEGER,
  creation_date TEXT
);


create table IF not exists REQUEST_DATE
(
  id          VARCHAR2(32) PRIMARY KEY,
  query_type  TEXT,
  query_date  TEXT
);

create table BASE_COST_CENTER
(
  id                VARCHAR2(32) PRIMARY KEY NOT NULL,
  org_id            VARCHAR2(32),
  cost_center_code  TEXT,
  cost_center_desc  TEXT,
  start_date        DATE,
  end_date          DATE
);


create table BASE_UNIT_CODE
(
  id                VARCHAR2(32) PRIMARY KEY NOT NULL,
  unit              VARCHAR2(30),
  long_desc         VARCHAR2(120)
);


create table BASE_PROJECT_NUM
(
  id                VARCHAR2(32) PRIMARY KEY NOT NULL,
  org_id            VARCHAR2(32),
  project_num_code  TEXT,
  project_num_desc  TEXT
);


create table  BASE_INSPECTION_PLACE
(
  id               VARCHAR2(32) PRIMARY KEY NOT NULL,
  code             TEXT,
  name             TEXT
);

create table IF not exists BASE_MATERIAL_CODE
(
  id                VARCHAR2(32) PRIMARY KEY NOT NULL,
  material_num      TEXT,
  material_desc     TEXT,
  material_group    TEXT,
  unit              TEXT,
  status            TEXT,
  old_material_num  TEXT,
  material_type     TEXT
);


create table IF not exists BASE_SUPPLIER
(
  id               VARCHAR2(32)  PRIMARY KEY NOT NULL,
  org_id           VARCHAR2(32),
  supplier_code    TEXT,
  supplier_desc    TEXT
);

create table IF not exists BASE_WAREHOUSE_GROUP
(
  id         VARCHAR2(32) PRIMARY KEY NOT null,
  group_code TEXT,
  group_desc TEXT
);

create table IF not exists BASE_LOCATION
(
  id                VARCHAR2(32) PRIMARY KEY NOT NULL,
  location          TEXT,
  storage_num       TEXT,
  work_id           TEXT,
  inv_id            TEXT
);

create table IF not exists P_AUTH_ORG
(
  org_id       VARCHAR2(32) PRIMARY KEY  NOT NULL,
  org_name     TEXT,
  org_code     TEXT,
  parent_id    VARCHAR2(32),
  order_no     INTEGER,
  phone_number TEXT,
  post_address TEXT,
  memo         TEXT,
  create_date  TEXT,
  creator      TEXT,
  modify_date  TEXT,
  modifier     TEXT,
  org_level    TEXT,
  lock_flag    TEXT,
  storage_code TEXT,
  storage_name TEXT,
  evaluation   TEXT
);

create table IF not exists P_AUTH_ORG2
(
  org_id       VARCHAR2(32) PRIMARY KEY  NOT NULL,
  org_name     TEXT,
  org_code     TEXT,
  parent_id    VARCHAR2(32),
  order_no     INTEGER,
  phone_number TEXT,
  post_address TEXT,
  memo         TEXT,
  create_date  TEXT,
  creator      TEXT,
  modify_date  TEXT,
  modifier     TEXT,
  org_level    TEXT,
  lock_flag    TEXT,
  storage_code TEXT,
  storage_name TEXT,
  evaluation   TEXT
);

create table IF not exists T_USER
(
  user_id            VARCHAR2(32) PRIMARY KEY NOT NULL,
  login_id           TEXT,
  last_login_date    INTEGER,
  user_name          TEXT,
  company_id         TEXT,
  company_code       TEXT,
  auth_orgs          TEXT,
  batch_flag         TEXT
);

create table IF not exists T_CONFIG
(
    id               VARCHAR2(32) PRIMARY KEY NOT NULL,
    property_name    TEXT,
    property_code    TEXT,
    display_flag     TEXT,
    input_flag       TEXT,
    company_id       TEXT,
    biz_type         TEXT,
    ref_type         TEXT,
    config_type      TEXT,
    ui_type          TEXT,
    col_num          TEXT,
    col_name         TEXT,
    data_source      TEXT
);

create table IF not exists T_EXTRA_DATA_SOURCE
(
    id               VARCHAR2(32) PRIMARY KEY NOT NULL,
    code             TEXT,
    name             TEXT,
    sort             INTEGER,
    val              TEXT
);

create table IF not exists T_FRAGMENT_CONFIGS
(
    id               VARCHAR2(32) PRIMARY KEY NOT NULL,
    fragment_tag     TEXT,
    biz_type         TEXT,
    ref_type         TEXT,
    tab_title        TEXT,
    mode             INTEGER,
    fragment_type    INTEGER,
    class_name       TEXT
);

create table IF not exists T_HOME_MENUS
(
    id               VARCHAR2(32) PRIMARY KEY NOT NULL,
    parent_id        VARCHAR2(32),
    biz_type         TEXT,
    ref_type         TEXT,
    caption          TEXT,
    functionCode     TEXT,
    login_id         TEXT,
    mode             INTEGER,
    tree_level       INTEGER
);

create table MTL_CHECK_HEADER
(
  id               VARCHAR2(32) PRIMARY KEY NOT NULL,
  storage_num      VARCHAR2(10),
  work_id          VARCHAR2(32),
  inv_id           VARCHAR2(32),
  check_special    VARCHAR2(2),
  check_num        VARCHAR2(32),
  sap_check_num    VARCHAR2(20),
  check_type       VARCHAR2(5),
  created_by       VARCHAR2(32),
  creation_date    INTEGER,
  last_updated_by  VARCHAR2(32),
  last_update_date INTEGER,
  upload_by        VARCHAR2(32),
  upload_date      INTEGER,
  check_level      VARCHAR2(2),
  biz_type         TEXT,
  trans_flag       VARCHAR2(2)
);

create table MTL_CHECK_LINES
(
  id               VARCHAR2(32) PRIMARY KEY NOT NULL,
  work_id          VARCHAR2(32),
  inv_id           VARCHAR2(32),
  special_flag     VARCHAR2(5),
  check_id         VARCHAR2(32),
  line_num         NUMBER(5),
  material_id      VARCHAR2(32),
  location         VARCHAR2(10),
  inv_type         VARCHAR2(5),
  special_num      VARCHAR2(20),
  batch_num        VARCHAR2(20),
  quantity         NUMBER(13,3),
  new_flag         VARCHAR2(5),
  created_by       VARCHAR2(32),
  creation_date    INTEGER,
  last_updated_by  VARCHAR2(32),
  last_update_date INTEGER,
  inv_quantity     NUMBER(13,3)
);

create table MTL_INSPECTION_HEADERS
(
  id                     VARCHAR2(32)  PRIMARY KEY NOT NULL,
  inspection_num         VARCHAR2(30),
  inspection_date        TEXT,
  po_id                  VARCHAR2(32),
  ref_code               TEXT,
  ins_flag               VARCHAR2(10),
  workflow_id            VARCHAR2(32),
  workflow_level         VARCHAR2(32),
  approval_flag          VARCHAR2(10),
  sap_gen_num            VARCHAR2(32),
  edit_flag              VARCHAR2(10),
  print_flag             VARCHAR2(10),
  print_date             TEXT,
  status                 VARCHAR2(10),
  created_by             VARCHAR2(32),
  creation_date          INTEGER,
  last_updated_by        VARCHAR2(32),
  last_update_date       INTEGER,
  inspection_type        VARCHAR2(10),
  inspection_instruction VARCHAR2(2000),
  ref_opinion            VARCHAR2(2000),
  arrival_date           TEXT,
  quality_file           VARCHAR2(2000),
  inspection_result      VARCHAR2(2000),
  system_flag            VARCHAR2(32)
);

create table MTL_INSPECTION_LINES
(
  id                     VARCHAR2(32) PRIMARY KEY NOT NULL,
  inspection_id          VARCHAR2(32),
  po_line_id             VARCHAR2(32),
  line_num               NUMBER(5),
  work_id                VARCHAR2(32),
  inv_id                 VARCHAR2(32),
  arrival_date           TEXT,
  material_id            VARCHAR2(32),
  unit                   VARCHAR2(32),
  unit_rate              NUMBER(13,3),
  price_rate             NUMBER(13,3),
  rec_quantity           NUMBER(13,3),
  quantity               NUMBER(13,3),
  qualified_quantity     NUMBER(13,3),
  inspection_person      VARCHAR2(32),
  inspection_date        TEXT,
  inspection_instruction VARCHAR2(2000),
  ref_opinion            VARCHAR2(2000),
  process_result         VARCHAR2(2000),
  status                 VARCHAR2(10),
  remark                 VARCHAR2(2000),
  created_by             VARCHAR2(32),
  creation_date          INTEGER,
  last_updated_by        VARCHAR2(32),
  last_update_date       INTEGER,
  quality_count          VARCHAR2(32),
  other_count            VARCHAR2(32),
  inspection_result      VARCHAR2(10)
);

create table MTL_INSPECTION_LINES_CUSTOM
(
  id                  VARCHAR2(32) PRIMARY KEY NOT NULL,
  inspection_id       VARCHAR2(32),
  inspection_line_id  VARCHAR2(32),
  random_quantity     NUMBER(13,3),
  rust_quantity       NUMBER(13,3),
  damaged_quantity    NUMBER(13,3),
  bad_quantity        NUMBER(13,3),
  other_quantity      NUMBER(13,3),
  z_package           VARCHAR2(2),
  qm_num              VARCHAR2(120),
  certificate         VARCHAR2(1),
  instructions        VARCHAR2(1),
  qm_certificate      VARCHAR2(1),
  claim_num           VARCHAR2(50),
  manufacturer        VARCHAR2(120),
  inspection_quantity NUMBER(13,3)
);

create table IF not exists MTL_PO_HEADERS
(
  id               VARCHAR2(32) PRIMARY KEY NOT NULL,
  po_num           VARCHAR2(32),
  po_date          TEXT,
  purchase_mode    VARCHAR2(32),
  contract_num     VARCHAR2(32),
  contract_name    VARCHAR2(200),
  supplier_code    VARCHAR2(32),
  supplier_desc    VARCHAR2(200),
  po_org           VARCHAR2(4),
  po_org_desc      VARCHAR2(200),
  po_group_en      VARCHAR2(32),
  po_group_cn      VARCHAR2(128),
  po_creator       VARCHAR2(32),
  zd_flag          VARCHAR2(32),
  doc_people       VARCHAR2(32),
  doc_people_name  VARCHAR2(300),
  doc_date         TEXT,
  contract_amount  NUMBER(13,3),
  type             VARCHAR2(1),
  status           VARCHAR2(10),
  created_by       VARCHAR2(32),
  creation_date    TEXT,
  last_updated_by  VARCHAR2(32),
  last_update_date TEXT,
  work_id          VARCHAR2(32),
  po_type          VARCHAR2(32)
);

create table IF not exists MTL_PO_LINES
(
  id                   VARCHAR2(32) PRIMARY KEY NOT NULL,
  po_id                VARCHAR2(32),
  po_line_id           VARCHAR2(32),
  line_num             TEXT,
  work_id              VARCHAR2(32),
  inv_id               VARCHAR2(32),
  biz_type             TEXT,
  ref_type             TEXT,
  material_id          VARCHAR2(32),
  material_num         VARCHAR2(18),
  material_desc        VARCHAR2(200),
  material_group       VARCHAR2(10),
  ref_doc              VARCHAR2(10),
  ref_doc_item         NUMBER(6),
  ins_lot              TEXT,
  ins_lot_quantity     TEXT,
  qualified_quantity   TEXT,
  unqualified_quantity TEXT,
  return_quantity      TEXT,
  order_quantity       TEXT,
  act_quantity         TEXT,
  material_unit        TEXT,
  record_unit          TEXT,
  unit_rate            REAL,
  qm_flag              VARCHAR2(10),
  unit                 VARCHAR2(32),
  status               VARCHAR2(10),
  created_by           VARCHAR2(32),
  creation_date        TEXT,
  last_updated_by      VARCHAR2(32),
  last_update_date     TEXT,
  send_inv_id          VARCHAR2(32),
  send_work_id         VARCHAR2(32),
  send_act_quantity    TEXT,
  act_quantity_103     TEXT,
  act_quantity_105     TEXT,
  line_num_105         TEXT,
  is_return            VARCHAR2(1),
  line_type            VARCHAR2(10)
);

create table IF not exists MTL_RESERVATION_LINES
(
  id               VARCHAR2(32) PRIMARY KEY NOT NULL,
  reservation_id   VARCHAR2(32),
  work_id          VARCHAR2(32),
  inv_id           VARCHAR2(32),
  line_num         NUMBER(4),
  material_id      VARCHAR2(32),
  shkzg            VARCHAR2(5),
  batch_num        VARCHAR2(32),
  order_quantity   NUMBER(13,3),
  act_quantity     NUMBER(13,3),
  delete_flag      VARCHAR2(1),
  movement_flag    VARCHAR2(1),
  last_flag        VARCHAR2(1),
  special_flag     VARCHAR2(1),
  unit             VARCHAR2(10),
  unit_rate        NUMBER(13,3),
  flag             VARCHAR2(4),
  status           VARCHAR2(10),
  created_by       VARCHAR2(32),
  creation_date    TEXT,
  last_updated_by  VARCHAR2(32),
  last_update_date TEXT,
  reservation_num  NUMBER(10),
  parent_material  VARCHAR2(32),
  sap_move_type    VARCHAR2(3),
  po_line_id       VARCHAR2(32),
  po_num           VARCHAR2(10),
  po_line_num      NUMBER(5),
  po_id            VARCHAR2(32)
);

create table IF not exists MTL_TRANSACTION_HEADERS
(
  id               VARCHAR2(32) PRIMARY KEY NOT NULL,
  trans_num        VARCHAR2(32),
  voucher_date     TEXT,
  ref_code_id      VARCHAR2(32),
  ref_type         VARCHAR2(5),
  trans_flag       VARCHAR2(5),
  workflow_id      VARCHAR2(32),
  supplier_id      VARCHAR2(32),
  workflow_level   VARCHAR2(32),
  approval_flag    VARCHAR2(10),
  mv_gen_num       VARCHAR2(50),
  move_type        VARCHAR2(10),
  biz_type         VARCHAR2(10),
  inv_type         VARCHAR2(10),
  comp_flag        VARCHAR2(1),
  trans_type       VARCHAR2(10),
  rec_org          VARCHAR2(32),
  rec_people       VARCHAR2(32),
  device_unit      VARCHAR2(300),
  system_flag      VARCHAR2(32),
  remark           VARCHAR2(500),
  created_by       VARCHAR2(32),
  creation_date    INTEGER,
  last_updated_by  VARCHAR2(32),
  last_update_date INTEGER,
  ref_code         VARCHAR2(20),
  supplier_code    VARCHAR2(20)
);



create table IF not exists  MTL_TRANSACTION_LINES
(
  id                  VARCHAR2(32)  PRIMARY KEY NOT NULL,
  trans_id            VARCHAR2(32),
  ref_line_id         VARCHAR2(32),
  ref_line_num        NUMBER(6),
  line_num            INTEGER,
  work_id             VARCHAR2(32),
  inv_id              VARCHAR2(32),
  rec_work_id         VARCHAR2(32),
  rec_inv_id          VARCHAR2(32),
  material_id         VARCHAR2(32),
  new_material_id     VARCHAR2(32),
  quantity            NUMBER(13,3),
  order_unit_quantity NUMBER(13,3),
  act_quantity        NUMBER(13,3),
  unit                TEXT,
  unit_rate           REAL,
  device_location     VARCHAR2(300),
  equi_location       VARCHAR2(300),
  detail_location     VARCHAR2(300),
  amount              NUMBER(13,2),
  ref_doc             VARCHAR2(10),
  ref_doc_item        NUMBER(5),
  return_quantity     TEXT,
  move_cause          VARCHAR2(32),
  move_cause_desc     VARCHAR2(1000),
  decision_code       VARCHAR2(32),
  project_text        VARCHAR2(1000),
  ins_lot             VARCHAR2(32),
  created_by          VARCHAR2(32),
  creation_date       INTEGER,
  last_updated_by     VARCHAR2(32),
  last_update_date    INTEGER
);

create table IF not exists MTL_TRANSACTION_LINES_SPLIT
(
  id                  VARCHAR2(32) PRIMARY KEY NOT NULL,
  trans_id            VARCHAR2(32),
  trans_line_id       VARCHAR2(32),
  ref_line_id         VARCHAR2(32),
  ref_line_num        NUMBER(6),
  line_num            NUMBER(5),
  work_id             VARCHAR2(32),
  inv_id              VARCHAR2(32),
  inv_type            VARCHAR2(32),
  special_flag        VARCHAR2(5),
  special_num         VARCHAR2(30),
  rec_work_id         VARCHAR2(32),
  rec_inv_id          VARCHAR2(32),
  rec_inv_type        VARCHAR2(32),
  rec_special_flag    VARCHAR2(32),
  rec_special_num     VARCHAR2(30),
  material_id         VARCHAR2(32),
  new_material_id     VARCHAR2(32),
  quantity            NUMBER(13,3),
  order_unit_quantity NUMBER(13,3),
  special_convert     TEXT,
  act_quantity        NUMBER(13,3),
  unit                VARCHAR2(32),
  unit_rate           NUMBER(13,3),
  amount              NUMBER(13,2),
  batch_num           VARCHAR2(32),
  rec_batch_num       VARCHAR2(32),
  ref_doc             VARCHAR2(10),
  ref_doc_item        NUMBER(5),
  return_quantity     NUMBER(13,3),
  move_cause          VARCHAR2(32),
  move_cause_desc     VARCHAR2(1000),
  decision_code       VARCHAR2(32),
  project_text        VARCHAR2(1000),
  ins_lot             VARCHAR2(32),
  created_by          VARCHAR2(32),
  creation_date       INTEGER,
  last_updated_by     VARCHAR2(32),
  last_update_date    INTEGER
);

create table IF not exists MTL_TRANSACTION_LINES_LOCATION
(
   id                  VARCHAR2(32) PRIMARY KEY NOT NULL,
   trans_id            VARCHAR2(32),
   trans_line_id       VARCHAR2(32),
   trans_line_split_id VARCHAR2(32),
   location            VARCHAR2(32),
   rec_location        VARCHAR2(32),
   quantity            NUMBER(13,3),
   order_quantity      NUMBER(13,3),
   rec_quantity        NUMBER(13,3),
   rec_order_quantity  NUMBER(13,3),
   device_id           VARCHAR2(32),
   rec_device_id       VARCHAR2(32),
   created_by          VARCHAR2(32),
   creation_date       INTEGER,
   last_updated_by     VARCHAR2(32),
   last_update_date    INTEGER
);