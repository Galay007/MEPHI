--SELECT * FROM organization.Departments
--SELECT * FROM organization.Roles
--SELECT * FROM organization.Employees
--SELECT * FROM organization.Projects
--SELECT * FROM organization.Tasks

--Task1
WITH RECURSIVE employee_hierarchy AS (
SELECT 		e.employeeid, e.name, e.managerid, e.departmentid, e.roleid
FROM 		organization.employees e
WHERE 		e.employeeid = 1  -- Иван Иванов
UNION ALL
SELECT 		e.employeeid, e.name, e.managerid, e.departmentid, e.roleid
FROM 		organization.employees e
INNER JOIN 	employee_hierarchy eh ON e.managerid = eh.employeeid
)

SELECT		eh.employeeid
    		,eh.name AS employeeName
    		,eh.managerid
    		,d.departmentName
    		,r.roleName
    		,p.projectName AS projectsNames
    		,string_agg(DISTINCT t.taskName, ', ') AS tasksNames
FROM		employee_hierarchy eh
LEFT JOIN 	organization.departments d ON eh.departmentid = d.departmentid
LEFT JOIN 	organization.roles r ON eh.roleid = r.roleid
LEFT JOIN 	organization.projects p ON p.departmentid = eh.departmentid
LEFT JOIN 	organization.tasks t ON eh.employeeid = t.assignedto
GROUP BY 	1,2,3,4,5,6
ORDER BY    2;

--Task2
WITH RECURSIVE employee_hierarchy AS (
SELECT 		e.employeeid, e.name, e.managerid, e.departmentid, e.roleid
FROM 		organization.employees e
WHERE 		e.employeeid = 1  -- Иван Иванов
UNION ALL
SELECT 		e.employeeid, e.name, e.managerid, e.departmentid, e.roleid
FROM 		organization.employees e
INNER JOIN 	employee_hierarchy eh ON e.managerid = eh.employeeid
)
SELECT		eh.employeeid
    		,eh.name AS employeeName
    		,eh.managerid
    		,d.departmentName
    		,r.roleName
    		,p.projectName AS projectsNames
    		,string_agg(DISTINCT t.taskName, ', ') AS tasksNames
    		,COUNT(DISTINCT t.taskid) AS TotalTasks
    		,COUNT(DISTINCT sub.employeeid) AS TotalSubordinates
FROM		employee_hierarchy eh
LEFT JOIN 	organization.departments d ON eh.departmentid = d.departmentid
LEFT JOIN 	organization.roles r ON eh.roleid = r.roleid
LEFT JOIN 	organization.projects p ON p.departmentid = eh.departmentid
LEFT JOIN 	organization.tasks t ON eh.employeeid = t.assignedto
LEFT JOIN 	organization.employees sub ON eh.employeeid = sub.managerid
GROUP BY 	1,2,3,4,5,6
ORDER BY    2;

--Task3
WITH RECURSIVE manager_hierarchy AS (
SELECT 		e.employeeid, e.name, e.managerid, e.departmentid, e.roleid
FROM 		organization.employees e
INNER JOIN 	organization.roles r ON e.RoleID = r.RoleID
WHERE 		r.rolename='Менеджер'
UNION ALL
SELECT 		e.employeeid, e.name, e.managerid, e.departmentid, e.roleid
FROM 		organization.employees e
INNER JOIN 	manager_hierarchy mh ON e.managerid = mh.employeeid
)
SELECT		mh.employeeid
    		,mh.name AS employeeName
    		,mh.managerid
    		,d.departmentName
    		,r.roleName
    		,p.projectName AS projectsNames
    		,string_agg(DISTINCT t.taskName, ', ') AS tasksNames
    		,COUNT(DISTINCT sub.employeeid) AS TotalSubordinates
FROM		manager_hierarchy mh
LEFT JOIN 	organization.departments d ON mh.departmentid = d.departmentid
LEFT JOIN 	organization.roles r ON mh.roleid = r.roleid
LEFT JOIN 	organization.projects p ON p.departmentid = mh.departmentid
LEFT JOIN 	organization.tasks t ON mh.employeeid = t.assignedto
LEFT JOIN 	organization.employees sub ON mh.employeeid = sub.managerid
GROUP BY 	1,2,3,4,5,6
HAVING 		COUNT(DISTINCT sub.employeeid) > 0
ORDER BY    2;