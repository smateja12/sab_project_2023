
CREATE TRIGGER TR_TRANSFER_MONEY_TO_SHOPS
   ON  [Order]
   AFTER UPDATE
AS 
BEGIN
	
	declare @kursorOrder cursor
	declare @IDOrder int
	declare @SentTime DATE
	declare @ReceivedTime DATE
	declare @FlagSystemDiscount int

	set @kursorOrder = cursor for
	select I.IDOrder, I.SentTime, I.ReceivedTime, I.DiscountPercentageSystem
	from inserted I
	where I.Status = 'arrived'

	open @kursorOrder

	fetch next from @kursorOrder
	into @IDOrder, @SentTime, @ReceivedTime, @FlagSystemDiscount

	while @@FETCH_STATUS = 0
	begin
		
		-- sracunati da li treba da se mnozi za 0.97 ili sa 0.95
		-- dodati nove transakcije za shop

		declare @kursorShop cursor

		set @kursorShop = cursor for
		select A.IDShop, coalesce((
			sum((1 - (OI.DiscountPercentage / 100)) * OI.Price * OI.OrderQuantity)
		), 0)
		from OrderItems OI join Article A on (OI.IDArticle = A.IDArticle)
		where OI.IDOrder = @IDOrder
		group by A.IDShop

		declare @IDShop int
		declare @TransactionQuantity decimal(10, 3)

		open @kursorShop

		fetch next from @kursorShop
		into @IDShop, @TransactionQuantity

		while @@FETCH_STATUS = 0
		begin
			
			-- ako treba da se obradi sistemski popust, tada mnozimo @TransactionQuantity sa 0.97
			-- ako NE treba da se obradi sistemski popust, tada mnozimo @TransactionQuantity sa 0.95 

			set @TransactionQuantity = @TransactionQuantity * (
				case when @FlagSystemDiscount = 1 then 0.97
				else 0.95 
				end
			)

			-- dodati novu transakciju
			insert into [Transaction](Quantity, ExecutionTime, IDOrder)
			values (@TransactionQuantity, @ReceivedTime, @IDOrder)

			declare @IDTransaction int
			
			select @IDTransaction = max(T.IDTransaction)
			from [Transaction] T
 			
			insert into ShopTransaction(IDTransaction, IDShop)
			values (@IDTransaction, @IDShop)

			fetch next from @kursorShop
			into @IDShop, @TransactionQuantity
		end

		close @kursorShop
		deallocate @kursorShop

		fetch next from @kursorOrder
		into @IDOrder, @SentTime, @ReceivedTime, @FlagSystemDiscount
	end

	close @kursorOrder
	deallocate @kursorOrder

END
GO
