package ga.util.Evaluation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import model.EvalResult;
import util.LeitorLog;
import util.LocalShell;
import util.ManagerJob;

public class RoundRobinEval implements RatePopulation{
	//CONSTANTES
	private static final int TOTAL_PARTIDAS_ROUND = 4;
	
	//Classes de controle do cluster
	private ManagerJob manager = new ManagerJob();
	private LocalShell shell = new LocalShell();
	
	//Atributos locais
	
	public RoundRobinEval() {
		super();
	}

	@Override
	public Population evalPopulation(Population population) {
		//limpa os valores existentes na population
		population.clearValueChromosomes();
		
		//executa os confrontos
		runBattles(population);
		
		//Só permite continuar a execução após terminar os JOBS.
		controllExecute();

		//validar se todos executaram adequadamente
				
		//remove qualquer aquivo que não possua um vencedor
		removeLogsEmpty();

		//ler resultados
		ArrayList<EvalResult> resultados = lerResultados();
		//atualizar valores das populacoes
		updatePopulationValue(resultados, population);

		return population;
	}
	
	
	private void removeLogsEmpty() {
		LeitorLog log = new LeitorLog();
		log.removeNoResults();
	}

	public Population updatePopulationValue(ArrayList<EvalResult> results, Population pop){
		ArrayList<EvalResult> resultsNoDraw = removeDraw(results);
		
		/* System.out.println("Avaliações sem Draw");
		for (EvalResult evalResult : resultsNoDraw) {
			evalResult.print();
		}
		*/
		
		for (EvalResult evalResult : resultsNoDraw) {
			updateChomoPopulation(evalResult, pop);
		}
		
		return pop;
	}
	
	private void updateChomoPopulation(EvalResult evalResult, Population pop) {
		
		//identicar qual IA foi a vencedora
		String IAWinner = "";
		if(evalResult.getEvaluation()==0){
			IAWinner = evalResult.getIA1();
		}else{
			IAWinner = evalResult.getIA2();
		}
		
		//buscar na população a IA compatível.
		Chromosome chrUpdate = null;
		for(Chromosome ch : pop.getChromosomes().keySet()){
			if(convertBasicTuple(ch).equals(IAWinner)){
				chrUpdate = ch;
			}
		}
		//atualizar valores.
		BigDecimal toUpdate = pop.getChromosomes().get(chrUpdate);
		toUpdate = toUpdate.add(BigDecimal.ONE);
		HashMap<Chromosome, BigDecimal> chrTemp = pop.getChromosomes();
		chrTemp.put(chrUpdate, toUpdate);
	}

	private ArrayList<EvalResult> removeDraw(ArrayList<EvalResult> results) {
		ArrayList<EvalResult> rTemp = new ArrayList<>();
		
		for (EvalResult evalResult : results) {
			if(evalResult.getEvaluation() != -1){
				rTemp.add(evalResult);
			}
		}
		
		return rTemp;
	}

	public ArrayList<EvalResult> lerResultados(){
		LeitorLog leitor = new LeitorLog();
		ArrayList<EvalResult> resultados = leitor.processar();
		/*
		for (EvalResult evalResult : resultados) {
			evalResult.print();
		}
		*/
		return resultados;
	}
	
	/**
	 * Verifica se os jobs já foram encerrados no cluster.
	 */
	private void controllExecute(){
		try {
			while( Integer.valueOf(shell.executeCommand("echo $(qselect -u es91661 | wc -l)").trim()) > 1 ){
				Thread.sleep(50000);
			}
		} catch (NumberFormatException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Metódo para enviar todas as batalhas ao cluster. 
	 * @param population Que contém as configuracoes para a IA
	 */
	private void runBattles(Population population){
		//montar a lista de batalhas que irão ocorrer

		for (int i = 0; i < TOTAL_PARTIDAS_ROUND; i++) {

			for (Chromosome cIA1 : population.getChromosomes().keySet()) {

				for (Chromosome cIA2 : population.getChromosomes().keySet()) {
					if(!cIA1.equals(cIA2)){
						System.out.println("IA1 = "+ convertTuple(cIA1)+ "  IA2 = "+ convertTuple(cIA2));

						//enviar jobs ao cluster
						manager.configureArq( convertTuple(cIA1), convertTuple(cIA2), i );
						shell.executeScript("/storage1/dados/es91661/ExecAIGA/aControlStartJob.sh");

						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						//controla o total de jobs em execução
						try {
							while( Integer.valueOf(shell.executeCommand("echo $(qselect -u es91661 | wc -l)").trim()) > ConfigurationsGA.NUMBER_JOBS ){
								System.gc();
								Thread.sleep(30000);
							}
						} catch (NumberFormatException | IOException | InterruptedException e) {
							e.printStackTrace();
						}


					}

				}
			}
		}
	}
	
	private String convertTuple(Chromosome cromo){
		String tuple = "'";

		for (Integer integer : cromo.getGenes()) {
				tuple +=integer+";";
		}
		
		return tuple += "'";
	}
	
	private String convertBasicTuple(Chromosome cromo){
		String tuple = "";

		for (Integer integer : cromo.getGenes()) {
				tuple +=integer+";";
		}
		
		return tuple ;
	}
	
}