import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class Node {
    int data;
    Node next;

    public Node(int data) {
        this.data = data;
        this.next = null;
    }
}

// linkedList used to store chain of presents
class LinkedList {
    Node head;

    // adds present to chain & removes from unordered bag
    public synchronized void add(int data) 
    {
        if (head == null) {
            head = new Node(data);
            return;
        }

        if (data < head.data) {
            Node newHead = new Node(data);
            newHead.next = head;
            head = newHead;
            return;
        }

        Node curr = head;

        while (curr.next != null) {
            if (data > curr.data && data < curr.next.data) {
                Node temp = curr.next;
                curr.next = new Node(data);
                curr.next.next = temp;
                return;
            }
            curr = curr.next;
        }

        curr.next = new Node(data);
    }

    // removes present from chain & writes "thank you" letter
    public synchronized Node remove() {

        if (head == null) {
            return null;
        }

        Node temp = head;
        
        head = head.next;

        return temp;
    }

    // checks if a present with given id is in chain
    public synchronized boolean check(int data) {
        if (head == null) {
            return false;
        }

        Node temp = head;
        while (temp != null) {
            if (data == temp.data) {
                return true;
            }
            temp = temp.next;
        }

        return false;
    }
}

class Servant extends Thread 
{
    private static ConcurrentLinkedQueue<Integer> unorderedBag = getUnorderedBag();
    private static LinkedList chain = new LinkedList();

    public static ConcurrentLinkedQueue<Integer> getUnorderedBag() {
        ArrayList<Integer> unorderedList = new ArrayList<>();
        // add 500000 presents to list
        for (int i = 1; i <= 500000; i++) {  
            unorderedList.add(i);
        }

        // shuffle around list
        Collections.shuffle(unorderedList);

        ConcurrentLinkedQueue<Integer> unorderedBag = new ConcurrentLinkedQueue<>(unorderedList);

        return unorderedBag;
    }

    public void run() 
    {
        while (!unorderedBag.isEmpty() || chain.head != null) {
            int choice = (int) (Math.random() * 3) + 1;

            if (choice == 1) 
            {
                if (!unorderedBag.isEmpty()) 
                {
                    int temp = unorderedBag.poll();
                    chain.add(temp);
                }
            } 
            else if (choice == 2) 
            {
                if (chain.head != null) 
                {
                    chain.remove();
                }
            } 
            else if (choice == 3) 
            {
                if (chain.head != null) 
                {
                    int randTag = (int) (Math.random() * 500000) + 1;
                    boolean containsTag = chain.check(randTag);
                }
            }
        }
    }
}

class Sensor extends Thread
{
    private static ConcurrentLinkedQueue<Integer> temps = new ConcurrentLinkedQueue<Integer>();
    AtomicInteger numTempsAdded = new AtomicInteger();

    public static int[] getMaxFiveTemps()
    {
        Object[] tempsArray = temps.toArray();
        int[] maxFive = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

        for (int i = 0; i < 480; i++) // 8 sensors read every minute for 1 hour = 480 readings
        {
            int currTemp = (int)tempsArray[i];
            for (int j = 4; j >= 0; j--) 
            {
                if (maxFive[j] == Integer.MIN_VALUE) 
                {
                    maxFive[j] = currTemp;
                }
                else if (currTemp > maxFive[j])
                {
                    int temp = maxFive[j];
                    maxFive[j] = currTemp;
                    currTemp = temp;
                }
            }
        }
        return maxFive;
    }

    public static int[] getMinFiveTemps()
    {
        Object[] tempsArray = temps.toArray();
        int[] minFive = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

        for (int i = 0; i < 480; i++) // 8 sensors read every minute for 1 hour = 480 readings
        {
            int currTemp = (int)tempsArray[i];
            for (int j = 4; j >= 0; j--) 
            {
                if (minFive[j] == Integer.MIN_VALUE) 
                {
                    minFive[j] = currTemp;
                }
                else if (currTemp < minFive[j])
                {
                    int temp = minFive[j];
                    minFive[j] = currTemp;
                    currTemp = temp;
                }
            }
        }
        return minFive;
    }

    public static int getHighestRange()
    {
        Object[] tempsArray = temps.toArray();
        int maxRange = Integer.MIN_VALUE;

        for (int i = 0; i < 400; i++) // looks at 80 ratings at a time, so has to stop 80 short
        {
            int range = highestRangeHelper(tempsArray, i);
            if (maxRange < range)
            {
                maxRange = range;
            }
        }
        return maxRange;
    }

    public static int highestRangeHelper(Object[] list, int start)
    {
        int minVal = (int)list[0];
        int maxVal = (int)list[0];

        for (int i = start; i < (start + 80); i++) // 80 readings per interval
        {
            if (minVal > (int)list[i])
            {
                minVal = (int)list[i];
            }
            if (maxVal < (int)list[i])
            {
                maxVal = (int)list[i];
            }
        }
        return Math.abs(maxVal - minVal);
    }

    public void run()
    {
        while (numTempsAdded.get() < 480)  // 8 sensors read every minute for 1 hour = 480 readings
        {
            int randTemp = (int) (Math.random() * 171) - 100; 
            temps.add(randTemp);
            numTempsAdded.incrementAndGet();
        }
    }
}

public class Assignment3 
{
    public static void main(String[] args) {
        // problem 1
        int numServants = 4;
        Servant[] servants = new Servant[numServants];

        
        for (int i = 0; i < numServants; i++) {
            servants[i] = new Servant();
            servants[i].start();
        }

        for (Servant thread : servants) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        System.out.println("Both unordered bag & chain of presents are empty");

        // problem 2
        int numSensors = 8;
        Sensor[] sensors = new Sensor[8];

        for (int i = 0; i < numSensors; i++) {
            sensors[i] = new Sensor();
            sensors[i].start();
        }

        for (Sensor thread : sensors) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int[] maxFiveTemps = Sensor.getMaxFiveTemps();
        int[] minFiveTemps = Sensor.getMinFiveTemps();
        System.out.print("5 highest temperatures: [");
        System.out.print(maxFiveTemps[0] + ", ");
        System.out.print(maxFiveTemps[1] + ", ");
        System.out.print(maxFiveTemps[2] + ", ");
        System.out.print(maxFiveTemps[3] + ", ");
        System.out.print(maxFiveTemps[4] + "]\n");
        System.out.print("5 lowest temperatures: [");
        System.out.print(minFiveTemps[0] + ", ");
        System.out.print(minFiveTemps[1] + ", ");
        System.out.print(minFiveTemps[2] + ", ");
        System.out.print(minFiveTemps[3] + ", ");
        System.out.print(minFiveTemps[4] + "]\n");
        System.out.println("largest range in a 10-minute interval: " + Sensor.getHighestRange());
    }
}