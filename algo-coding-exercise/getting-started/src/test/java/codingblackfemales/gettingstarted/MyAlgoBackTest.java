package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.marketdata.*;
import messages.order.Side;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test plugs together all of the infrastructure, including the order book (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data coming back from the order book.
 *
 * If you cross the srpead (i.e. you BUY an order with a price which is == or > askPrice()) you will match, and receive
 * a fill back into your order from the order book (visible from the algo in the childOrders of the state object.
 *
 * If you cancel the order your child order will show the order status as cancelled in the childOrders of the state object.
 *
 */
public class MyAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    protected UnsafeBuffer createMyAlgoTick() {

        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.LME);
        encoder.instrumentId(123L);

        encoder.bidBookCount(3)
                .next().price(98L).size(100L)
                .next().price(95L).size(200L)
                .next().price(91L).size(300L);


        encoder.askBookCount(3)
                .next().price(100L).size(101L)
                .next().price(110L).size(200L)
                .next().price(115L).size(5000L);


        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    protected UnsafeBuffer createMyAlgoTick2() {

        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.LME);
        encoder.instrumentId(123L);

        encoder.bidBookCount(3)
                .next().price(95L).size(100L)
                .next().price(93L).size(200L)
                .next().price(91L).size(300L);


        encoder.askBookCount(4)
                .next().price(98L).size(501L)
                .next().price(101L).size(200L)
                .next().price(110L).size(5000L)
                .next().price(119L).size(5600L);


        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    protected UnsafeBuffer createMyAlgoTick3() {

        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        encoder.venue(Venue.LME);
        encoder.instrumentId(123L);

        encoder.bidBookCount(3)
                .next().price(101L).size(100L)
                .next().price(96L).size(200L)
                .next().price(95L).size(300L);

        encoder.askBookCount(3)
                .next().price(104L).size(101L)
                .next().price(109L).size(200L)
                .next().price(119L).size(5600L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    @Test
    public void testExampleBackTest() throws Exception {

        // Tick 1: buy rests at 98
        send(createMyAlgoTick());
        assertEquals(4, container.getState().getChildOrders().size());
        assertEquals(4, container.getState().getActiveChildOrders().size());

        // Tick 2: ask at 98 fills our buy
        send(createMyAlgoTick2());
        long filledQuantity = container.getState().getChildOrders().stream()
                .mapToLong(ChildOrder::getFilledQuantity)
                .sum();
        assertEquals(220L, filledQuantity);
        assertEquals(4, container.getState().getActiveChildOrders().size());

        // Tick 3: best bid 101 is above our buy price of 98, so we sell
        send(createMyAlgoTick3());
        long sellOrders = container.getState().getChildOrders().stream()
                .filter(order -> order.getSide() == Side.SELL)
                .count();
        assertTrue(sellOrders >= 1);
    }
}

//    @Test
//    public void testExampleBackTest() throws Exception {
//        //create a sample market data tick....
//        send(createMyAlgoTick());
//
//
//        //ADD asserts when you have implemented your algo logic
////        assertEquals(1, container.getState().getActiveChildOrders().size());
//
//        //when: market data moves towards us
//        send(createMyAlgoTick2());
//        //then: get the state
////        var state = container.getState();
////        System.out.println(state);
////        assertEquals(3, state.getActiveChildOrders().size());
////        assertEquals(6, state.getChildOrders().size());
//
//        //Check things like filled quantity, cancelled order count etc....
////        long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
////        and: check that our algo state was updated to reflect our fills when the market data
////        assertEquals(225, filledQuantity);
//        send(createMyAlgoTick3());
//
//    }
//
//
//
//}
