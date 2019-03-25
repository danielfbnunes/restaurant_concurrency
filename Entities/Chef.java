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
import SharedRegions.SharedKitchen;
//Import Chef's states
import Entities.States.ChefStates;
//Import problem's constants
import static Main.Simulator.NCOURSES;
import static Main.Simulator.NSTUDENTS;

/**
 * Entity Chef, containing his methods and life cycle ({@link #run() run}).
 * @author Daniel Nunes and Rafael Direito.
 */
public class Chef extends Thread
{
    //Define shared regions
    //The Chef will have access to his kitchen and to the bar, so he can
    //call the waiter
    private SharedBar bar;
    private SharedKitchen kit;
    
    //Define the chef's state
    private ChefStates state;
    
    
    /**
     * Chef's default constructor.
     * @param bar restaurant's shared bar zone
     * @param kit restaurant's shared kitchen zone
     */
    public Chef(SharedBar bar,  SharedKitchen kit)
    {
        //Thread
        super();
        
        //Set initial state
        state = ChefStates.WAITOD;
        
        //Initialize shared regions
        this.bar = bar;
        this.kit = kit;
    }
    
    
    /**
     * Used to set the chef's state.
     * @param s receives Chef State, from {@link Entities.States.ChefStates }.
     */
    public void setChefState(ChefStates s) {  state = s; }

    
    /**
     * Contains the chef's life cycle, overriding the run method of
     * a generic thread.
     */
    @Override
    public void run()
    {
        kit.watchTheNews();
        kit.startPreparation();
        for ( int nc = 0; nc < NCOURSES; nc++ ) 
        {
            kit.proceedToPresentation();
            for ( int nst = 0; nst < NSTUDENTS; nst++ ) 
            {
                kit.alertTheWaiter();
                if (!kit.haveAllPortionsBeenDelivered()) kit.haveNextPortionReady();
            }
            if (!kit.hasTheOrderBeenCompleted()) kit.continuePreparation();
        }
        kit.cleanUp();
    }
}
