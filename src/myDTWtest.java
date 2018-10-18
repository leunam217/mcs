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
    
    public static final String testRep="/test_res/test4/";
    
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
		File rep = new File ("."+testRep);
		String liste []=rep.list();
		if (liste==null){
			System.err.println("erreur en chargeant le test");
			System.exit(1);
		}
		for (int i=0;i<vocabulaire.length;++i)
			for (int  j=0;j<liste.length;++j){
				if (liste[j].contains("csv") && liste[j].contains(vocabulaire [i]) && !liste[j].contains("tourne"+vocabulaire[i]))
				result[i]=getField(testRep, liste[j]);
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

	static float [][] temp;
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
		temp=distances;
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
	public static void printResults (float [][] confusionMatrix){
	    for (int i = 0; i < vocabulaire.length; i++)
	    {
	    	for (int j = 0; j < vocabulaire.length; j++)
	    		System.out.print(confusionMatrix[i][j] + " ");
	    	System.out.println("");
	    }
	    
	    System.out.println("Taux d'erreur = "+tauxErreur);		
	}
	public static void main(String[] args) throws IOException, InterruptedException {

		printResults(computeConfusionMatrix());
		printResults(temp);
	}
	
}
