import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class Node
{
    int data;
    Node next;

    public Node(int data)
    {
        this.data = data;
        this.next = null;
    }
}

class LinkedList
{
    Node head;

    public synchronized void add(int data)
    {
        System.out.println("added " + data);
        if (head == null)
        {
            head = new Node(data);
            return;
        }

        if (data < head.data)
        {
            Node newHead = new Node(data);
            newHead.next = head;
            head = newHead;
            return;
        }

        Node curr = head;

        while (curr.next != null)
        {
            if (data > curr.data && data < curr.next.data)
            {
                Node temp = curr.next;
                curr.next = new Node(data);
                curr.next.next = temp;
                return;
            }
            curr = curr.next;
        }

        curr.next = new Node(data);
    }

    public synchronized Node remove()
    {

        if (head == null)
        {
            //System.out.println("chain empty, could not remove");
            return null;
        }
        

        Node temp = head;
        System.out.println("Removed " + temp.data);
        head = head.next;

        return temp;
    }

    public synchronized boolean check(int data)
    {
        if (head == null)
        {
            return false;
        }

        Node temp = head;
        while (temp != null)
        {
            if (data == temp.data)
            {
                return true;
            }
            temp = temp.next;
        }

        return false;
    }

    public synchronized boolean isEmpty()
    {
        return head == null;
    }

    public synchronized void printList()
    {
        Node temp = head;

        while(temp != null)
        {
            System.out.print(temp.data + " ");
            temp = temp.next;
        }
        System.out.println();
    }
}

class Servant extends Thread
{
    private static ConcurrentLinkedQueue<Integer> unorderedBag = getUnorderedBag();
    private static LinkedList chain = new LinkedList();

    public static ConcurrentLinkedQueue<Integer> getUnorderedBag()
    {
        ArrayList<Integer> unorderedList = new ArrayList<>();
        for (int i = 1; i <=5; i++) 
        {
            unorderedList.add(i);
        }

        Collections.shuffle(unorderedList);
        
        ConcurrentLinkedQueue<Integer> unorderedBag = new ConcurrentLinkedQueue<>(unorderedList);


        return unorderedBag;
    }

    public void run() 
    {
        while (!unorderedBag.isEmpty() || chain.head != null)
        {
            int choice = (int)(Math.random() * 3) + 1;

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
                    int randTag = (int)(Math.random() * 500000) + 1;
                    boolean containsTag = chain.check(randTag);
                }
            }
        }
    }

    public class Assignment3
    { 
        
        public static void main(String[] args) 
        {
            int numServants = 4;
            Servant[] servants = new Servant[numServants];


            long startT = System.currentTimeMillis();

            for (int i = 0; i < numServants; i++)
            {
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

            long endT = System.currentTimeMillis();

            System.out.println("Time: " + (endT- startT) + " ms");
        }
    }   
}