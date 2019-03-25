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

//Import Shared Regions
import SharedRegions.SharedBar;
import SharedRegions.SharedKitchen;
import SharedRegions.SharedRepo;
import SharedRegions.SharedTable;
//Import Entities
import Entities.Waiter;
import Entities.Student;
import Entities.Chef;
//Import problem's constant
import static Main.Simulator.NSTUDENTS;



/**
 * Restaurant's main program.
 * @author Daniel Nunes and Rafael Direito
 */
public class RestaurantMain {
    
    public static void main(String[] args) 
    {
        //Define entities.        
        Student [] student;
        Waiter waiter ;
        Chef chef ;
        
        //Create shared regions.
        SharedRepo repo = new SharedRepo();
        SharedBar bar = new SharedBar(repo);
        SharedTable  tab = new SharedTable(repo, bar);
        SharedKitchen kit = new SharedKitchen(repo, bar);
        
        //Write log's file header.
        repo.writeHeader("RestaurantLog.txt");
        
        
        //Instanciate entities.
        waiter =  new Waiter(bar, tab, kit);
        chef = new Chef(bar, kit);
        student = new Student[NSTUDENTS];
        for (int i = 0; i < NSTUDENTS; i++)
            student[i] = new Student(bar, tab, i);
        
        
        //Start entities.
        for (int t = 0; t < NSTUDENTS ; t++)
        {
            student[t].start();
            System.out.println("\u001B[32m Student S" + t + " is starting.");
        }
        waiter.start();
        System.out.println("\033[1;32m Waiter is starting.");
        chef.start();
        System.out.println("\033[1;32m Chef is starting.");
        
        
        //End entities.
        for (int t = 0; t < NSTUDENTS; t++)
        {
            try
            {
                student[t].join ();
            }
            catch (InterruptedException e)
            {
                System.err.println("Couldn't end students!");
                System.exit(1);
            }
            System.out.println("\u001B[31m Student S" + t + " has ended.");
        }
        
         try
        {
            chef.join ();
            System.out.println("\u001B[31m Chef has ended.");
        }
        catch (InterruptedException e)
        {
            System.err.println("Couldn't end chef!");
            System.exit(1);        
        }
         
        try
        {
            waiter.join ();
            System.out.println("\u001B[31m Waiter has ended.");
        }
        catch (InterruptedException e)
        {
            System.err.println("Couldn't end waiter!");
            System.exit(1);   
        }        
    }   
}
