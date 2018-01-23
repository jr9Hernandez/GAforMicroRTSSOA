package ga.config;

public final class ConfigurationsGA {
	//total de scripts que serão utilizados no teste. ex.: 300 = 0-299
	public final static int QTD_SCRIPTS = 300;
	//tamanho fixo do cromossomo
	public final static int SIZE_CHROMOSOME = 5;
	//tamanho fixo da população
	public final static int SIZE_POPULATION = 20;
	//Total de jobs que serão enviados ao cluster
	public final static int NUMBER_JOBS = 30;
	//tamanho fixo da elite
	public final static int SIZE_ELITE = 10;
	//tamanho fixo do k do torneio
	public final static int K_TOURNMENT = 3;
	//tamanho fixo dos pais para crossover
	public final static int SIZE_PARENTSFORCROSSOVER = 4;
	//taxa mutacao
	public final static double MUTATION_RATE = 0.01;
	//---------------------------------------------------------------------------------
	//Controle do dispositivo de parada do GA
	//Parametro: 0 = Tempo; 1 = Gerações
	public final static int TYPE_CONTROL = 1;
	//tempo, em horas, que o GA será executado
	public final static int TIME_GA_EXEC = 13;
	//número de gerações que serão executadas
	public final static int QTD_GENERATIONS = 30;
	//---------------------------------------------------------------------------------
	
}
