DROP SCHEMA `go_nature`;
CREATE DATABASE `go_nature` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

use go_nature;

CREATE TABLE `guides` (
	`visitor_id` varchar(9) NOT NULL,
	PRIMARY KEY (`visitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `parks` (
  `parkName` varchar(255) NOT NULL,
  `capacity` int DEFAULT NULL,
  `diff` int DEFAULT NULL,
  `visitTimeInMinutes` int DEFAULT NULL,
  `currentVisitors` int DEFAULT NULL,
  `newCapacity` int DEFAULT NULL,
  `newDiff` int DEFAULT NULL,
  `newVisitTimeInMinutes` int DEFAULT NULL,

  PRIMARY KEY (`parkName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
  `username` varchar(80) NOT NULL,
  `password` varchar(80) DEFAULT NULL,
  `type` varchar(80) DEFAULT NULL,
  `parkName` varchar(255) DEFAULT NULL,
  `workerId` varchar(255) DEFAULT NULL,
  `firstName` varchar(90) DEFAULT NULL,
  `lastName` varchar(90) DEFAULT NULL,
  `email` varchar(90) DEFAULT NULL,
  PRIMARY KEY (`username`),
  KEY `fk_parkName` (`parkName`),
  CONSTRAINT `fk_parkName` FOREIGN KEY (`parkName`) REFERENCES `parks` (`parkName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orders` (
	`orderId` int NOT NULL AUTO_INCREMENT,
	`visitor_id` varchar(9) DEFAULT NULL,
	`parkName` varchar(90) DEFAULT NULL,
	`time_of_visit` datetime DEFAULT NULL,
	`visitor_number` int DEFAULT NULL,
	`visitor_email` varchar(90) DEFAULT NULL,
	`visitor_phone` varchar(90) DEFAULT NULL,
	`status` ENUM('Active', 'Cancelled', 'WaitList', 'Entered') DEFAULT NULL,
    `paid` BOOLEAN DEFAULT NULL,
	`reminderMsgSend` BOOLEAN DEFAULT NULL,
	`visitorConfirmedOrder` BOOLEAN DEFAULT NULL,
	PRIMARY KEY (`orderId`),
	KEY `parkName` (`parkName`),
	CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`parkName`) REFERENCES `parks` (`parkName`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



CREATE TABLE `visits` (
	`visitId` INT NOT NULL AUTO_INCREMENT,
    `visitor_id` VARCHAR(9) DEFAULT NULL,
    `parkName` varchar(90) DEFAULT NULL,
    `timeOfEntrence` DATETIME DEFAULT NULL,
    `timeOfExit` DATETIME DEFAULT NULL,
    `numberOfVisitors` INT DEFAULT NULL,
    `isGroup` BOOLEAN,
    PRIMARY KEY(`visitId`),
    KEY `parkName` (`parkName`),
	CONSTRAINT `visits_ibfk_1` FOREIGN KEY (`parkName`) REFERENCES `parks` (`parkName`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `numberOfVisitorsReport` (
	`month` ENUM('1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'),
    `year` VARCHAR(4) NOT NULL,
    `parkName` VARCHAR(90) NOT NULL,
    `amountOfNonGroup` INT DEFAULT NULL,
    `amountOfGroup` INT DEFAULT NULL,
    PRIMARY KEY (`month`, `year`, `parkName`),
    KEY `parkName` (`parkName`),
	CONSTRAINT `numReport_ibfk_1` FOREIGN KEY (`parkName`) REFERENCES `parks` (`parkName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `externaluserinfo` (
  `username` varchar(80) NOT NULL,
  `password` varchar(80) DEFAULT NULL,
  `type` varchar(80) DEFAULT NULL,
  `parkName` varchar(255) DEFAULT NULL,
  `workerId` varchar(255) DEFAULT NULL,
  `firstName` varchar(90) DEFAULT NULL,
  `lastName` varchar(90) DEFAULT NULL,
  `email` varchar(90) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO `parks` (`parkName`, `capacity`, `diff`, `visitTimeInMinutes`, `currentVisitors`, `newCapacity`, `newDiff`, `newVisitTimeInMinutes`) VALUES
('Richmond Park', 100, 5, 120, 0, 100, 5, 120),
('Hyde Park', 150, 6, 130, 0, 150, 6, 130),
('Greenwich Park', 200, 7, 0, 90, 200, 7, 140);

INSERT INTO `externaluserinfo` (`username`, `password`, `type`, `parkName`, `workerId`, `firstName`, `lastName`, `email`) VALUES
('John', '123456', 'ParkWorker', 'Richmond Park', 'W123', 'John', 'Doe', 'john@example.com'),
('Jane', '123456', 'ParkWorker', 'Hyde Park', 'W124', 'Jane', 'Smith', 'jane@example.com'),
('Mike', '123456', 'ParkWorker', 'Greenwich Park', 'W125', 'Mike', 'Johnson', 'mike@example.com'),
('David', '123456', 'DepartmentWorker', 'Richmond Park', 'DW123', 'David', 'Lee', 'david@example.com'),
('Emily', '123456', 'DepartmentManager', 'Hyde Park', 'DM124', 'Emily', 'Taylor', 'emily@example.com'),
('Michael', '123456', 'ParkManager', 'Richmond Park', 'PM123', 'Michael', 'Brown', 'michael@example.com'),
('Jessica', '123456', 'ParkManager', 'Hyde Park', 'PM124', 'Jessica', 'Wilson', 'jessica@example.com'),
('Kevin', '123456', 'ParkManager', 'Greenwich Park', 'PM125', 'Kevin', 'Anderson', 'kevin@example.com');


-- Order for 29.03.24
INSERT INTO orders (visitor_id, parkName, time_of_visit, visitor_number, visitor_email, visitor_phone, status, paid, reminderMsgSend, visitorConfirmedOrder)
VALUES ('337526789', 'Richmond Park', '2024-03-29 10:00:00', 2, 'visitor1@example.com', '123456789', 'Active', true, false, true);

-- Order for 30.03.24
INSERT INTO orders (visitor_id, parkName, time_of_visit, visitor_number, visitor_email, visitor_phone, status, paid, reminderMsgSend, visitorConfirmedOrder)
VALUES ('337546789', 'Hyde Park', '2024-03-30 11:30:00', 3, 'visitor2@example.com', '987654321', 'WaitList', false, false, false);

-- Order for 31.03.24
INSERT INTO orders (visitor_id, parkName, time_of_visit, visitor_number, visitor_email, visitor_phone, status, paid, reminderMsgSend, visitorConfirmedOrder)
VALUES ('397526789', 'Greenwich Park', '2024-03-31 14:00:00', 4, 'visitor3@example.com', '456123789', 'Cancelled', false, false, false);

-- Order for 01.04.24
INSERT INTO orders (visitor_id, parkName, time_of_visit, visitor_number, visitor_email, visitor_phone, status, paid, reminderMsgSend, visitorConfirmedOrder)
VALUES ('397526989', 'Richmond Park', '2024-04-01 15:30:00', 5, 'visitor4@example.com', '789654123', 'Entered', true, true, true);

-- Order for 02.04.24
INSERT INTO orders (visitor_id, parkName, time_of_visit, visitor_number, visitor_email, visitor_phone, status, paid, reminderMsgSend, visitorConfirmedOrder)
VALUES ('391526989', 'Hyde Park', '2024-04-02 09:00:00', 2, 'visitor5@example.com', '321987654', 'Active', true, false, true);

-- Visit corresponding to the order for 29.03.24
INSERT INTO visits (visitor_id, parkName, timeOfEntrence, timeOfExit, numberOfVisitors, isGroup)
VALUES ('337526789', 'Richmond Park', '2024-03-29 10:00:00', '2024-03-29 13:00:00', 2, false);

-- Visit corresponding to the order for 30.03.24
INSERT INTO visits (visitor_id, parkName, timeOfEntrence, timeOfExit, numberOfVisitors, isGroup)
VALUES ('337546789', 'Hyde Park', '2024-03-30 11:30:00', '2024-03-30 15:00:00', 3, false);

-- Visit corresponding to the order for 01.04.24
INSERT INTO visits (visitor_id, parkName, timeOfEntrence, timeOfExit, numberOfVisitors, isGroup)
VALUES ('397526989', 'Richmond Park', '2024-04-01 15:30:00', '2024-04-01 17:30:00', 5, false);

-- Visit corresponding to the order for 02.04.24
INSERT INTO visits (visitor_id, parkName, timeOfEntrence, timeOfExit, numberOfVisitors, isGroup)
VALUES ('391526989', 'Hyde Park', '2024-04-02 09:00:00', '2024-04-02 12:00:00', 2, false);
