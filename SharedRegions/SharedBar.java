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

package SharedRegions;

//Import class semaphore
import CommonInfra.Semaphore;
//Import entities states
import Entities.States.WaiterStates;
//Import entities
import Entities.Waiter;
//Import problem's constants
import static Main.Simulator.NSTUDENTS;


/**
 * Restaurant's shared bar zone.
 * @author Daniel Nunes and Rafael Direito
 */
public class SharedBar {
    
    
    /**
     * Imposition of mutual exclusion. 
     */
    private Semaphore mutex;
    
    
    /**
     * Synchronization point of the waiter while he's waiting for a request from 
     * a client or the chef.
     */
    private Semaphore waiterBusy;
    
    
    /**
     * The waiter blocks here while the students are eating, waiting for 
     * the last student eating each course or the student that will pay the bill
     * to unlock him.
     */
    private Semaphore waitingClientsSignal;
    
    
    /**
     * Char that indicates what operation is being executed by the waiter.
     */
    private char op;
    
    
    /**
     * Restaurant's shared repository zone.
     */
    private SharedRepo repo;
    
    
    /**
     * Number of students that left the restaurant. 
     */
    private  int numStudentsLeft;
       
    
    /**
     * <code>True</code> if waiter has to wait for the students to eat, so that
     * he can move on.
     */
    private boolean waitStudentsFinishCourse;
    
    
    /**
     * Constructor of the restaurant's shared bar zone.
     * @param repo Restaurant's shared repository zone.
     */
    public SharedBar(SharedRepo repo)
    {
        
        //Initializate restaurant's shared repository zone.
        this.repo=repo;
        
        //Initializate global variables.
        numStudentsLeft=0;
        waitStudentsFinishCourse=false;
        
        //Initializate semaphores.
        mutex = new Semaphore();
        waiterBusy = new Semaphore();
        waitingClientsSignal = new Semaphore();
        
        //Allow entrance in critical region. 
        mutex.up();
    }
    
    
    /**
     * Method that keeps waiter busy waiting for a request.
     * @return Char that indicates what operation is being executed by the 
     * waiter.
     */
    public char lookAround()
    {
        //Waiter blocks waiting for a request.
        waiterBusy.down();
        
        //Check if the waiter has to wait for all the students to eat.
        if(waitStudentsFinishCourse)
            waitingClientsSignal.down();

        return op;
    }
    
    
    /**
     * Waiter returns to the bar after ending an action.
     */
    public void returnToTheBar()
    {
        //Enters in critical region.
        mutex.down();
        
        //Get current thread.
        Waiter w = ( (Waiter) Thread.currentThread());
        
        //Update waiter states to appraising situation.
        w.setWaiterState(WaiterStates.APPSIT);
        repo.updateWaiterState(WaiterStates.APPSIT);

        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Sub operation of tab.enter() that indicates to the waiter that the 
     * students are arriving.
     */
    public void studentArrived()
    {
        //Enters in critical region.
        mutex.down();
        
        //Change operation to N.
        op = 'N';
        
        //Releases the waiter because a student has arrived.
        waiterBusy.up();
        
        //Leaves critical region.
        mutex.up();  
    }
    
    
    /**
     * After all the students have chosen their meal, this method is called by
     * the student that has arrived in first place.
     */
    public void callTheWaiter()
    {
        //Enters in critical region.
        mutex.down();
        
        //Change operation to O.
        op = 'O';
        
        //Releases the waiter because all students have chosen their meal.
        waiterBusy.up();
        
        //Leaves critical region.
        mutex.up();  
    }
    
    
    /**
     * Called by the chef, from the kitchen, this method informs that the waiter 
     * can pick up the portions from the kitchen and deliver them to the students.
     */
     public void alertTheWaiter()
    {
        //Enter critical region
        mutex.down();

        //Change operation to C.
        op = 'C';
        
        //Releases the waiter because all students have chosen their meal.
        waiterBusy.up(); 
        
        //Leave critical region
        mutex.up();
    }
     
     
    /**
    * Sub operation of the tab.shouldHaveArrivedEarlier() called by the last
    * student arriving at the restaurant.
    */
     public void studentReadyToPayTheBill()
    {
        //Enters in critical region.
        mutex.down();
        
        //Change operation to P.
        op = 'P';
        
        //Releases the waiter because all the courses have been eaten.
        waiterBusy.up();
        
        //Unlock the waiter, so he can go on.
        waitingClientsSignal.up();
        
        //The students already ate, so the waiter doesn't have to wait 
        //for their signal.
        waitStudentsFinishCourse = false;
        
        //Leaves critical region.
        mutex.up();          

    }
     
     
     /**
     * Sub operation of tab.exit() that indicates that a student is leaving.
     */
     public void studentIsLeaving()
    {
        //Enters in critical region.
        mutex.down();
        
        //Change operation to G.
        op = 'G';
        
        //Releases the waiter because the students are leaving the restaurant
        //and now he can say goodbye to each one. 
        waiterBusy.up();
        
        //Leaves critical region.
        mutex.up(); 
    }  
     
     
     /**
      * Sub operation of tab.exit() that is called when all students have left
      * the restaurant.
     */
     public void allStudentsHaveLeft()
    {
        //This method is invoqued by sayGoodBye(), which is already inside of
        //the critical region
        
        //Change operation to E.
        op = 'E';
        
        //Releases the waiter because all students have left the restaurant and
        //now he can end his life cycle.
        waiterBusy.up();
    }     
     
    
    
     /**
      * Invoked by the last student eating each course, so that the waiter can
      * move on, collecting the portions from the chef.
      */
     public void signalTheWaiter()
     {
         //Enter critical regian
         mutex.down();
         
         //Unlock waiter
         waitingClientsSignal.up();
         
         //Leave critical regian
         mutex.up();
     }
     
     
     /**
      * Invoked by the waiter as soon as he serves all the clients. This method
      * will change a flag, allowing the waiter to get blocked in the bar, until
      * he is called by the last student eating each meal, or, if the we are at
      * the last course, by the last student entering the restaurant.
      */
     public void waitForStudentToEat()
     {        
        //Enter critical region
        mutex.down();
        
        //If flag is false, change it to true
        if(!waitStudentsFinishCourse )
            waitStudentsFinishCourse = true;
        
        //Leave critical region
        mutex.up();
     }
     
     
     /**
     * If all students have finished the last course, the waiter prepares the
     * bill.
     */
    public void prepareTheBill()
    {
        //Enters in critical region.
        mutex.down();
        
        //Get current thread.
        Waiter w = ((Waiter) Thread.currentThread());
        
        ////Update waiter states to processing the bill.
        w.setWaiterState(WaiterStates.PROBIL);
        repo.updateWaiterState(WaiterStates.PROBIL);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Waiter says goodbye to each student when the bill is already payed.
     */
    public void sayGoodbye()
    {   
        //Enters in critical region.
        mutex.down();
        
        //One more student leaves the restaurant.
        numStudentsLeft++;
        
        //All students have left the restaurant.
        if(numStudentsLeft == NSTUDENTS)
            allStudentsHaveLeft();
        
        //Leaves critical region.
        mutex.up();
    }
}
