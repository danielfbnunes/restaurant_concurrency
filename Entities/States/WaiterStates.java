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
 * Contains all the waiter's states.
 * @author Daniel Nunes and Rafael Direito
 * <ul>
 *  <li>{@link #APPSIT}</li>
 *  <li>{@link #PREMEN}</li>
 *  <li>{@link #TAKORD}</li>
 *  <li>{@link #PLAORD}</li>
 *  <li>{@link #WAIPOR}</li>
 *  <li>{@link #PROBIL}</li>
 *  <li>{@link #RECPAY}</li>
 * </ul>
 */


public enum WaiterStates
{
    /**
    *   Appraising situation.
    */
    APPSIT ,
    /**
    *   Presenting the menu.
    */
    PREMEN ,
    /**
    *   Taking the order.
    */
    TAKORD ,
    /**
    *   Placing the order.
    */
    PLAORD ,
    /**
    *   Waiting for portion.
    */
    WAIPOR ,
    /**
    *   Processing the bill.
    */
    PROBIL ,
    /**
    *   Receiving payment.
    */
    RECPAY ;
}
