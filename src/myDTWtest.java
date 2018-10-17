import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;


public class myDTWtest {

    public static final String[] vocabulaire = {"arretetoi","avance","droite",
			"faisunflip","gauche","recule","tournedroite","tournegauche"/*,"penduleinverse"*/};
    
    public static final String lbRep="/test_res/audio/csv/";
    
    public static final String testRep="/test_res/test/";
    
    public static final int maxLocuteurs=50;
    
    public static float tauxErreur;
	
    // Fonction permettant de calculer la taille des Fields 
			//c'est-�-dire le nombre de MFCC du Field 
	static int FieldLength(String fileName) throws IOException {
		int counter= 0;
		File file = new File(System.getProperty("user.dir") + fileName);
	    for (String line : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
	           	counter++;
	    }
	    return 2*Math.floorDiv(counter, 512);
	}
	
	public static Field[] loadingAudioFiles (String nomLocuteur, String[] vocabulaire) throws IOException, InterruptedException
	{
	    String base = "/test_res/audio/csv/" + nomLocuteur + "_";
	    Field[] fieldVocabulaire = new Field[vocabulaire.length];
	    MFCC[][] mfccVocabulaire = new MFCC[vocabulaire.length][];
		Extractor extractor = Extractor.getExtractor();

	    String nomF;
	    int MFCCLength; 
	    for(int i = 0; i < vocabulaire.length; i++)
	    {	nomF=base + vocabulaire[i] + ".csv";
	    	// Etape 1. Lecture du fichier
		    List<String> files = new ArrayList<>();
		    files.add(nomF);
		    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
		    
		    // Etape 2. Recuperation des MFCC du mot
		    MFCCLength =FieldLength(nomF);
		    mfccVocabulaire[i] = new MFCC[MFCCLength];
	        for (int j = 0; j < MFCCLength; j++) {
	        	mfccVocabulaire[i][j] = extractor.nextMFCC(windowMaker);
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) du mot
	        fieldVocabulaire[i] = new Field(mfccVocabulaire[i]);
	    }
	    
	    return fieldVocabulaire;
	}
	
	
	public static Field getField (String rep,String name) throws IOException,InterruptedException
	{
	    String nomF = rep + name;
	    int MFCCLength;
	    List<String> files= new ArrayList<>();
	    files.add(nomF);
	    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
		Extractor extractor = Extractor.getExtractor();
		MFCCLength=FieldLength(nomF);
		MFCC [] instructionMfcc=new MFCC[MFCCLength];
		for (int i=0;i<MFCCLength;++i)
			instructionMfcc[i]=extractor.nextMFCC(windowMaker);
		return new Field (instructionMfcc);

	}
	
	public static HashMap <String,Set<Field>> getLearningBase () throws IOException, InterruptedException{
		HashMap <String,Set <Field> >lb =  new HashMap<> (vocabulaire.length);
		for (int i=0;i<vocabulaire.length;++i)
			lb.put(vocabulaire[i], new HashSet<Field>(maxLocuteurs));
		File rep = new File ("."+lbRep);
		String liste []=rep.list();
		if (liste==null){
			System.err.println("erreur en chargeant la base d'apprentisage");
			System.exit(1);
		}
		
		for (int i=0;i<liste.length;++i)
			for (int j=0;j<vocabulaire.length;++j)	
				if ( liste[i].contains(vocabulaire[j]) && !liste[i].contains("tourne"+vocabulaire[j]) ){
				// la deuxième condition est nécessaire pour ne pas inclure les fields des fichiers tournedroite,par
				// example, dans l'ensemble des fields de droite
					Set <Field> set=lb.get(vocabulaire[j]);
					set.add(getField(lbRep,liste[i]));
					lb.put(vocabulaire[j], set );  
				}
		return lb;
		
	}
	
	public static  Field [] getTestFields () throws IOException, InterruptedException{
		Field[] result=new Field[vocabulaire.length];
		for (int i=0;i<vocabulaire.length;++i) {
			result[i]=getField(testRep, vocabulaire[i]+".csv" );
		}
	return result;
	}
	
