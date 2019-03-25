package CommonInfra;

/**
 *    Generic FIFO derived from a generic memory.
 */

public class MemFIFO extends MemObject
{
    /**
     *   Pointer to the first empty location.
     */
    
    private int inPnt;
    
    /**
     *   Pointer to the first occupied location.
     */
    
    private int outPnt;
    
    /**
     *   Signaling FIFO empty state.
     */
    
    private boolean empty;
    
    /**
     *   FIFO instantiation.
     *   The instantiation only takes place if the FIFO size is meaningful (greater than zero).
     *   No error is reported.
     *
     *     @param nElem FIFO size
     */
    
    public MemFIFO (int nElem)
    {
        super (nElem);
        inPnt = outPnt = 0;
        empty = true;
    }
    
    /**
     *   FIFO insertion.
     *   A generic object is written into it.
     *   If the FIFO is full, nothing happens. No error is reported.
     *
     *    @param val generic object to be written
     */
    
    @Override
    public synchronized void  write (Object val)
    {
        //if the FIFO is full it gets blocked here
        while ((inPnt == outPnt) && !empty)
            try {
                wait();
            } catch (InterruptedException e)
            {}
        
        mem[inPnt] = val;
        inPnt = (inPnt + 1) % mem.length;
        empty = false;
        
        notifyAll();
        
    }
    
    /**
     *   FIFO retrieval.
     *   A generic object is read from it.
     *   If the FIFO is empty, <code>null</code> is returned. No error is reported.
     *
     *    @return first generic object that was written
     */
    
    @Override
    public synchronized Object read ()
    {
        Object val = null;                                    // default returned object
        
        //if the FIFO is empty it gets blocked here
        while(empty)
            try {
                wait();
            } catch (InterruptedException e)
            {}
        
        val = mem[outPnt];
        outPnt = (outPnt + 1) % mem.length;
        empty = (inPnt == outPnt);
        
        notifyAll();
        
        return val;
    }
}