package pacote;
import java.util.Vector;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class Progression{

	Vector<Action> actionSet;		
	BDD goal;
	BDD initialState;
	BDD constraints;
	
	/* Constructor */
	public Progression(ModelReader model) {
		this.actionSet = model.getActionSet();
		this.initialState = model.getInitialStateBDD();
		this.goal = model.getGoalSpec();		
		this.constraints = model.getConstraints();
	}	
	
	/* Foward search from the initial state, towards a goal state. */
	public boolean planForward(){
		System.out.println("initial: " + initialState);
		System.out.println("goal: " + goal);
		BDD reached = initialState.id(); //accumulates the reached set of states.
		BDD Z = reached.id(); // Only new states reached	
		BDD aux;	
		int i = 0;
				
		while(Z.isZero() == false){
			System.out.println(i);
			aux = Z.and(goal.id());	

			 if (aux.toString().equals("") == false) {
			 	System.out.println("The problem is solvable.");	
			 	return true;
			 }
			
			aux.free();

			Z = progression(Z); 
			Z = Z.apply(reached, BDDFactory.diff); // The new reachable states in this layer
			reached = reached.or(Z); //Union with the new reachable states
			reached = reached.and(constraints);
			i++;
		}
		
		System.out.println("The problem is unsolvable.");
		
		return false;
	}
		
	/* Deterministic Progression of a formula by a set of actions */
	public BDD progression(BDD formula){
		BDD reg = null;	
		BDD teste = null;
		for (Action a : actionSet) {
			teste = progressionQbf(formula,a);
			teste = teste.and(constraints);
			if(reg == null){
				reg = teste;
			}else{
				reg.orWith(teste);
			}	
		}
		return reg;
	}
	
	/* Propplan progression based on action: Qbf based computation */
	public BDD progressionQbf(BDD Y, Action a) {
		BDD reg;
		reg = Y.and(a.getPrecondition()); //(Y ^ effect(a))
		
		if(reg.isZero() == false){
//			System.out.println("Ação aplicável: " + a.getName());
			reg = reg.exist(a.getChange()); //qbf computation
			reg = reg.and(a.getEffect()); //precondition(a) ^ E changes(a). test
			reg = reg.and(constraints);
		}
		return  reg;
 	}
}
	