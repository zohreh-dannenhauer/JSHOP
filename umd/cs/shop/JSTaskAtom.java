package umd.cs.shop;

import java.io.*;

import java.util.*;

public class JSTaskAtom extends JSPredicateForm

{

	/* ==== instance variables ==== */

	boolean isPrimitive;
	boolean isCompound;
	ArrayList<JSMonitor> monitors;

	public JSTaskAtom(String text) {
		super(text);
		monitors = new ArrayList<>();
		String taskName = (String) this.elementAt(0);
		if (taskName.charAt(0) == '!') {
			isPrimitive = true;
			isCompound = false;
		} else {
			isPrimitive = false;
			isCompound = true;
		}
	}

	public JSTaskAtom(StreamTokenizer token) {
		super(token);
		monitors = new ArrayList<>();
		String taskName = (String) this.elementAt(0);
		if (taskName.charAt(0) == '!') {
			isPrimitive = true;
			isCompound = false;

		} else {
			isPrimitive = false;
			isCompound = true;

		}
	}

	JSTaskAtom() {
		super();
		monitors = new ArrayList<>();
	}

	public JSPairPlanTState seekSimplePlan(JSPlanningDomain dom, JSTState ts, int depth) {
		JSPairPlanTState pair;
		JSSubstitution alpha;
		JSOperator op;
		JSListOperators list = dom.operators();
		JSTaskAtom top;
		JSState ns;
		JSTState tState;
		JSPlan pl = new JSPlan();
		boolean thisIsGround = this.isGround();
		monitors = new ArrayList<>();

		for (short i = 0; i < list.size(); i++) {
			op = (JSOperator) list.elementAt(i);
			top = op.head();
			if (!thisIsGround) {
				top = top.standarizerTA();
			}
			alpha = top.matches(this);
			if (!alpha.fail()) {
				op = op.standarizerOp();
				alpha = alpha.standarizerSubs();
				JSJshopVars.VarCounter++;
				// create monitor
				JSTaskAtom tmp = top.applySubstitutionTA(alpha);
				JSListLogicalAtoms opDelL = op.deleteList().applySubstitutionListLogicalAtoms(alpha);
				CreateMonitors(monitors, opDelL, ts, depth);

				for (int j = 0; j < monitors.size(); j++) {
					JSMonitor m = monitors.get(j);
					m.start();
					System.out.println("RUNNING!");
				}

				JSListSubstitution satisfiers = dom.axioms().TheoremProver(op.precondition(), ts.state(), alpha, true);
				if (!satisfiers.isEmpty()) {

					tState = ts.state().applyOp(op, alpha, ts.addList(), ts.deleteList());
					ns = tState.state();
					top = op.head();
					pl.addWithCost(top.applySubstitutionTA(alpha), op.cost());
					return new JSPairPlanTState(pl, tState);
				}
			}
		}
		pl.assignFailure();
		return new JSPairPlanTState(pl, new JSTState());

	}

	private ArrayList<JSMonitor> CreateMonitors(ArrayList<JSMonitor> monitor_list, JSListLogicalAtoms deleteList, JSTState ts, int depth) {
		
		
		for (short i = 0; i < deleteList.size(); i++) {
		
			JSPredicateForm item1 = (JSPredicateForm) deleteList.elementAt(i);
			int alreadyExixted = 0;
			
			//check to see if the monitor already existed:
			for (JSMonitor m : monitor_list) {
				if(m.op_pre.equals(item1)){
					alreadyExixted = 1;
					break;
				}
			}
		
			if(alreadyExixted == 0){
				String str = (String) item1.elementAt(0);
				// JSState s = ts.state;
				JSMonitor m = new JSMonitor("monitor_" + str, ts.state, item1, depth);
				
				monitors.add(m);
			}
			//
			// t.start();
			//
			//
			// String str = (String) item1.elementAt(0);
			//
			// if (str.equalsIgnoreCase("not"))
			// {
			// JSPredicateForm el;
			// el = (JSPredicateForm) this.elementAt(1);
			//// res.append(el.toStr());
			// }
			// else
			// { //Additions end
			// JSTerm el;
			// for (short j = 1; j < this.size(); j++)
			// {
			// el = (JSTerm) this.elementAt(j);
			//// res.append(el.toStr());
			// }
			//
			// }
			// }
		}
		return monitors;
	}

