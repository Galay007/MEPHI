--Task1		
SELECT 		c.name
			,c.email
			,c.phone
			,count(b.id_booking) AS total_bookings 
			,string_agg(DISTINCT h.name,', ') AS unique_hotels
			,AVG(check_out_date - check_in_date) AS avg_days
FROM 		bookings.booking b
JOIN 		bookings.room r ON r.id_room = b.id_room 
JOIN 		bookings.customer c ON c.id_customer = b.id_customer 
JOIN 		bookings.hotel h ON h.id_hotel = r.id_hotel
GROUP BY 	1,2,3
HAVING 		count(b.id_booking) > 2 AND count(DISTINCT h.id_hotel) > 1
ORDER BY 	1

--Task2			
SELECT 		c.id_customer 
			,c.name
			,count(b.id_booking) AS total_bookings
			,sum(r.price) * sum(check_out_date - check_in_date) AS total_spent
			,count(DISTINCT h.name) unique_hotels
FROM 		bookings.booking b
JOIN 		bookings.room r ON r.id_room = b.id_room 
JOIN 		bookings.customer c ON c.id_customer = b.id_customer 
JOIN 		bookings.hotel h ON h.id_hotel = r.id_hotel
GROUP BY 	1,2
HAVING 		count(b.id_booking) > 2 
			AND count(DISTINCT h.id_hotel) > 1
			AND (sum(r.price) * sum(check_out_date - check_in_date) ) > 500
ORDER BY 	total_spent;

--Task3
WITH price_category AS (
SELECT 		h.id_hotel
			,h.name
			,CASE WHEN AVG(r.price) < 175 THEN 1
				  WHEN AVG(r.price) BETWEEN 175 AND 300 THEN 2
				  ELSE 3
			END AS price_cat
FROM		bookings.booking b
JOIN 		bookings.room r ON r.id_room = b.id_room 
JOIN 		bookings.hotel h ON h.id_hotel = r.id_hotel
GROUP BY 	h.id_hotel,h.name
)
SELECT		b.id_customer 
			,c.name
			,CASE WHEN MAX(p.price_cat)=3 THEN 'Дорогой'
				  WHEN MAX(p.price_cat)=2 THEN 'Средний'
			ELSE 'Дешевый'
			END preferred_hotel_type
			,string_agg(DISTINCT h.name,', ') AS unique_hotels
FROM		bookings.booking b
JOIN 		bookings.room r ON r.id_room = b.id_room 
JOIN 		bookings.customer c ON c.id_customer = b.id_customer 
JOIN 		bookings.hotel h ON h.id_hotel = r.id_hotel
JOIN 		price_category p ON p.id_hotel = h.id_hotel
GROUP BY 	1,2
ORDER BY 	(CASE WHEN MAX(p.price_cat)=3 THEN 3
				  WHEN MAX(p.price_cat)=2 THEN 2
			ELSE 1 
			END),1;