	public static float computeMinDistance(HashMap<String, Set<Field> >lb,Field  testUnit,int i ){
		myDTW myDTW = new myDTW();
		Set<Field> set=lb.get(vocabulaire[i]);
		float min = Float.POSITIVE_INFINITY;
		float d;
		for (Field field : set) {
			d=myDTW.DTWDistance(testUnit, field);
			if (d<min)
				min=d;
		}
		return min;
	}
	
	public static int [][] computeConfusionMatrix () throws IOException, InterruptedException{
		float [] [] distances=new float [vocabulaire.length][vocabulaire.length];
		int [] [] result=new int [vocabulaire.length][vocabulaire.length];
		int minIndex = 0;
		float min;
		HashMap<String, Set<Field>> lb=getLearningBase();
		Field [] test=getTestFields();
		for (int j=0;j<vocabulaire.length;++j){
			min=Float.POSITIVE_INFINITY;
			for (int i=0;i<vocabulaire.length;++i){
				result[i][j]=0;
				distances[i][j]=computeMinDistance (lb,test[j],i);
				if (distances[i][j]<min){
					min=distances[i][j];
					minIndex=i;
				}
			}
			if (minIndex!=j)tauxErreur++;
			result[minIndex][j]++;
		}
		tauxErreur=tauxErreur/vocabulaire.length;
		return result;
	}

	public static void printResults(int [][] confusionMatrix){
	    for (int i = 0; i < vocabulaire.length; i++)
	    {
	    	for (int j = 0; j < vocabulaire.length; j++)
	    		System.out.print(confusionMatrix[i][j] + " ");
	    	System.out.println("");
	    }
	    
	    System.out.println("Taux d'erreur = "+tauxErreur);		
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		/*
		myDTW myDTW = new myDTW();

	    // Appel a l'extracteur par defaut (calcul des MFCC)
	    
	    //Enregistrements perso :
		
	    String locuteur = new String("M01");
	    String[] hypoteses = {"M02"};

	    
	    //Chargement du locuteur r�f�rent
	    Field[] motLocuteur = loadingAudioFiles(locuteur,vocabulaire);
	    
        
	    float tauxReco = 0;
	    int[][] matriceConfusion = new int[vocabulaire.length][vocabulaire.length];
	    float[][] matRes = new float[vocabulaire.length][vocabulaire.length];


	    //Initialisation
	    for (int i = 0; i < vocabulaire.length; i++)
	    {
	    	for (int j = 0; j < vocabulaire.length; j++)
	    	{
	    		matRes[i][j] = 0;
	    		matriceConfusion[i][j] = 0;
	    	}
	    }
	    
	    
	    for (int i = 0; i < hypoteses.length; i++)
	    {
	    	//Chargement des hypotheses
		    Field[] motHyptoses = loadingAudioFiles(hypoteses[i],vocabulaire);
		    
		    for (int motHypIdx = 0; motHypIdx < vocabulaire.length; motHypIdx++)
		    {
		    	//Calcul des distances
		    	for (int motRefIdx = 0; motRefIdx < vocabulaire.length; motRefIdx++) {
		            matRes[motHypIdx][motRefIdx] = myDTW.DTWDistance(motHyptoses[motHypIdx],motLocuteur[motRefIdx]);
		    	}
		      
		    	int minIdx = 0;
		    	float min = matRes[motHypIdx][0];

		    	//R�cup�ration de l'indice de la valeur min
		    	for (int ind = 0; ind < vocabulaire.length; ind++)
		    	{
		    		if (matRes[motHypIdx][ind] < min)
		    		{
		    			min = matRes[motHypIdx][ind];
		    			minIdx = ind;
		    		}
		    	}
		    
		    	//MAJ
		    	matriceConfusion[motHypIdx][minIdx]++;
		      
		    	//MAJ
		    	if (motHypIdx == minIdx)
		    		tauxReco++;
		    }
	    }
	    tauxReco /= (vocabulaire.length * hypoteses.length);
	    
	    //Affichage des r�sultats
	    for (int i = 0; i < vocabulaire.length; i++)
	    {
	    	for (int j = 0; j < vocabulaire.length; j++)
	    		System.out.print(matriceConfusion[i][j] + " ");
	    	System.out.println("");
	    }
	    
	    System.out.println("TauxReco = "+tauxReco);
	    */
		printResults(computeConfusionMatrix());
	}
	
}
