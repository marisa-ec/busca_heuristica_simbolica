package pacote;
import java.io.IOException;
import java.util.Vector;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class Regression{
	
	Vector<Action> actionSet;		
	BDD goalState;
	BDD initialState;
	BDD constraints;
	Vector<BDD> BDDHValues;
	
	/* Constructor */
	public Regression(ModelReader model) {
		this.actionSet = model.getActionSet();
		this.initialState = model.getInitialStateBDD();
		this.goalState = model.getGoalSpec();		
		this.constraints = model.getConstraints();
	}	
	
	
	/* Backward search from the goal state, towards initial state. */
	public boolean planBackward() throws IOException{
		BDD reached = goalState.id(); //accumulates the reached set of states.
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
		int j = 0;
		BDD reached = goalState.id(); //accumulates the reached set of states.
		BDDHValues.add(j, goalState);
		
		BDD Z = reached.id(); // Only new states reached	
		BDD aux;	
		int i = 1;
				
		while(Z.isZero() == false){
			j++; //index do vetor de BDDs com valor heurístico
			System.out.println(i); 
			
			aux = Z.and(initialState.id());
			
			if (aux.toString().equals("") == false) {
				System.out.println("The problem is solvable.");	
				return true;
			}
			
			aux.free();
			
			Z = regression(Z);
			Z = Z.apply(reached, BDDFactory.diff); // The new reachable states in this layer
			//adicionar o Z na posição i do vetor.
			BDDHValues.add(j, Z);
			
			reached = reached.or(Z); //Union with the new reachable states
			reached = reached.and(constraints);
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
//			System.out.println("Ação aplicável: " + a.getName());
			reg = reg.exist(a.getRelaxChange()); //qbf computation				
			reg = reg.and(a.getPrecondition()); //precondition(a) ^ E changes(a). test
			reg = reg.and(constraints);
		}
		return  reg;
 	}
	
}
	