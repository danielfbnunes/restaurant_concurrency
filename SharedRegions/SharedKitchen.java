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
import Entities.States.ChefStates;

//Import entities
import Entities.Waiter;
import Entities.Chef;

//Import problem's constants
import static Main.Simulator.NCOURSES;
import static Main.Simulator.NSTUDENTS;

/**
 * Restaurant's shared kitchen zone.
 * @author Daniel Nunes and Rafael Direito
 */
public class SharedKitchen {
    
    
    /**
     * Imposition of mutual exclusion. 
     */
    private Semaphore mutex;
    
    
    /**
     * Synchronization point of the waiter while waits for the confirmation of
     * the chef that receive the order.
     */
    private Semaphore waitForChef;
    
    
    /**
     * Synchronization point of the chef while waits for an order.
     */
    private Semaphore waitingOrder;
    
    
    /**
     * Synchronization point of the chef that confirms that he has delivered a
     * portion.
     */
    private Semaphore portionDelivered;
    
    
    /**
     * Guarantees that the chef waits for the waiter's arrival.
     */
    private Semaphore waitForWaiter;
    
    
    /**
     * Number of portions delivered by the chef.
     */
    private int numPortionsDelivered;
    
    
    /**
     * Number of courses delivered by the chef.
     */
    private int numCoursesDelivered;
    
    
    /**
     * Counts the number of portions produced by the chef, at each course.
     */
    private int numPortionsProducedByChef;
    
    
    /**
     * Restaurant's shared repository zone.
     */
    private SharedRepo repo;
    
    
    /**
     * Restaurant's shared bar zone.
     */
    private SharedBar bar;
    
    
    /**
     * Constructor of the restaurant's shared kitchen zone.
     * @param repo Restaurant's shared repository zone.
     * @param bar Restaurant's shared bar zone.
     */
    public SharedKitchen ( SharedRepo repo, SharedBar bar)
    {
        //Initializate restaurant's shared repository zone.
        this.repo = repo;
        this.bar = bar;
        
        //Initializate global variables.
        numPortionsDelivered = 0;
        numCoursesDelivered = 0;
        numPortionsProducedByChef = 0;
        
        //Initializate semaphores.
        mutex = new Semaphore();
        waitingOrder = new Semaphore();
        waitForChef = new Semaphore();
        waitingOrder = new Semaphore();
        portionDelivered = new Semaphore();
        waitForWaiter = new Semaphore();
        
        //Allow entrance in critical region. 
        mutex.up();

    }
    
    
    /**
     * Chef is blocked waiting for an order given by waiter.
     */
    public void watchTheNews()
    {
        //Chef locks waiting for an order.
        waitingOrder.down();
    }
    
    
    /**
     * Waiter gives an order to the chef and waits for the confirmation.
     */
    public void handNoteToTheChef()
    {
        //Get current thread.
        Waiter w = ( (Waiter) Thread.currentThread());
        
        //Update waiter's state to place the order.
        w.setWaiterState(WaiterStates.PLAORD);
        repo.updateWaiterState(WaiterStates.PLAORD);
        
        //The waiter will unlock the chef who is blocked waiting for an order.
        waitingOrder.up();
        
        //The waiter will block waiting for the confirmation given by chef.
        waitForChef.down();
    }
        
        
    /**
     * Chef starts the preparation of the order.
     */    
    public void startPreparation()
    {
        //Enters in critical region.
        mutex.down(); 
        
        //Get current thread.
        Chef c = ( (Chef) Thread.currentThread());
        
        //Chef confirms the order, waking up the waiter
        waitForChef.up();
        
        //Update chef's state to preparing course
        c.setChefState(ChefStates.PREPCO);
        repo.updateCourse(numCoursesDelivered + 1);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Chef starts to dish the portions.
     */
    public void proceedToPresentation()
    {
        //Enters in critical region.
        mutex.down();
        
        //Get current thread.
        Chef c = (Chef) Thread.currentThread();
        
        //Update chef's state to dishing portions.
        c.setChefState(ChefStates.DISHPO);
        repo.updateChefState(ChefStates.DISHPO);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Check invokes this method when he wants the waiter to pick the portions
     * up. This method invokes an alertTheWaiter() method inside the bar,
     * signaling the waiter to come to the kitchen to collect the portions.
     */
    public void alertTheWaiter()
    {
        //Enters in critical region.
        mutex.down();
        
        //One more portion cooked.
        numPortionsProducedByChef++;
        
        //Get current thread.
        Chef c = (Chef) Thread.currentThread();
        //Update chef's state to delivering portions
        c.setChefState(ChefStates.DILPOR);
        repo.updateChefState(ChefStates.DILPOR);
        
        
        //Waiter is called when the first portion of each course has been
        //prepared.
        if(numPortionsProducedByChef==1)
            bar.alertTheWaiter();
        
        //When all portions have been prepared, next course comes and the number
        //of portions cooked starts at 0.
        if(numPortionsProducedByChef == NSTUDENTS)
            numPortionsProducedByChef = 0;
         
        //Leaves critical region. 
        mutex.up();
        
        
        //Chef blocks here, waiting for the waiter's arrival
        waitForWaiter.down();
        
    }
    
    
    /**
     * Chef delivers the portions one by one until all the portions have been
     * delivered.
     * @return Returns <code>true</code> if all portions have been delivered and
     * false if not.
     */
    public boolean haveAllPortionsBeenDelivered()
    {
        //Enters in critical region.
        mutex.down();
                
        //Boolean that indicates if all portions have been delivered.
        boolean allPortionsDelivered;

        //Increment number of portions delivered.
        numPortionsDelivered++;
        
        //Checks if all portions have been delivered.
        if (numPortionsDelivered == NSTUDENTS)  allPortionsDelivered = true;
        else   allPortionsDelivered = false;
                
        //Leaves critical region.        
        mutex.up();
        
        //Chef tell the waiter to pick up the portion.
        portionDelivered.up();
        
        //Restart the numPortionsDelievred if all portions of this course have 
        //been delivered so that, in the next course, the numPortionsDelivered
        //starts at 0.
        if(allPortionsDelivered) numPortionsDelivered = 0;
        
        return allPortionsDelivered;
    }
    
    
    /**
     * The waiter collects all the portions, one by one.
     */
    public void collectPortion()
    {
        //Enters in critical region.
        mutex.down();
        
        //Get current thread.
        Waiter w = ( (Waiter) Thread.currentThread());
        
        //Waiter only updates his state to waiting portion when he is
        //delivering the first portion.
        if(numPortionsDelivered == 0)
        {
            //update waiter's state to waiting for portion.
            repo.updateWaiterState(WaiterStates.WAIPOR);
            w.setWaiterState(WaiterStates.WAIPOR);
        }
        
        //Waiter unlocks the chef, signaling him that the waiter has arrived.
        waitForWaiter.up();
        
        //Leaves critical region.
        mutex.up();
        
        //Waiter blocks here, waiting for to give him the food
        portionDelivered.down();
        
    }
        
    
    /**
     * Chef continues to dish the portions.
     */
    public void haveNextPortionReady()
    {
        //Enters in critical region.
        mutex.down();
        
        //Get current thread.
        Chef c = (Chef) Thread.currentThread();
        
        //update chef's state, who returns to dishing portion.
        c.setChefState(ChefStates.DISHPO);
        repo.updateChefState(ChefStates.DISHPO);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Check if all the courses have been delivered.
     * @return Returns true if all courses have been delivered and false if not.
     */
    public boolean hasTheOrderBeenCompleted()
    {
        //Enters in critical region.
        mutex.down();
        
        //Indicates if all courses have been delivered.
        boolean allCoursesServed;
        
        //Increment number of served courses.
        numCoursesDelivered++;

        //Checks if all courses have been delivered.
        if (numCoursesDelivered == NCOURSES) allCoursesServed = true;
         else allCoursesServed = false;
        
        //Leaves critical region.
        mutex.up();
        
        return allCoursesServed;
    }
    
    
    /**
     * Chef prepares another course.
     */
    public void continuePreparation()
    {
        //Enters in critical region.
        mutex.down(); 
        
        //Get current thread.
        Chef c = ( (Chef) Thread.currentThread());
        
        //Update chef's state to preparing course.
        c.setChefState(ChefStates.PREPCO);
        repo.updateCourse(numCoursesDelivered + 1);
        
        //Leaves critical region.
        mutex.up();  
    }
    
    
    /**
     * Chef ends his service.
     */
    public void cleanUp()
    {
        //Enters in critical region.
        mutex.down();
        
        //Get current thread.
        Chef c = (Chef) Thread.currentThread();
        
        //Update chef's state to closing service.
        c.setChefState(ChefStates.CLOSER);
        repo.updateChefState(ChefStates.CLOSER);
        
        //Leaves critical region.
        mutex.up();
    }
}
