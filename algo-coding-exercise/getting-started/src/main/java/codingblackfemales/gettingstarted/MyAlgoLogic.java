package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        var allChildOrder = state.getChildOrders().size();
        if (allChildOrder > 4) {
            return NoAction.NoAction;
        }

        final var option = state.getActiveChildOrders().stream().findFirst();
        if (option.isPresent()) {
            var activeOrder = option.get();
            logger.info("[MYALGO] Cancelling order:" + activeOrder);
            return new CancelChildOrder(activeOrder);
        }


        //Counts how many buy order got filled
        final var filledBuy = state.getChildOrders().stream().filter(order ->
                order.getSide() == Side.BUY && order.getFilledQuantity() > 0).count();
        //Counts how many sell others got filled
        final var filledSell = state.getChildOrders().stream().filter(order ->
                order.getSide() == Side.SELL && order.getFilledQuantity() > 0).count();
//      Checks if orders have more filledBuy than filledSell
        if (filledBuy - filledSell >  0) {
            // variable shows the first buy order that has not been sold
            var filledBuyNotSold = state.getChildOrders().stream().filter(order ->
                    order.getSide() == Side.BUY && order.getFilledQuantity() > 0).findFirst();
            if (filledBuyNotSold.isPresent()) {
                final var bestBid = state.getBidAt(0);
                final var childOrder = filledBuyNotSold.get();
                if (childOrder.getPrice() < bestBid.price) {
                    logger.info("[MYALGO] Bought at " + childOrder.getPrice() + ", sell now at " + bestBid.price );
                    return new CreateChildOrder(Side.SELL, childOrder.getQuantity(), bestBid.price);
                } else {
                    return NoAction.NoAction;
                }
            } else {
                return NoAction.NoAction;
            }

        } else {
            BidLevel level = state.getBidAt(0);
            final long price = level.price;
            final long quantity = 55;
            logger.info("[MYALGO] Adding order for" + quantity + "@" + price);
            return new CreateChildOrder(Side.BUY, quantity, price);

        }






    }
}
