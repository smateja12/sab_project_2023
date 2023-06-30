USE sab_proj_23_db
GO

CREATE VIEW ShortestPaths AS
WITH RoutesBetweenCities(StartCity, CurrentCity, DistanceBetween, CurrentRoute) AS (
	SELECT IDCity, IDCity, 0, cast(IDCity as nvarchar(100))
	FROM City
						
	UNION ALL
						
	SELECT StartCity, City2, DistanceBetween + L.Distance,
	CAST(DistanceBetween + '|' + CAST(City2 as nvarchar(10)) AS nvarchar(100))
	FROM Line L JOIN RoutesBetweenCities ON (City1 = CurrentCity)
	WHERE DistanceBetween NOT LIKE '%' + CAST(City2 AS varchar(10)) + '%'
)
select RBC1.StartCity, RBC1.CurrentCity as FinalCity, RBC1.DistanceBetween, RBC1.CurrentRoute
from RoutesBetweenCities RBC1
where RBC1.DistanceBetween = (
	select min(RBC2.DistanceBetween)
	from RoutesBetweenCities RBC2
	where RBC1.CurrentCity = RBC2.CurrentCity and RBC1.StartCity = RBC2.StartCity
)
