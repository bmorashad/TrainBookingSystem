import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class PassengerQueueTest {

    private PassengerQueue p;

    @org.junit.Test
    public void testEnqueue() throws Exception {
        p = new PassengerQueue(4);
        p.enqueue(new Passenger("Rashad", 3));
        boolean result = p.isEmpty();
        assertEquals(result, p.isEmpty());
    }

    @Test
    public void testGetSize() throws Exception {
        p = new PassengerQueue(3);
        p.enqueue(new Passenger("Sara", 4));
        p.enqueue(new Passenger("Sara", 4));
        int result = 2;
        assertEquals(result, p.getSize());
    }

    @Test(expected = Exception.class)
    public void testEnqueueException() throws Exception {
        p = new PassengerQueue(2);
        p.enqueue(new Passenger("Fara", 2));
        p.enqueue(new Passenger("Fara", 2));
        p.enqueue(new Passenger("Fara", 2));
    }

    @org.junit.Test
    public void testDequeue() throws Exception{
        p = new PassengerQueue(2);
        p.enqueue(new Passenger("Ammar", 1));
        p.dequeue();
        assertEquals(true, p.isEmpty());
    }

    @org.junit.Test
    public void testDeleteAvailable() throws Exception{
        p = new PassengerQueue(3);
        p.enqueue(new Passenger("Ammar", 1));
        p.enqueue(new Passenger("Tharindu", 2));
        p.enqueue(new Passenger("Yasitha", 3));
        Passenger deleted = p.delete("Tharindu", 2);
        assertNotEquals(null, deleted);
        assertEquals(2, p.getSize());
    }

    @org.junit.Test
    public void testDeleteEmpty() throws Exception{
        p = new PassengerQueue(3);
        Passenger deleted = p.delete("Tharindu", 2);
        assertEquals(null, deleted);
        assertEquals(0, p.getSize());
        assertEquals(true, p.isEmpty());
    }

    @Test
    public void testGetQueueAfterDelAndDeq() throws Exception{
        p = new PassengerQueue(5);
        p.enqueue(new Passenger("Ammar", 10));
        p.enqueue(new Passenger("Tharindu", 4));
        p.dequeue();

        p.enqueue(new Passenger("Adeesha", 33));
        p.enqueue(new Passenger("Omar", 32));
        p.delete("Adeesha", 33);

        p.enqueue(new Passenger("Hala", 31));
        p.delete("Leena", 31);
        p.enqueue(new Passenger("Leena", 35));

        Passenger[] queue = p.getQueue();

        assertEquals(4, queue.length);
        assertEquals("Tharindu", queue[0].getFullName());
        assertEquals("Hala", queue[1].getFullName());
        assertEquals("Omar", queue[2].getFullName());
        assertEquals("Leena", queue[3].getFullName());
    }

    @Test
    public void testBubbleSort() throws Exception {
        p = new PassengerQueue(5);
        p.enqueue(new Passenger("Ammar", 10));
        p.enqueue(new Passenger("Tharindu", 4));
        p.enqueue(new Passenger("Yasitha", 3));
        p.bubbleSortQueue();
        Passenger[] queue = p.getQueue();
        assertEquals("Yasitha", queue[0].getFullName());
        assertEquals("Tharindu", queue[1].getFullName());
        assertEquals("Ammar", queue[2].getFullName());
    }

    @org.junit.Test
    public void testDeleteUnavailable() throws Exception{
        p = new PassengerQueue(4);
        p.enqueue(new Passenger("Ammar", 1));
        p.enqueue(new Passenger("Tharindu", 2));
        p.enqueue(new Passenger("Yasitha", 3));
        Passenger deleted = p.delete("Wrong", 2);
        assertEquals(3, p.getSize());
        assertEquals(null, deleted);
    }
}