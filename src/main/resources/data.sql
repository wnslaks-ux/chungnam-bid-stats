MERGE INTO region (region_code, region_name, sort_order) KEY(region_code) VALUES
('YESAN','예산군',1),('HONGSEONG','홍성군',2),('SEOSAN','서산시',3),('TAEAN','태안군',4),
('BUYEO','부여군',5),('NONSAN','논산시',6),('CHEONGYANG','청양군',7);
INSERT INTO search_preset (preset_name, region_code, region_name, date_from, date_to, min_price, max_price, keyword)
SELECT '예산군 기본(최근 3개월)','YESAN','예산군',DATEADD('MONTH',-3,CURRENT_DATE),CURRENT_DATE,NULL,NULL,NULL
WHERE NOT EXISTS (SELECT 1 FROM search_preset);
