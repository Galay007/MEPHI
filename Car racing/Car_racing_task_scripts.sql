--Task1
SELECT 		car_name,car_class,average_position,race_count 
FROM (
SELECT 		r.car AS car_name
			,c.class AS car_class
			,AVG(r.position) AS average_position
			,COUNT(DISTINCT r.race) AS race_count
			,ROW_NUMBER () OVER (PARTITION BY c.class ORDER BY AVG(r.position)) AS row_
FROM 		car_racing.results r
JOIN		car_racing.cars c ON c.name = r.car
GROUP BY 	1,2)
WHERE 		row_ = 1
ORDER BY 	average_position;

--Task2
SELECT 		r.car AS car_name
			,c.class AS car_class
			,AVG(r.position) AS average_position
			,COUNT(DISTINCT r.race) AS race_count
			,cl.country AS car_country
FROM 		car_racing.results r
JOIN		car_racing.cars c ON c.name = r.car
JOIN 		car_racing.classes cl ON cl.CLASS = c.class
GROUP BY 	1,2,5
HAVING 		AVG(r.position) = (SELECT min(POSITION) FROM car_racing.results)
ORDER BY 	1
LIMIT 		1;

--Task3
SELECT 		r.car AS car_name
			,c.class AS car_class
			,AVG(r.position) AS average_position
			,COUNT(DISTINCT r.race) AS race_count
			,cl.country AS car_country
			,COUNT(DISTINCT r.race) OVER (PARTITION BY r.race) AS total_races
FROM 		car_racing.results r
JOIN		car_racing.cars c ON c.name = r.car
JOIN 		car_racing.classes cl ON cl.CLASS = c.class
GROUP BY 	1,2,5,r.race
HAVING 		AVG(r.position) = (SELECT min(POSITION) FROM car_racing.results)
ORDER BY 	1;

--Task4
SELECT 		car_name,car_class,average_position,race_count,car_country
FROM 		(
SELECT 		r.car AS car_name
			,c.class AS car_class
			,AVG(r.position) AS average_position
			,COUNT(DISTINCT r.race) AS race_count
			,cl.country AS car_country
			,AVG(r.position) OVER (PARTITION BY c.class ) AS avg_
FROM 		car_racing.results r
JOIN		car_racing.cars c ON c.name = r.car
JOIN 		car_racing.classes cl ON cl.CLASS = c.class
GROUP BY 	1,2,5,r.POSITION)
WHERE 		average_position < avg_
ORDER BY 	2,3;

--Task5
SELECT 		*
FROM 		(
SELECT 		r.car AS car_name
			,c.class AS car_class
			,AVG(r.position) AS average_position
			,COUNT(DISTINCT r.race) AS race_count
			,cl.country AS car_country
			,COUNT(r.race) OVER (PARTITION BY c.class) AS total_races
			,COUNT(r.car) OVER (PARTITION BY c.class) AS low_position_count
FROM 		car_racing.results r
JOIN		car_racing.cars c ON c.name = r.car
JOIN 		car_racing.classes cl ON cl.CLASS = c.class
GROUP BY 	1,2,5,r.race
ORDER BY 	7)
WHERE 		average_position > 3
ORDER BY 	low_position_count DESC;
