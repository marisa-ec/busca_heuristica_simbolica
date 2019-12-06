package pacote;
import java.io.IOException;
import java.util.Vector;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class Search{

	Vector<Action> actionSet;		
	BDD goal;
	BDD initialState;
	BDD constraints;
	int numProp;
	Vector<BDD> BDDHValues = new Vector<BDD>();

	/* Constructor */
	public Search(ModelReader model) {
		this.actionSet = model.getActionSet();
		this.initialState = model.getInitialStateBDD();
		this.goal = model.getGoalSpec();		
		this.constraints = model.getConstraints();
		this.numProp = model.getPropNum();
	}
	
	public void heuristcSearch(ModelReader model) throws IOException {
		if(heuristicPlanBackward() == true) {
			heuristicPlanForward();
		}
	}
	
	/* Forward search from the initial state, towards a goal state. */
	public boolean heuristicPlanForward(){
	//	System.out.println("initial: " + initialState);
	//	System.out.println("goal: " + goal);
		BDD reached = initialState.id(); //accumulates the reached set of states.
		BDD Z = reached.id(); // Only new states reached	
		BDD aux;
		BDD teste;
		int i = 0;
		
		System.out.println("Progressive search");
				
		while(Z.isZero() == false){
			System.out.println(i);
			aux = Z.and(goal.id());	
	

			 if (aux.toString().equals("") == false) {
			 	System.out.println("The problem is solvable.");	
			 	return true;
			 }			
			aux.free();

			/*chamar a progressão só para o BDD retornado pela função minHValue*/
			teste = minHvalue(BDDHValues, Z);
			Z = progression(teste); //Z = progression(teste);
						
			Z = Z.apply(reached, BDDFactory.diff); // The new reachable states in this layer
			reached = reached.or(Z); //Union with the new reachable states
			reached = reached.and(constraints);
//			if(i < 4){
//				System.out.println(i + "\n" + reached);
//			}
			i++; //g(n)
		}
		
		
		
		
		System.out.println("The problem is unsolvable.");
		
		return false;
	}
	
	/*Consult a tabela de valores heurísticos e 
	 * retorna o subconjunto de estados (BDD result) com menos valor heurístico*/	
	public BDD minHvalue(Vector<BDD> H, BDD X) {
		BDD result;
		int i = 0;
		while(i < H.size()) {
			result = X.and(H.get(i)); 
			if(result.isZero() == false) {
				return result;
			}
			++i;
		}
		return null;
	}
	
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
//			if(i < 4){
//				System.out.println(i + "\n" + reached);
//			}
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
	
	
	
	/* Backward search from the goal state, towards initial state. */
	public boolean planBackward() throws IOException{
		BDD reached = goal.id(); //accumulates the reached set of states.
		BDD Z = reached.id(); // Only new states reached	
		BDD aux;	
		int i = 0;
				
		while(Z.isZero() == false){
			System.out.println(i);
			
			aux = Z.and(initialState.id());
			
			if (aux.toString().equals("") == false) {
				System.out.println("The problem is solvable.");	
				return true;
			}
			
			
			aux.free();
			
			Z = regression(Z);
			Z = Z.apply(reached, BDDFactory.diff); // The new reachable states in this layer
			reached = reached.or(Z); //Union with the new reachable states
			reached = reached.and(constraints);
			i++;				
		}
		
		System.out.println("The problem is unsolvable.");
		return false;
	}
	
	/*Busca regressiva no problema relaxado (sem efeitos negativos)*/
	/*Ao computar os estados regredidos, colocar os novos estados alcançados em um vetor de BDDs*/
	public boolean heuristicPlanBackward() throws IOException{
		//System.out.println("Performing heuristic search in a relaxed problem");
		System.out.println("initial: " + initialState);
		System.out.println("goal: " + goal);
		
		int j = 0;
		BDD reached = goal.id(); //accumulates the reached set of states.
		BDDHValues.add(j, goal);
		
		BDD Z = reached.id(); // Only new states reached	
		BDD aux;	
		int i = 1;
		System.out.println("Heuristic computation");
		
		while(Z.isZero() == false){
			//System.out.println(BDDHValues);
			j++; //index do vetor de BDDs com valor heurístico
			System.out.println(i); 
			
			aux = Z.and(initialState.id());
			
			if (aux.toString().equals("") == false) {
				System.out.println("END");
				//System.out.println("The problem is solvable.");	
				return true;
			}
			
			aux.free();
			//System.out.println("Z [antes da regression]" + Z);
			Z = heuristicRegression(Z);
			//System.out.println("Z-->" + Z);
		    //System.out.println("Z [depois da regression]" + Z);
			Z = Z.apply(reached, BDDFactory.diff); // The new reachable states in this layer
			//adicionar o Z na posição i do vetor.
			//System.out.println("Z-->" + Z);
			BDDHValues.add(j,Z);
			
			reached = reached.or(Z); //Union with the new reachable states
			reached = reached.and(constraints);
//			if(i < 4){
//				System.out.println(reached);
//			}
			i++;
			
		}
		
		System.out.println("The problem is unsolvable.");
		return false;
	}
	
	public Vector<BDD> getBDDHValues() {
		return BDDHValues;
	}
	
	
	/* Deterministic Regression of a formula by a set of actions */
	public BDD regression(BDD formula){
		BDD reg = null;	
		BDD teste = null;
		for (Action a : actionSet) {
			teste = regressionQbf(formula,a);
			teste = teste.and(constraints);
			if(reg == null){
				reg = teste;
			}else{
				reg.orWith(teste);
			}	
		}
		return reg;
	}
	
	public BDD heuristicRegression(BDD formula){
		BDD reg = null;	
		BDD teste = null;
		for (Action a : actionSet) {
			//System.out.println(a.getName());
			teste = heuristicRegressionQbf(formula,a);
			teste = teste.and(constraints);
			if(reg == null){
				reg = teste;
			}else{
				reg.orWith(teste);
			}	
		}
		return reg;
	}
	
	/* Propplan regression based on action: Qbf based computation */
	public BDD regressionQbf(BDD Y, Action a) {
		BDD reg;
		reg = Y.and(a.getEffect()); //(Y ^ effect(a))
		
		if(reg.isZero() == false){
//			System.out.println("Ação aplicável: " + a.getName());
			reg = reg.exist(a.getChange()); //qbf computation				
			reg = reg.and(a.getPrecondition()); //precondition(a) ^ E changes(a). test
			reg = reg.and(constraints);
		}
		return  reg;
 	}
	
	
	public BDD heuristicRegressionQbf(BDD Y, Action a) {
		BDD reg;
		reg = Y.and(a.getRelaxEffect()); //(Y ^ effect(a))
		
		if(reg.isZero() == false){
//		System.out.println("Ação aplicável: " + a.getName());
//		System.out.println("precondição: " + a.getPrecondition());
//		System.out.println("efeitos" + a.getEffect());
			reg = reg.exist(a.getRelaxChange()); //qbf computation	
			reg = reg.and(a.getPrecondition()); //precondition(a) ^ E changes(a). test
			//System.out.println(reg + "\n");
			reg = reg.and(constraints);
			
			}
		return  reg;
 	}
}