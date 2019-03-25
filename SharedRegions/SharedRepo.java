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
import Entities.States.ChefStates;
import Entities.States.StudentStates;
import Entities.States.WaiterStates;

//Import problem's constants
import static Main.Simulator.NSTUDENTS;

//Import GenClass
import genclass.TextFile;


/**
 * Restaurant's shared repository zone.
 * @author Daniel Nunes and Rafael Direito
 */
public class SharedRepo {
    
    /**
     * Allows to manipulate text files.
     */
    TextFile log;
    
    /**
     * Log file where the data is written.
     */
    private String fileName;
    
    /**
     * Current course.
     */
    private int courseNumber;
    
    /**
     * Restaurant's chef states.
     */
    private ChefStates chefState;
    
    /**
     * Restaurant's waiter states.
     */
    private WaiterStates waiterState;
    
    /**
     * Restaurant's students states.
     */
    private StudentStates[] studentsState;
    
    /**
     * Imposition of mutual exclusion. 
     */
    private Semaphore mutex;
    
    /**
     * Constructor of restaurant's shared repository zone.
     */
    public SharedRepo()
    {
        //Initializate entities states.
        studentsState = new StudentStates[NSTUDENTS];
        for (int i = 0; i< NSTUDENTS ; i++)
            studentsState[i] = StudentStates.GOTTRT;
        chefState = ChefStates.WAITOD;
        waiterState = WaiterStates.APPSIT;
        
        //Initializate log.
        log =  new TextFile();
        
        //Initializate semaphores.
        mutex = new Semaphore();
        
        //Allow entrance in critical region. 
        mutex.up();
    }
    
    
    /**
     * Method that updates chef's state.
     * @param cState New state given to the chef.
     */
    public void updateChefState(ChefStates cState)
    {
        //Enters in critical region.
        mutex.down();
        
        //Append a new line with the new chef state.
        chefState = cState;
        appendLine(fileName, 0);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Method that updates waiter's state.
     * @param wState New state given to the waiter.
     */
    public void updateWaiterState(WaiterStates wState)
    {
        //Enters in critical region.
        mutex.down();
        
        //Append a new line with the new waiter state.
        waiterState = wState;
        appendLine(fileName, 0);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Method that updates student's state.
     * @param sState New state given to the student.
     * @param ID Id of the student that has a new state.
     */
    public void updateStudentState(StudentStates sState, int ID)
    {
        //Enters in critical region.
        mutex.down();
        
        //Append a new line with the new student state.
        studentsState[ID] = sState;
        appendLine(fileName, 0);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Method that updates the current course.
     * @param courseNumber Current course number.
     */
    public void updateCourse(int courseNumber)
    {
        //Enters in critical region.
        mutex.down();
        
        //Append a new line only refering the new course number.
        this.courseNumber = courseNumber;
        appendLine(fileName, 1);
        
        //Append a new line with the new chef state, that is preparing the new
        //course.
        chefState = ChefStates.PREPCO;
        appendLine(fileName, 0);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Calls initFile that will create a new log file and his header.
     * @param fileName Log file where the data will be written.
     */
    public void writeHeader(String fileName)
    {
        //Enters in critical region.
        mutex.down();
        
        //Function that creates a new file and writes the header on it.
        initFile(fileName);
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Appends a new line to the log file based on the option given by argument.
     * @param fileName Log file where the data will be written.
     * @param optType If option == 0, a new states line will be written with the
     * updated states. If option == 1, a new course line will be written 
     * updating the current course.
     */
    public void appendLine(String fileName, int optType)
    {
        if(optType == 0)
        {
            boolean error;
            
            //openForAppending returns true if the file can be created and false
            //in other case.
            error = !log.openForAppending(null, fileName);
            
            //Exit program if file wasn't created.
            if(error)
            {
                System.err.println("Unable to create file!");
                System.exit(1);
            }
            
            //Write states line with the updated states for each entitie.
            log.writeFormString(16 , chefState.name() ,
                    waiterState.toString());
            for(int i = 0 ; i<NSTUDENTS; i++)
                log.writeFormString(16 , studentsState[i].toString());
            log.writeString("\n");
            log.close();
        }
        
        if(optType == 1)
        {
            boolean error;
            //openForAppending returns true if the file can be created and false
            //in other case.
            error = !log.openForAppending(null, fileName);
            
            //Exit program if file wasn't created.
            if(error)
            {
                System.err.println("Unable to create file!");
                System.exit(1);
            }
            //Write course line with the current course.
            log.writelnString(courseNumber+" course");
            log.close();
        }
    }
    
    
    /**
     * This method writes a new line on the log file with the string passed as
     * argument (This method is not being used, although it can be used for 
     * testing purposes).
     * @param input Line to be written in the log file.
     */
    public void customAppendLine(String input)
    {
        //Enters in critical region.
        mutex.down();
        
        boolean error;
        //openForAppending returns true if the file can be created and false
        //in other case.
        error = !log.openForAppending(null, fileName);
        
        //Exit program if file wasn't created.
        if(error)
        {
            System.err.println("Unable to open file!");
            System.exit(1);
        }
        
        //Write a new line with the string passed as argument.
        log.writelnString("-->"+input);
        log.close();
        
        //Leaves critical region.
        mutex.up();
    }
    
    
    /**
     * Method that create a new log file and initiates it.
     * @param fileName Log file where the data will be written.
     */
    private void initFile(String fileName)
    {
        boolean error;
        //openForAppending returns true if the file can be created and false
        //in other case.
        error = !log.openForWriting(null, fileName);
        
        //Exit program if file wasn't created.
        if(error)
        {
            System.err.println("Unable to create file!");
            System.exit(1);
        }
        
        //Saves the log's file name.
        this.fileName = fileName;
        
        //Print a title line.
        //Print an empty line.
        //Print the columns identifier. 
        //Print the initial state.
        for (int i = 0 ; i <  (32 + 16*NSTUDENTS)/2 - 7 ; i++)
            log.writeChar(' ');
        log.writelnString("THE RESTAURANT\n");

        log.writeFormString(16 , "CHEF_STATE","WAITER_STATE");
        for(int i = 0 ; i<NSTUDENTS; i++)
            log.writeFormString(16 , "STU_STATE"+(i+1));
        
        log.writeString("\n");
        log.writeFormString(16 , chefState.name() ,waiterState.toString());
        for(int i = 0 ; i<NSTUDENTS; i++)
            log.writeFormString(16 , studentsState[i].toString());

        log.writeString("\n");
        log.close();
    }
}
