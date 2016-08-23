package umd.cs.shop;

import java.util.*;
import java.io.*;

public class JSTasks extends JSListLogicalAtoms

{

	/* ==== instance variables ==== */

	private boolean fail;

	JSTasks() {
		super();
	}

	JSTasks(StreamTokenizer tokenizer) {
		super();
		JSTaskAtom ta;
		JSUtil.flagParser("in ListTasks()");

		if (!JSUtil.readToken(tokenizer, "List of tasks"))
			throw new JSParserError(); // return;

		/* If this is an empty list "nil" */
		if ((tokenizer.ttype == tokenizer.TT_WORD) && (tokenizer.sval.equalsIgnoreCase("nil")))
			return;

		tokenizer.pushBack();

		if (!JSUtil.expectTokenType(JSJshopVars.leftPar, tokenizer, " Expecting '(' "))
			throw new JSParserError(); // return;

		if (!JSUtil.readToken(tokenizer, "Expecting list of tasks"))
			throw new JSParserError(); // return;

		while (tokenizer.ttype != JSJshopVars.rightPar) {
			tokenizer.pushBack();
			ta = new JSTaskAtom(tokenizer);
			if (ta.size() != 0) {
				this.addElement(ta);
			} else {
				JSUtil.println("Line: " + tokenizer.lineno() + " parsing list of tasks: unexpected atom");
				throw new JSParserError(); // return;
			}
			if (!JSUtil.readToken(tokenizer, "Expecting ')'"))
				throw new JSParserError(); // return;
		}

		// JSUtil.flagParser("ListTasks parse succesful");
	}

	public JSPairPlanTState seekPlan(JSTState ts, JSPlanningDomain dom, JSPlan pl, Vector listNodes, int depth) {

		JSPlan ans;
		JSPairPlanTState pair;
		JSPlan sol;

		if (this.isEmpty()) {
			return new JSPairPlanTState(pl, ts);
		}

		JSTaskAtom t = (JSTaskAtom) this.firstElement();
		JSTasks rest = (JSTasks) this.cdr();
		rest.removeElement(t);

		if (t.isPrimitive()) {
			pair = t.seekSimplePlan(dom, ts, depth);

			// Generate Monitors for ts.addList here

			ans = pair.plan();
			if (ans.isFailure()) {
				return pair; // failure
			} else {
				pl.addElements(ans);
				listNodes.addElement(new JSJshopNode(t, new Vector()));
				return rest.seekPlan(pair.tState(), dom, pl, listNodes, depth + 1);
			}
		} else {
			JSJshopNode node;
			JSReduction red = new JSReduction();
			red = t.reduce(dom, ts.state(), red); // counter to iterate
													// on all reductions

			JSTasks newTasks;
			JSMethod selMet = red.selectedMethod();
			while (!red.isDummy()) {
				newTasks = (JSTasks) red.reduction();
				node = new JSJshopNode(t, newTasks.cloneTasks());

				// JSUtil.flag("<- tasks");
				newTasks.addElements(rest);
				pair = newTasks.seekPlan(ts, dom, pl, listNodes, depth + 1);
				if (!pair.plan().isFailure()) {
					// JSUtil.flag("reduced");
					listNodes.addElement(node);
					return pair;
				}
				// JSUtil.flag("iterating");
				red = t.reduce(dom, ts.state(), red);
				selMet = red.selectedMethod();
			}
		}
		ans = new JSPlan();
		ans.assignFailure();
		return new JSPairPlanTState(ans, new JSTState());

	}

	/*****************************************************************/
	// Multi plan generator
	/**
	 * @param ts
	 ****************************************************************/

	public JSMonitor checkForFiredMonitors(ArrayList<JSMonitor> monitors_for_task, JSTState ts, int depth) {

		for (Iterator it = monitors_for_task.iterator(); it.hasNext();) {
			JSMonitor mntr = (JSMonitor) it.next();
			System.out.println(mntr.name);
			if (mntr.isFired)
				return mntr;
		}
		return null;
	}
	static JSMonitor fired_mntr;
	static ArrayList<JSTasks> list = new ArrayList<>();
	
