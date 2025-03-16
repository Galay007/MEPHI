CREATE TABLE car_racing.Classes (
	class VARCHAR(100) NOT NULL,
	type VARCHAR(20) NOT NULL CHECK (type IN ('Racing', 'Street')), -- тип класса
	country VARCHAR(100) NOT NULL,
	numDoors INT NOT NULL,
	engineSize DECIMAL(3, 1) NOT NULL, -- размер двигателя в литрах
	weight INT NOT NULL,            	-- вес автомобиля в килограммах
	PRIMARY KEY (class)
);
 
-- Создание таблицы Cars
CREATE TABLE car_racing.Cars (
	name VARCHAR(100) NOT NULL,
	class VARCHAR(100) NOT NULL,
	year INT NOT NULL,
	PRIMARY KEY (name),
	FOREIGN KEY (class) REFERENCES car_racing.Classes(class)
);
 
-- Создание таблицы Races
CREATE TABLE car_racing.Races (
	name VARCHAR(100) NOT NULL,
	date DATE NOT NULL,
	PRIMARY KEY (name)
);
 
-- Создание таблицы Results
CREATE TABLE car_racing.Results (
	car VARCHAR(100) NOT NULL,
	race VARCHAR(100) NOT NULL,
	position INT NOT NULL,
	PRIMARY KEY (car, race),
	FOREIGN KEY (car) REFERENCES car_racing.Cars(name),
	FOREIGN KEY (race) REFERENCES car_racing.Races(name)
);
