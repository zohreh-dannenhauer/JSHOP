package umd.cs.shop;
import java.io.*;
import java.util.*;

public class JSTester
{
	private boolean pass;
	private JSTaskAtom fault; 
	
	JSTester()
	{
	}
	
	JSTester(JSPlanningDomain dom, JSPlanningProblem prob, JSPlan sol)
	{
		pass = test(dom, prob, sol);
	}
	
	private boolean test (JSPlanningDomain dom, JSPlanningProblem prob, JSPlan sol)
	{
		System.out.println("(Tester): Enter test");
		int size = 0, pos = 0;
		boolean found = false;
		JSTaskAtom t = new JSTaskAtom();
		JSState curState = new JSState();
		JSTState newState = new  JSTState();
		JSSubstitution resultSub = new JSSubstitution();

		size = sol.size();
		System.out.println("(Tester): Size of plan: " + size);
		curState = prob.state();

		for (int i = 0; i < size; i++)
		{
			t = (JSTaskAtom)sol.elementAt(i);
			found = false;
			for (int j = 0; j < dom.operators().size(); j++)
			{
				if (t.elementAt(0).toString().equals(((JSOperator)dom.operators().elementAt(j)).head().elementAt(0).toString()))
				{
					pos = j;
					System.out.println("(Tester): Task head: " + t.elementAt(0).toString());
					System.out.println("(Tester): Dom head: " + ((JSOperator)dom.operators().elementAt(j)).head().elementAt(0).toString());
					resultSub = (curState.satisfies(((JSOperator)dom.operators().elementAt(pos)).precondition(), new JSSubstitution(), dom.axioms()));
					if (!resultSub.fail()) 
					{
						newState = curState.applyOp(((JSOperator)dom.operators().elementAt(pos)), resultSub, 
													new JSListLogicalAtoms(), new JSListLogicalAtoms());
						curState = newState.state();
						System.out.println("(Tester): Op satisfies");
						found = true;
						j = dom.operators().size();
					}
				}
				if (!found && j == (dom.operators().size()-1))
				{
					fault = t;
					return (false);
				}
			}
		}
		
		
		return (true);
	}
	
	public boolean pass()
	{
		return pass;
	}
	
	public JSTaskAtom fault()
	{
		return fault;
	}
	
}
