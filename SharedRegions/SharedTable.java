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

//import common infra-strutures
import CommonInfra.Semaphore;
import CommonInfra.MemFIFO;
//import entities
import Entities.Student;
import Entities.Waiter;
//import entities states
import Entities.States.StudentStates;
import Entities.States.WaiterStates;
//import problem's constants
import static Main.Simulator.NSTUDENTS;
import static Main.Simulator.FIRST_ID;
import static Main.Simulator.LAST_ID;
import static Main.Simulator.NCOURSES;


/**
 * Table's shared region.
 * @author Daniel Nunes and Rafael Direito
 */
public class SharedTable 
{
    /**
     * Guarantees the critical region is only accessible in mutual 
     * exclusion regime.
     */
    private Semaphore mutex;

    
    
    /**
     * Signals the First Student that another one has already chosen his course.
     */
    private Semaphore courseChosen;
    
    
    /**
     * Semaphore array that blocks each students until all oh them
     * receive their food.
     */
    private Semaphore[] served;
    
    
    /**
     * Signals that the order has been described to the waiter.
     */
    private Semaphore orderDescribed;
    
    
    /**
     * Semaphore array that signals the initial interaction between a
     * student and the waiter, when the waiter salutes the student.
     */
    private Semaphore[] studentSaluted;
    
    
    /**
     * Signals that the bill has already been payed.
     */
    private Semaphore billPayed;
    
    
    /**
     * Signals that some student read the menu.
     */    
    private Semaphore studenReadTheMenu;
        
    
    /**
     * The waiter signals he received the payment of the bill, releasing
     * the last student.
     */
    private Semaphore billsPaymentConfirmed;
    
    
    /**
     * Signals that the waiter presented the bill, releasing the last student
     * to come and pay it.
     */
    private Semaphore waiterPresentedTheBill;
    
    
    /**
     * Signals that the first student to arrived has received information
     * regarding the course choice of another one.
     */
    private Semaphore courseInformationAcknowledged;
    
    
    /**
     * Counts the students that are already seated.
     */
    private int nStudentsSeated;
    
    /**
     * Counts the students that have chosen their course.
     */
    private int nStudentsChose;
    
     /**
     * Counts the students that have already eaten.
     */
    private int NStudsEat;
    
     /**
     * Current course number.
     */
    private int currentCourse;
    
     /**
     * Counts the the number of portions that have been served.
     */
    private int portionsServed;
    
    /**
    * If <code>true</code>, the first student is already organizing the order. 
    */
    private boolean firstOrganizedOrder;
    
    /**
     * FIFO saving the students as they come inside the restaurant. 
     */
    
    private MemFIFO studentsArrivalFIFO;
    
    /**
     * Declaration of the shared repository.
     */
    private SharedRepo repo;
    
    /**
     * Declaration of the shared bar region.
     */
    private SharedBar bar;
    
    
    
