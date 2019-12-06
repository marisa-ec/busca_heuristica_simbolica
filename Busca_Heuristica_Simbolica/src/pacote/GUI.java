package pacote;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Scanner;

/* Implementation of the algorithms that performs backward planning, using regression.
 * The method proposed by [Fourmann, 2000] and the method proposed  by "[Ritanen, 2008]" */

public class GUI {
	/* The main method receives a file containing the description of the planning  domain\problem 
	 * and calls the backward search. */
	public static void main(String[] args) throws IOException{
		//File containing the description of the planning domain-problem
		
//		String fileName = "grounded_25.txt";
		//String fileName = "rovers-01-GROUNDED.txt";
		//String fileName = "storage-01-grounded.txt"; 
		
		Runtime runtime = Runtime.getRuntime();
		long initmemory = runtime.totalMemory() - runtime.freeMemory();
		int t;
			
		String fileName;
		if(args.length == 0) {
			Scanner ler = new Scanner(System.in);
			System.out.printf("Informe o número para busca: 0 - exaustiva | 1 - heuristica\n");
			t = ler.nextInt();
			int p;	
			
			System.out.printf("Problema:\n");
			p = ler.nextInt();
			//fileName = "LOGISTICS-" + p + "-0-GROUNDED.txt"; 
			//fileName = "rovers-" + p + "-0-GROUNDED.txt"; 	
			fileName = "rovers-0" + p + "-GROUNDED.txt"; 	
		} else {
			t = Integer.parseInt(args[0]);
			//fileName = "LOGISTICS-" + args[1] + "-0-GROUNDED.txt";
			fileName = "rovers-0" + args[1] + "-GROUNDED.txt"; 
		}
		
		
		
//		String fileName = "LOGISTICS-14-0-GROUNDED.txt"; 		

		
		String type = "propplan"; //"ritanen" or "propplan"
		
		int nodenum = 50000000;
		int cachesize =  5000000;
		
		ModelReader model = new ModelReader();	
		model.fileReader(fileName, type, nodenum, cachesize);

			
		
		
		
		System.out.println(fileName.substring(fileName.lastIndexOf("/") + 1,fileName.lastIndexOf(".")));
		
		// 0 - exaustiva | 1 - heuristica
		
	//	int t = 0;
		
		if(t == 0) {
			System.out.println("Exhaustive search");
			System.out.println("\n" + "Performing search...");
		    
		    Search s = new Search(model);
			PrintStream out = new PrintStream("exaustiva-"+fileName);
			System.setOut(out);
			System.setErr(out);
			long start = System.currentTimeMillis();
		    s.planForward();
		    long end = System.currentTimeMillis();
		    System.out.println("Tempo gasto: " + (end - start));

		    long memory = runtime.totalMemory() - runtime.freeMemory();
			System.out.println("Used memory is bytes: " + (memory - initmemory));
			
		    out.close();
			
			
		}else if(t == 1) {
			System.out.println("Heuristic search");
			System.out.println("Performing search...");
			Search r = new Search(model);	    
		    PrintStream out2 = new PrintStream("heuristica-"+fileName);
			System.setOut(out2);
			System.setErr(out2);
			long start = System.currentTimeMillis();
		    r.heuristcSearch(model);
		    long end = System.currentTimeMillis();
		    System.out.println("Tempo gasto: " + (end - start));
			
			runtime.gc();
			long memory = runtime.totalMemory() - runtime.freeMemory();
			System.out.println("Used memory is bytes: " + (memory - initmemory));
			out2.close();

		}
	}
}