	public JSReduction reduce(JSPlanningDomain dom, JSState s, JSReduction red) {

		// JSUtil.flag("reduce(PlanningDomain dom,State s,Reduction red)");

		JSListMethods mets = dom.methods();

		return mets.findReduction(this, s, red, dom.axioms());

	}

	public JSTaskAtom applySubstitutionTA(JSSubstitution alpha) {
		JSTaskAtom nta = new JSTaskAtom();

		/*
		 * if (JSJshopVars.flagLevel > 8 && JSJshopVars.flagPlanning ){
		 * JSUtil.print("applyTA subst:"); alpha.print(); JSUtil.print("to:");
		 * this.print(); }
		 */
		/*
		 * if (JSJshopVars.flagLevel > 9 && JSJshopVars.flagPlanning )
		 * JSUtil.flag("stop");
		 */

		if (this.isPrimitive()) {
			nta.makePrimitive();
		} else {
			nta.makeCompound();
		}

		nta.addElement(this.elementAt(0));
		JSTerm ti, te;

		/*
		 * if (JSJshopVars.flagLevel > 8 && JSJshopVars.flagPlanning )
		 * nta.print();
		 */
		/*
		 * if (JSJshopVars.flagLevel > 9 && JSJshopVars.flagPlanning )
		 * JSUtil.flag("<--");
		 */
		for (short i = 1; i < this.size(); i++) {
			/*
			 * if (JSJshopVars.flagLevel > 8 && JSJshopVars.flagPlanning )
			 * nta.print();
			 */
			/*
			 * if (JSJshopVars.flagLevel > 9 && JSJshopVars.flagPlanning )
			 * JSUtil.flag("<--");
			 */
			ti = (JSTerm) this.elementAt(i);
			// nta.addElement(ti.applySubstitutionT(alpha));
			// Added 11/29/2000
			te = ti.applySubstitutionT(alpha);
			nta.addElement(te.call());
			// Additions ended
		}

		/*
		 * if (JSJshopVars.flagLevel > 8 && JSJshopVars.flagPlanning )
		 * nta.print(); if (JSJshopVars.flagLevel > 9 &&
		 * JSJshopVars.flagPlanning ) JSUtil.flag("<-- final applyTA");
		 */
		return nta;
	}

	public JSTaskAtom cloneTA() {
		JSTaskAtom nTA = new JSTaskAtom();

		if (this.isPrimitive()) {
			nTA.makePrimitive();
		} else {
			nTA.makeCompound();
		}

		nTA.addElement(this.elementAt(0));
		JSTerm ti;

		for (short i = 1; i < this.size(); i++) {
			ti = (JSTerm) this.elementAt(i);
			nTA.addElement(ti.cloneT());
		}

		return nTA;

	}

	public boolean isGround() {
		JSTerm ti;

		for (short i = 1; i < this.size(); i++) {
			ti = (JSTerm) this.elementAt(i);
			if (!ti.isGround()) {
				return false;
			}
		}

		return true;

	}

	public JSTaskAtom standarizerTA() {
		JSTaskAtom nTA = new JSTaskAtom();

		if (this.isPrimitive()) {
			nTA.makePrimitive();
		} else {
			nTA.makeCompound();
		}

		nTA.addElement(this.elementAt(0));
		JSTerm ti;

		for (short i = 1; i < this.size(); i++) {
			ti = (JSTerm) this.elementAt(i);
			nTA.addElement(ti.standardizerTerm());
		}

		return nTA;

	}

	JSJshopNode findInList(Vector list) {
		JSJshopNode node;

		for (int i = 0; i < list.size(); i++) {
			node = (JSJshopNode) list.elementAt(i);
			if (this.equals(node.atom())) {
				return node;
			}
		}

		return new JSJshopNode(this, new Vector());
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public boolean isCompound() {
		return isCompound;
	}

	public void makePrimitive() {
		isPrimitive = true;
		isCompound = false;
	}

	public void makeCompound() {
		isPrimitive = false;
		isCompound = true;
	}

}
