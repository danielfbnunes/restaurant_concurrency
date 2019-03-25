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

package Entities.States;
/**
 * Contains all the student's states.
 * @author Daniel Nunes and Rafael Direito
 * <ul>
 *  <li>{@link #GOTTRT}</li>
 *  <li>{@link #TASATT}</li>
 *  <li>{@link #SELTCO}</li>
 *  <li>{@link #ORGTOD}</li>
 *  <li>{@link #CHAWCO}</li>
 *  <li>{@link #ENJTME}</li>
 *  <li>{@link #PAYTBI}</li>
 *  <li>{@link #GOINHO}</li>
 * </ul>
 */
public enum StudentStates 
{
    /**
     *  Going to the restaurant.
     */
    GOTTRT  , 
    /**
     *  Taking a seat at the table.
     */
    TASATT  ,
    /**
     *  Selecting the courses.
     */
    SELTCO  ,
    /**
     *  Organizing the order.
     */
    ORGTOD  ,
    /**
     *  Chatting with companions.
     */
    CHAWCO  ,
    /**
     *  Enjoying the meal.
     */
    ENJTME  ,
    /**
     *  Paying the bill.
     */
    PAYTBI  ,
    /**
     *  Going home.
     */
    GOINHO  ;
}
