/*
 * The problem:
 * "Events portray activities that take place when a group of students, enrolled in
 * Computação Distribuída, go to a famous restaurant downtown for a gourmet dinner 
 * to celebrate the beginning of the second semester. There are three main locations
 * within the restaurant that should be accounted for: the table where the students 
 * sit to have their meal, the kitchen where the chef prepares it according to the
 * orders placed by the students, and the bar where the waiter stands waiting for 
 * service requests. There are, furthermore, three kinds of interacting entities: 
 * N students , one waiter and one chef."
 * 
 * The main goal of this project is to design and implement a solution to the 
 * problem given above.
 * 
 * This project was developed during the course of Distributed Computing, at the 
 * University of Aveiro, under the supervision of Prof. António Rui Borges.
 */

package Entities;

//Import Shared Regions
import SharedRegions.SharedBar;
import SharedRegions.SharedTable;
//Import Student's states
import Entities.States.StudentStates;
//Import problem's constants
import static Main.Simulator.NCOURSES;
import static Main.Simulator.FIRST;
//Impor math random
import java.util.Random;


/**
 * Entity Student, containing his methods and life cycle ({@link #run() run}).
 * @author Daniel Nunes and Rafael Direito
 */
public class Student extends Thread
{
    //Define student ID
    private int ID;
    
    //Define shared regions.The Student will have access to the table and to the
    //bar, so he can call the waiter
    private SharedBar bar;
    private SharedTable tab;
    
    //Define the student's state
    private StudentStates state;
    
    //Define random
    private Random rand;
    
    
    /**
     * Student's default constructor.
     * @param bar restaurant's shared bar zone
     * @param table restaurant's shared table
     * @param ID student's ID
     */
    public Student(SharedBar bar, SharedTable table, int ID)
    {
        //Thread
        super();
        
        //Instanciate and initialize random
        rand = new Random();
        
        //Initialize this student's ID
        this.ID = ID;
        
        //Initialize student state
        state = StudentStates.GOTTRT;
        
        //Initialize shared regions
        this.bar = bar;
        this.tab = table; 
    }
    
    
    /**
     * Used to set the student's state.
     * @param s receives Student State, from {@link Entities.States.StudentStates }.
     */
    public void setStudentState(StudentStates s) { state = s; }
    
    
    /**
     * Used to get the student's ID.
     * @return ID of the student
     */
    public int getStudentID() { return ID; }
    
    
    /**
     * Invoked by the student when he starts his life cycle.
     * Contains a sleep function with a random value.
     */
    public void walkABit()
    {
        try 
        {
            Thread.sleep((long)(Math.random() * 300));
        } 
        catch (InterruptedException ex) 
        {
            System.err.print("Unable to realize sleep() inside the walkABit() method");
            System.exit(1);
        }
    }
    
 
    /**
     * Contains the student's life cycle, overriding the run method of
     * a generic thread.
     */    
    @Override
    public void run()
    {
        int arrivalOrder;
        walkABit();
        arrivalOrder = tab.enter();
        tab.readTheMenu();
       
        if (arrivalOrder == FIRST)
        {
            while(!tab.hasEveryBodyChosen())
                tab.prepareTheOrder();
            
            bar.callTheWaiter();
            tab.describeTheOrder();
            tab.joinTheTalk();
        }
        else
        {
            tab.informCompanion();
        }
        for(int nc=0; nc < NCOURSES ; nc++)
        {
            tab.startEating();
            tab.endEating();
            if(tab.hasEveryBodyFinished())
            {
                if(nc==(NCOURSES-1))
                {
                    tab.shouldHaveArrivedEarlier();
                    tab.honourTheBill();
                }
                else
                {
                    tab.signalTheWaiter();
                }
            }
        }
        tab.exit();
    }   
}
