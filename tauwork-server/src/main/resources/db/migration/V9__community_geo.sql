-- 已有社区补上坐标，供小程序按距离排序；封面地址加长，与商品主图一致
UPDATE community SET lat = 22.363100, lng = 114.131000 WHERE id = 9001 AND lat IS NULL;
UPDATE community SET lat = 22.541000, lng = 114.055000 WHERE id = 9002 AND lat IS NULL;
UPDATE community SET lat = 22.604000, lng = 113.846000 WHERE id = 9003 AND lat IS NULL;

ALTER TABLE community
    MODIFY COLUMN cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面图';
