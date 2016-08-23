package umd.cs.shop;



public class Monitor extends Thread {
	   private Thread t;
	   private String threadName;
	   JSState state;
	   JSPredicateForm op_pre;
	   
	   Monitor( String name, JSState st, JSPredicateForm op){
	       threadName = name;
	       state = st; 
	       op_pre = op;
	       
	       System.out.println("Creating " +  threadName );
	   }
	   public void run() {
	      System.out.println("Running " +  threadName );
	      try {
	         for(int i = 4; i > 0; i--) {
	            System.out.println("Thread: " + threadName + ", " + i);
	            // Let the thread sleep for a while.
	            Thread.sleep(50);
	         }
	     } catch (InterruptedException e) {
	         System.out.println("Thread " +  threadName + " interrupted.");
	     }
	     System.out.println("Thread " +  threadName + " exiting.");
	   }
	   
	   public void start ()
	   {
	      System.out.println("Starting " +  threadName );
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }

	}

	 class TestThread {
	   public static void main(String args[]) {
	   
//	      Monitor T1 = new Monitor( "Thread-1");
//	      T1.start();
//	      
//	      Monitor T2 = new Monitor( "Thread-2");
//	      T2.start();
	   }   
	}