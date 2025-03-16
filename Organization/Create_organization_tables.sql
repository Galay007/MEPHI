CREATE TABLE organization.Departments (
    DepartmentID SERIAL PRIMARY KEY,  -- Используем SERIAL для автоматической генерации идентификаторов
    DepartmentName VARCHAR(100) NOT NULL
);

CREATE TABLE organization.Roles (
    RoleID SERIAL PRIMARY KEY,  -- Используем SERIAL для автоматической генерации идентификаторов
    RoleName VARCHAR(100) NOT NULL
);

CREATE TABLE organization.Employees (
    EmployeeID SERIAL PRIMARY KEY,  -- Используем SERIAL для автоматической генерации идентификаторов
    Name VARCHAR(100) NOT NULL,
    Position VARCHAR(100),
    ManagerID INT,
    DepartmentID INT,
    RoleID INT,
    FOREIGN KEY (ManagerID) REFERENCES organization.Employees(EmployeeID) ON DELETE SET NULL,  -- Устанавливаем поведение при удалении
    FOREIGN KEY (DepartmentID) REFERENCES organization.Departments(DepartmentID) ON DELETE CASCADE,  -- Устанавливаем поведение при удалении
    FOREIGN KEY (RoleID) REFERENCES organization.Roles(RoleID) ON DELETE SET NULL  -- Устанавливаем поведение при удалении
);

CREATE TABLE organization.Projects (
    ProjectID SERIAL PRIMARY KEY,  -- Используем SERIAL для автоматической генерации идентификаторов
    ProjectName VARCHAR(100) NOT NULL,
    StartDate DATE,
    EndDate DATE,
    DepartmentID INT,
    FOREIGN KEY (DepartmentID) REFERENCES organization.Departments(DepartmentID) ON DELETE CASCADE  -- Устанавливаем поведение при удалении
);

CREATE TABLE organization.Tasks (
    TaskID SERIAL PRIMARY KEY,  -- Используем SERIAL для автоматической генерации идентификаторов
    TaskName VARCHAR(100) NOT NULL,
    AssignedTo INT,
    ProjectID INT,
    FOREIGN KEY (AssignedTo) REFERENCES organization.Employees(EmployeeID) ON DELETE SET NULL,  -- Устанавливаем поведение при удалении
    FOREIGN KEY (ProjectID) REFERENCES organization.Projects(ProjectID) ON DELETE CASCADE  -- Устанавливаем поведение при удалении
);