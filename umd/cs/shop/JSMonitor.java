package umd.cs.shop;

import java.util.*;
import java.io.*;

public class JSMonitor extends Thread {

	/* ==== instance variables ==== */

	private JSTaskAtom head;
	public Thread t;
	public String name;
	public int depth;
	public boolean isFired;
	JSState state;
	JSPredicateForm op_pre;
	static boolean flag = true;
	JSMonitor(String t_name, JSState st, JSPredicateForm op, int d) {
		name = t_name;
		state = st;
		op_pre = op;
		depth = d;
		isFired = false;

		System.out.println("Creating " + name);
	}

	JSMonitor() {
		super();

	}

	public void run() {
		System.out.println("Running " + name);

		// check if the state is equal to it!
		String str = (String) this.op_pre.elementAt(0);
		
		while (this.isFired == false) {
			try {
				Thread.sleep(1);

				if (!(this.state.contains(this.op_pre))) {
					// if this change it is cut?
					this.isFired = true;

					System.out.println("monitor fires!!");
					break;
				}
				if (flag == true && depth == 10 && str.equals("clear") && !this.isFired) {
					JSPredicateForm s1 = new JSPredicateForm("(clear b5)");
					JSPredicateForm s2 = new JSPredicateForm("(on b5 b3)");
					JSPredicateForm s3 = new JSPredicateForm("(clear b3)");

					System.out.println("state change, b3 get not clear");
					this.state.add(s1);
					this.state.add(s2);
					this.state.remove(s3);
					this.isFired = true;
					flag = false;
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void start() {
		System.out.println("Starting " + name);
		if (t == null) {
			t = new Thread(this, name);
			t.start();
		}
	}

	public void print() {

		// JSUtil.flag("<-- method");
	}
	
	public String reasoning(){
		return "Add";
	}


}
