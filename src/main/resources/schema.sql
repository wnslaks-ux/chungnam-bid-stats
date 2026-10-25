CREATE TABLE IF NOT EXISTS region (
  region_code VARCHAR(10) PRIMARY KEY,
  region_name VARCHAR(50) NOT NULL,
  sort_order INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS search_preset (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  preset_name VARCHAR(100) NOT NULL,
  region_code VARCHAR(10),
  region_name VARCHAR(50),
  date_from DATE,
  date_to DATE,
  min_price BIGINT,
  max_price BIGINT,
  keyword VARCHAR(200),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS bid_cache (
  bid_ntce_no VARCHAR(40) PRIMARY KEY,
  bid_ntce_nm VARCHAR(500),
  dminstt_nm VARCHAR(200),
  bid_ntce_dt VARCHAR(30),
  presmpt_prce BIGINT,
  bid_ntce_url VARCHAR(1000),
  cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS admin_user (
  username VARCHAR(50) PRIMARY KEY,
  password_hash VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS app_config (
  config_key VARCHAR(100) PRIMARY KEY,
  config_value VARCHAR(2000),
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