    /**
     * Shared Table's default constructor.
     * @param repo general repository
     * @param bar restaurant's shared bar zone
     */
    public SharedTable ( SharedRepo repo, SharedBar bar)
    {
        //Initialize shared regions
        this.repo = repo;
        this.bar = bar;
        
        //Instanciate and Initialize studentsArrivalFIFO
        studentsArrivalFIFO = new MemFIFO(NSTUDENTS);
        
        //Inicialize all the counters
        nStudentsSeated = 0;
        NStudsEat = 0;
        portionsServed = 0;
        nStudentsChose = 0;
        currentCourse = 1;
        
        //Initialize firstOrganizedOrder
        firstOrganizedOrder = false;
        
        //Instanciate ans Inititalize all the Semaphores
        mutex = new Semaphore();
        studentSaluted = new Semaphore [NSTUDENTS];
        orderDescribed =  new Semaphore();
        billPayed =  new Semaphore();
        courseChosen =  new Semaphore();
        studenReadTheMenu = new Semaphore();
        billsPaymentConfirmed = new Semaphore();
        waiterPresentedTheBill = new Semaphore();
        courseInformationAcknowledged = new Semaphore();
        for (int i = 0; i < NSTUDENTS; i++)
            studentSaluted[i]= new Semaphore();
        
        served =  new Semaphore [NSTUDENTS];
        for (int i = 0; i < NSTUDENTS; i++)
            served[i]= new Semaphore();
        
        //Guarentee that at least one entity will be able to enter inside the 
        //critical region
        mutex.up();
    }
    
    
    /**
     * Method invoked by a student when he enters the restaurant.
     * @return arrival order of each student
     */
    public int enter()
    {
        //stores the arrival order
        int arrivalOrder;
        
        //enter critical region
        mutex.down();
        
        //get student's ID
        Student s = ((Student) Thread.currentThread());
        int studentID = s.getStudentID();
        
        //update the student state to taking a seat at the table
        s.setStudentState(StudentStates.TASATT);
        repo.updateStudentState(StudentStates.TASATT, studentID);
        
        //waking up the waiter at the bar
        bar.studentArrived();
        
        //save arrivalOrder
        arrivalOrder = nStudentsSeated++;
        
        //Put the student in a FIFO, waiting for the wait to salute him
        studentsArrivalFIFO.write(studentID);
        
        //update FIRST and LAST student to arrive
        if(arrivalOrder == 0) FIRST_ID = studentID;
        if(arrivalOrder == NSTUDENTS-1) LAST_ID = studentID;
        
        //exit critical region
        mutex.up();
        
        //Student blocks here, waiting for the waiter to salute him
        studentSaluted[studentID].down();
                
        return arrivalOrder;
    }
    
    
    /**
     * Method invoked by the waiter, each time a student enters the restaurant.
     * The waiter comes and salutes the student, allowing him to take a seat
     * at the table, joining his companions.
     */
    public void saluteTheClient()
    {
        //enter critical region        
        mutex.down();
        
        Waiter w = ((Waiter) Thread.currentThread());
       
        //update the waiter's state
        w.setWaiterState(WaiterStates.PREMEN);
        repo.updateWaiterState(WaiterStates.PREMEN);
                
        //get the next client to salute, from FIFO
        int studentID = (int) studentsArrivalFIFO.read();
        
        //salute one client, who was waiting in a FIFO, unlocking him
        studentSaluted[studentID].up();
        
        //exit critical region
        mutex.up();
        
        //Waiter blocks here, waiting for the student to acknowledge he 
        //was saluted and read the menu        
        studenReadTheMenu.down();
    }
    
    
    /**
     * Method invoked by the student when he reads the Menu. This method will
     * unlock the waiter, who was waiting for the student to read the menu.
     */
    public void readTheMenu()
    {
        //enter critical region
        mutex.down();
        
        Student s = ((Student) Thread.currentThread());

        //update student state to SelectingCourse
        s.setStudentState(StudentStates.SELTCO);
        repo.updateStudentState(StudentStates.SELTCO, s.getStudentID());
        
        //Student confirms to the waiter, he is reading the menu.
        //This unlocks the waiter
        studenReadTheMenu.up();
      
        //exit critical region
        mutex.up();
    }
    
   
    /**
     * In here, the first student receives all the course choices of 
     * his companions.
     * Method invoked only by the first student arriving to the restaurant. 
     */
    public void prepareTheOrder()
    {
        //enter critical region
        mutex.down();
                
        Student s = ((Student) Thread.currentThread());
        
        //A student has chosen his dish
        nStudentsChose++;
                
        //if the first student did not start to organize the order:
        if(!firstOrganizedOrder)
        {
            //update student sate to Organizing the order
            s.setStudentState(StudentStates.ORGTOD);
            repo.updateStudentState(StudentStates.ORGTOD, s.getStudentID());

            //inform that the first student is already organizing the order
            firstOrganizedOrder = true;
        }

        //exit critical region
        mutex.up();
        
        //The first student will be locked here, until he is informed that
        //another student chose a chose
        courseChosen.down();
        
        //enter critical region
        mutex.down();
        
        //inform the other student that he received his order
        courseInformationAcknowledged.up();
        
        //exit critical region
        mutex.up();
    }
    
    
    /**
     * In here, a student informs the first to arrive that he has already
     * chosen a course.
     * Method invoked by all the students, except the first one to arrive.
     */
    public void informCompanion()
    {
        //enter critical region
        mutex.down();
               
        Student s = ((Student) Thread.currentThread());
       
        //unlocks the student, who is blocked in prepareOrder
        courseChosen.up();
              
        //update student state to Chatting with companions
        s.setStudentState(StudentStates.CHAWCO);
        repo.updateStudentState(StudentStates.CHAWCO, s.getStudentID());
        
        //exit critical region
        mutex.up();
        
        //waits for the first student to confirm he received his order
        courseInformationAcknowledged.down();
  
        //The student will be blocked here, until his food gets served
        served[ ((Student) Thread.currentThread()).getStudentID() ].down();
    }
    
    
    /**
     * This method guarantees that the order has been organized before the 
     * waiter is called.
     * @return <code>true</code> if the first student has already received
     * the orders from all his companions.
     */
    public boolean hasEveryBodyChosen()
    {        
        //enter critical region
        mutex.down();
            
        //define stat 
        boolean stat = false;

        //if all students chose, return true
        if(nStudentsChose == NSTUDENTS-1)
            stat = true;

        //exit critical region
        mutex.up();
        
        return stat;
    }
    
    
    /**
     * This method is invoked by the waiter, once he is called to write
     * down the student's order.
     */
    public void getThePad()
    {
        //enter critical region
        mutex.down();
        
        Waiter w = ((Waiter) Thread.currentThread());
        
        //update waiter's state to Taking the order
        w.setWaiterState(WaiterStates.TAKORD);
        repo.updateWaiterState(WaiterStates.TAKORD);
        
        
        //unlock the first student arriving at the restaurant, so that
        //he can describe him the order
        courseChosen.up();
        
        //exit critical region
        mutex.up();
        
        //block here until the student describes him order.
        orderDescribed.down();
    }
    
    
    /**
     * Method invoked by the first student to arrive, in order to describe the
     * group's order to the waiter.
     */
    public void describeTheOrder()
    {
        //student blocks here, waiting for the waiter to get his pad
        courseChosen.down();
        
        //Enter critical region
        mutex.down();
        
        //the student will unlock the waiter confirms the 
        //order was described
        orderDescribed.up();
        
        //Leave critical region
        mutex.up();
    }
    
    
    /**
     * Invoked when the first student joins his companions, after describing the 
     * order to the waiter.
     */
    public void joinTheTalk()
    {
        //enter critical region
        mutex.down();

        Student s = ((Student) Thread.currentThread());
        
        //after describing the order, the first student joins the conversation
        //and his state is changed to Chatting with companions
        s.setStudentState(StudentStates.CHAWCO);
        repo.updateStudentState(StudentStates.CHAWCO, s.getStudentID());
        
        //exit critical region
        mutex.up();
        
        //The frst student will be blocked here, until his food gets served
        served[ ((Student) Thread.currentThread()).getStudentID() ].down();
    }
    
    
    /**
     * Invoked by the waiter, when he serves a portion, unlocking a student.
     */
    public void deliverPortion()
    {
        //enter critical region
        mutex.down();
                
        //Incrementation on the number of portions served
        portionsServed++;
        
        //exit critical region
        mutex.up();
    }
    
    
    /**
     * Invoked by each the students, when starting to eat.
     */
    public void startEating()
    {
        //enter critical region
        mutex.down();
               
        //Update the student's state to enjoying the meal
        Student s = ((Student) Thread.currentThread());
        s.setStudentState(StudentStates.ENJTME);
        repo.updateStudentState(StudentStates.ENJTME, s.getStudentID());
        
        try 
        {
            //get random eating time
            Thread.sleep((long)(Math.random() * 100));
        } 
        catch (InterruptedException ex) { 
            System.err.print("Unable to make thread sleep, in the startEating() method");
            System.exit(1);
        }
        
        //exit critical region
        mutex.up();
    }
    
    
    /**
     * Invoked by each student, when they end eating their course.
     */
    public void endEating()
    {
        //enter critical region
        mutex.down();
        
        //increase the number of students who have already eaten
        NStudsEat++ ;
        
        //Update the student state to chatting with companions
        Student s = ((Student) Thread.currentThread());
        s.setStudentState(StudentStates.CHAWCO);
        repo.updateStudentState(StudentStates.CHAWCO, s.getStudentID());
        
        //exit critical region
        mutex.up();
    }
    
    
    /**
     * Invoked by each student, to verify if all the students have already eaten.
     * @return <code>true</code> if all student have eaten their course.
     */
    public boolean hasEveryBodyFinished()
    {
        //boolean containig the value to return
        boolean allHaveEaten = false;

        //enter critical region
        mutex.down();
        
        //If all student have eaten
        if (NStudsEat == NSTUDENTS)
        {
            allHaveEaten =true;
            //restart the number of student that have eaten 
            //(because of the next course)
            NStudsEat=0;
        }
        
        //exit critical region
        mutex.up();
        
        //if we are at the last coursw
        if(currentCourse == NCOURSES)
        {
            //if we are at the last course, and all students have eaten
            if (allHaveEaten)
            {
                //If the student was not the last one to arrive, he has to wake
                //him up (the last to arrive), and lock himself here, 
                //waiting for the bill to be payed
                if( ((Student) Thread.currentThread()).getStudentID() != LAST_ID)
                {
                    //unlock the last student to arrive
                    served[LAST_ID].up();
                                        
                    //student blocks himself and turns his flag to false, so that
                    //he doens't have to pay the bill
                    allHaveEaten = false;
                    served [((Student) Thread.currentThread()).getStudentID()].down();
                }
            }
            else
            {
                //If someone has not eaten yet, the student waits for him
                served [((Student) Thread.currentThread()).getStudentID()].down();
            }
            
            //IF we are at the last course, the last student to arrive will have
            //to return true sometime, so that he can pay the bill
            if(currentCourse == NCOURSES &&  ((Student) Thread.currentThread()).getStudentID() == LAST_ID )
            {
                allHaveEaten = true;
            }
        }
        
        //If we are not at the last course, and not all the students have eaten
        if(!allHaveEaten && currentCourse < NCOURSES)
        {  
            //Student will be blocked here, waiting for the waiter to bring
            //him some food
            served [((Student) Thread.currentThread()).getStudentID()].down();
        }
        
        return allHaveEaten;
    }
    
      
    /**
     * Method invoked by the last student to eat, when the meal is not over,
     * signaling to waiter to serve the next course.
     */
    public void signalTheWaiter()
    {
        //enter critical region
        mutex.down();
        
        //count the number of courses taht have been served
        currentCourse++;
        
        //unlock the waiter
        bar.signalTheWaiter();
        
        //exit critical region
        mutex.up();
        
        //the last student eating will be blocked here, waiting for the
        //waiter to serve him his food
        served [((Student) Thread.currentThread()).getStudentID()].down();
    }
    
    
    /**
     * Invoked by the waiter to test if all students have been served.
     * @return <code>true</code> if all students have been served.
     */
    public boolean haveAllClientsBeenServed()
    {
        boolean stat = false;
        
        //enter critical region
        mutex.down();
        
        //if all have been served
        if(portionsServed == NSTUDENTS )
        {
            //set the return value to true
            stat=true;
            
            //restart counter
            portionsServed = 0;
            
            //The waiter unlocks all the students
            for(int i = 0; i<NSTUDENTS; i++)
                served[i].up();
        }
        
        //If waiter have served all portions, he has to return to the bar, but 
        //get locked there, until the last student eating calls him
        if(stat)
            bar.waitForStudentToEat();
        
        //exit critical region
        mutex.up();
        
        return stat;
    }
    
    
    /**
     * Invoked by the last student arriving to the restaurant, telling 
     * the waiter to get the bill.
     */
    public void shouldHaveArrivedEarlier()
    {
        //enter critical region
        mutex.down();

        Student s = ((Student) Thread.currentThread());
        
        //call the waiter from the bar
        bar.studentReadyToPayTheBill();

        //Update the student state to paying the bill
        s.setStudentState(StudentStates.PAYTBI);
        repo.updateStudentState(StudentStates.PAYTBI, s.getStudentID());
        
        //exit critical region
        mutex.up();
        
        //waits for the waiter to arrive
        waiterPresentedTheBill.down();
    }
    
    
    /**
     * Invoked by the waiter, to present the last student arriving at the 
     * restaurant the bill. 
     */
    public void presentTheBill()
    {
        //enter critical region
        mutex.down();
        
        //unlocks the last student, who was waiting for the waiter to arrive
        waiterPresentedTheBill.up();
        
        Waiter w = ((Waiter) Thread.currentThread());

        //update waiter's state to receiving payment
        w.setWaiterState(WaiterStates.RECPAY);
        repo.updateWaiterState(WaiterStates.RECPAY);        
        
        //exit critical region
        mutex.up();
        
        //waiter get's locked here, until the last student arriving
        //pays the bill, unlocking him
        billPayed.down();
        
        //enter critical region
        mutex.down();
        
        //waiter confirms the student he has payed him the bill
        billsPaymentConfirmed.up();

        //exit critical region
        mutex.up();
    }
    
    
    /**
     * Invoked by the last student arriving the restaurant. In this method
     * the bill will be payed, releasing all the students.
     */
    public void honourTheBill()
    {
        //enter critical region
        mutex.down();
        
        //unlocks the waiter, who was waiting for the payment
        billPayed.up();
        
        //exit critical region
        mutex.up();
        
        //waiter confirms the student has paid him the bill
        billsPaymentConfirmed.down();

        //enter critical region
        mutex.down();
        
        //unlock all students, signaling them that the bill was payed
        for (int i = 0 ; i < NSTUDENTS ; i++)
            if(i != LAST_ID)
                served [i].up();
        
        //exit critical region
        mutex.up();
    }
    
    
    /**
     * Invoked by each student, when leaving the restaurant.
     */
    public void exit()
    {
        //enter critical region
        mutex.down();
        
        Student s = ((Student) Thread.currentThread());

        //Update the student state to going home
        s.setStudentState(StudentStates.GOINHO);
        repo.updateStudentState(StudentStates.GOINHO, s.getStudentID());
        
        //alert the waiter, at the bar, that a student is leaving 
        bar.studentIsLeaving();
         
        //exit critical region
        mutex.up();
    }
}
