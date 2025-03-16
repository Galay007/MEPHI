--Заданиче 1
SELECT  	v.maker,
			m.model
FROM   		transport.motorcycle m
JOIN    	transport.vehicle v ON m.model = v.model
WHERE   	horsepower > 150
			AND price < 20000
			AND m.TYPE ILIKE '%sport%'
ORDER BY 	horsepower DESC;
    
--Задание 2
SELECT 		maker
			,v.model
			,COALESCE(c.horsepower,m.horsepower) AS horsepower
			,COALESCE(c.engine_capacity,m.engine_capacity) AS engine_capacity
			,v.type AS vehicle_type 
FROM 		transport.vehicle v
LEFT JOIN 	transport.car c ON c.model = v.model
LEFT JOIN 	transport.motorcycle m ON m.model = v.model
LEFT JOIN 	transport.bicycle b ON b.model = v.model
WHERE 		1 = 1
			AND c.horsepower > 150
			AND c.engine_capacity < 3
			AND c.price < 35000
			OR m.horsepower > 150
			AND m.engine_capacity < 1.5
			AND m.price < 20000
			OR b.gear_count > 18
			AND b.price < 4000
ORDER BY 	horsepower DESC NULLS LAST ;


SELECT * FROM vehicle   