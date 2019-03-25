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

package Main;

/**
 * Class containing the problem's constants: (Number of students, number of 
 * courses, ...).
 * @author Daniel Nunes and Rafael Direito
 */
public class Simulator 
{
    /**
     * Number of courses.
     */
    public final static int NCOURSES = 3;
    
    /**
     * Number of students attending at the dinner.
     */
    public final static int NSTUDENTS = 7;
    
    /**
     * Arrival order of the first student arriving at the restaurant.
     */
    public final static int FIRST = 0 ;
    
    /**
     * Arrival order of the last student arriving at the restaurant.
     */
    public final static int LAST = NSTUDENTS -1;
    
    /**
     * ID of the first student arriving.
     */
    public static  int FIRST_ID;
    
    /**
     * ID of the last student arriving.
     */
    public static  int LAST_ID;
}
