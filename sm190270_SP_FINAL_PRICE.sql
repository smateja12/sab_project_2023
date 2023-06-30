use sab_proj_23_db
go

CREATE PROCEDURE SP_FINAL_PRICE
	@IDOrder int,
	@OrderDate DATE, 
	@FinalPrice decimal(10, 3)
AS
BEGIN

	-- broj narudzbina u poslednjih 30 dana za kupca

	declare @BuyersOrdersCnt int

	select @BuyersOrdersCnt = count(*)
	from [Order] O join Buyer B on (O.IDBuyer = B.IDBuyer)
	where DATEDIFF(DAY, O.SentTime, @OrderDate) < 30

	declare @BuyersOrdersMultiplier decimal(10, 3)

	-- sistemski popust of 2%
	set @BuyersOrdersMultiplier = case
	when @BuyersOrdersCnt > 0 then 0.98 else 1.0 end

	if (
		select O.Price
		from [Order] O
		where O.IDOrder = @IDOrder
	) IS NOT NULL
	begin
		-- price je vec sracunat

		select @FinalPrice = O.Price
		from [Order] O
		where O.IDOrder = @IDOrder

	end	
	else 
	begin
		
		-- price treba da se sracuna

		declare @BuyersOrdersPercentage decimal(10, 3)

		set @BuyersOrdersPercentage = (1 - (@BuyersOrdersMultiplier / 100))

		select @FinalPrice = sum(OI.OrderQuantity * A.Price * @BuyersOrdersPercentage)
		from Shop S join Article A on (S.IDShop = A.IDShop)
		join OrderItems OI on (OI.IDArticle = A.IDArticle)
		where OI.IDOrder = @IDOrder

		update [Order]
		set Price = @FinalPrice
		where IDOrder = @IDOrder

	end


END
GO