	public JSListPairPlanTStateNodes seekPlanAll(JSTState ts, JSPlanningDomain dom, boolean All, int depth) {
		JSListPairPlanTStateNodes results, plans = new JSListPairPlanTStateNodes();

		JSPairPlanTSListNodes ptl;
		JSPlan ans;
		JSPairPlanTState pair;
		JSPlan sol;
		JSJshopNode node;
		Vector listnodes;
		JSTaskAtom ta;
		JSTState tts;
		System.out.println("depth: " + depth);

		System.out.println("_______");
		fired_mntr = checkForFiredMonitors(dom.monitors, ts, depth);
		if (fired_mntr != null) {
			System.out.println("a monitor fires! Return fail!");
			if (fired_mntr.depth < depth) {
				System.out.println("should return to: "+fired_mntr.depth);
				
				
				return plans;
			} else {
				System.out.println("here!");
				fired_mntr.isFired = false;
				ts.state = fired_mntr.state;
				dom.monitors.remove(fired_mntr);
				System.out.println("state is updated at depth " + depth);
			}
		} else {
			if (this.isEmpty()) {
				if (JSJshopVars.flagLevel > 1)
					JSUtil.println("Returning successfully from find-plan : No more tasks to plan");

				pair = new JSPairPlanTState((new JSPlan()), ts);
				ptl = new JSPairPlanTSListNodes(pair, new Vector());
				plans.addElement(ptl);
				return plans;
			}

			JSTaskAtom t = (JSTaskAtom) this.firstElement();
			t.print();
			if (JSJshopVars.flagLevel > 2) {
				JSUtil.println(" ");
				JSUtil.print("Searching a plan for");

			}
			JSTasks rest = (JSTasks) this.cdr();

			if (t.isPrimitive()) {
				pair = t.seekSimplePlan(dom, ts, depth);
				// assign the depth to each monitor
				for (Iterator iterator = t.monitors.iterator(); iterator.hasNext();) {
					JSMonitor m = (JSMonitor) iterator.next();
					
					dom.monitors.add(m);
				}

				ans = pair.plan();
				if (ans.isFailure()) {
					System.out.println("failure!");
					if (JSJshopVars.flagLevel > 1)
						JSUtil.println("Returning failure from find-plan: Can not find an operator");
					return plans; // failure - empty list
				}

				results = rest.seekPlanAll(pair.tState(), dom, All, depth + 1);

				if (results.isEmpty()){
					if(fired_mntr != null && fired_mntr.depth > depth){
						System.out.println("d" + depth);
						//I just added this
						//the state should be changed
						//if it is primitive go up!
						dom.monitors.remove(fired_mntr);
						pair.tState.state = fired_mntr.state;
						fired_mntr = null;
						rest.print();
						results = rest.seekPlanAll(pair.tState(), dom, All, depth + 1);
						
					}
					else
						return plans;
				}

				ta = (JSTaskAtom) ans.elementAt(0);
				node = new JSJshopNode(t, new Vector());

				for (int i = 0; i < results.size(); i++) {
					ptl = (JSPairPlanTSListNodes) results.elementAt(i);
					ptl.planS().plan().insertWithCost(0, ta, ans.elementCost(0));
					ptl.listNodes().insertElementAt(node, 0);
					plans.addElement(ptl);
					
				}
				
				return plans;
			}

			JSAllReduction red = new JSAllReduction();
			red = dom.methods().findAllReduction(t, ts.state(), red, dom.axioms());
			JSTasks newTasks;
			JSMethod selMet = red.selectedMethod();
			if (JSJshopVars.flagLevel > 1 && red.isDummy())
				System.out.println("Returning failure from find-plan: Can not find an applicable method");
			while (!red.isDummy()) {
				if (JSJshopVars.flagLevel > 4) {
					JSUtil.println("The reductions are: ");
					System.out.println("The reductions are: ");
					red.printReductions();
				}

				for (int k = 0; k < red.reductions().size(); k++) {

					newTasks = (JSTasks) red.reductions().elementAt(k);
					node = new JSJshopNode((JSTaskAtom) t.clone(), newTasks.cloneTasks());
					newTasks.addElements(rest);
					results = newTasks.seekPlanAll(new JSTState(ts), dom, All, depth + 1);

					if (results.isEmpty()){
						if(fired_mntr != null && fired_mntr.depth > depth+1){
							System.out.println("d" + depth);
							//call it with the parent!
							results = newTasks.seekPlanAll(new JSTState(ts), dom, All, depth + 1);
							fired_mntr = null;
						}
						continue;
					}

					for (int j = 0; j < results.size(); j++) {
						ptl = (JSPairPlanTSListNodes) results.elementAt(j);
						ptl.listNodes().addElement(node);
						plans.addElement(ptl);
						if (plans.size() >= 1 && !All)
							return plans;
					}
				}
				red = dom.methods().findAllReduction(t, ts.state(), red, dom.axioms());
				selMet = red.selectedMethod();
				
			}
		}
		return plans;

	}

	/*****************************************************************/

	public boolean fail() {
		return fail;
	}

	public void makeFail() {
		fail = true;
	}

	public void makeSucceed() {
		fail = false;
	}

	public JSTasks applySubstitutionTasks(JSSubstitution alpha) {
		JSTasks nt = new JSTasks();
		JSTaskAtom ti;
		JSTaskAtom nti;

		for (short i = 0; i < this.size(); i++) {
			ti = (JSTaskAtom) this.elementAt(i);
			// ti.print();
			nti = ti.applySubstitutionTA(alpha);
			// nti.print();
			nt.addElement(nti);
			// nt.print();
			// JSUtil.flag("<-- applyJSTasks");
		}

		/*
		 * nt.print(); // JSUtil.flag("<-- final applyTasks");
		 */
		return nt;
	}

	public boolean contains(JSTaskAtom t) {
		JSTaskAtom el;

		for (int i = this.size() - 1; i > -1; i--) {
			el = (JSTaskAtom) this.elementAt(i);

			/*
			 * JSUtil.print("JSTaskAtom:"); el.print(); JSUtil.print(" equals: "
			 * ); t.print(); JSUtil.flag("?");
			 */

			if (t.equals(el)) {
				// JSUtil.flag("YES");
				return true;
			}
		}
		// JSUtil.flag("NO");
		return false;
	}

	public JSTasks cloneTasks() {
		JSTasks newTs = new JSTasks();
		JSTaskAtom t;

		for (short i = 0; i < this.size(); i++) {
			t = (JSTaskAtom) this.elementAt(i);
			newTs.addElement(t.cloneTA());
		}
		return newTs;

	}

	public JSTasks cdr() {
		JSTasks newTs = new JSTasks();
		JSTaskAtom t;

		for (short i = 1; i < this.size(); i++) {
			t = (JSTaskAtom) this.elementAt(i);
			newTs.addElement(t);
		}
		return newTs;

	}

	public JSTasks standarizerTasks() {
		JSTasks newTs = new JSTasks();
		JSTaskAtom t;

		for (short i = 0; i < this.size(); i++) {
			t = (JSTaskAtom) this.elementAt(i);
			newTs.addElement(t.standarizerTA());
		}
		return newTs;

	}
}
