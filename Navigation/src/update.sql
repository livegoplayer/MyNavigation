UPDATE `hotspot` AS u1
INNER JOIN (
SELECT id
FROM `hotspot`
) AS u2 ON u1.热点ID = u2.热点ID SET u1.热点名 = u2.热点ID
UPDATE `crossroad` AS u1
INNER JOIN (
SELECT ID
FROM `crossroad`
) AS u2 ON u1.ID = u2.ID SET u1.name = u2.ID
UPDATE `road_polines` AS u1
INNER JOIN (
SELECT 路段ID
FROM `road_polines`
) AS u2 ON u1.路段ID = u2.路段ID SET u1.name = u2.路段ID;