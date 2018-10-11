import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;
import fr.enseeiht.danck.voice_analyzer.defaults.DTWHelperDefault;


public class myDTWtest {
	
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
		Extractor extractor = Extractor.getExtractor();
	    String base = "/test_res/audio/csv/" + nomLocuteur + "_";
	    Field[] fieldVocabulaire = new Field[vocabulaire.length];
	    MFCC[][] mfccVocabulaire = new MFCC[vocabulaire.length][];
	    
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
	        for (int j = 0; j < mfccVocabulaire[i].length; j++) {
	        	mfccVocabulaire[i][j] = extractor.nextMFCC(windowMaker);
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) du mot
	        fieldVocabulaire[i] = new Field(mfccVocabulaire[i]);
	    }
	    
	    return fieldVocabulaire;
	}

	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		myDTW myDTW = new myDTW();

	    // Appel a l'extracteur par defaut (calcul des MFCC)
	    
	    //Enregistrements perso :
	    //String locuteur = new String("Axel");
	    String locuteur = new String("M01");
	    String[] hypoteses = {"M02"};
	    String[] vocabulaire = {"arretetoi","atterrissage","avance","decollage","droite","etatdurgence",
	    						"faisunflip","gauche","plusbas","plushaut","recule","tournedroite","tournegauche"};
	    
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
	}
}
