import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;
import fr.enseeiht.danck.voice_analyzer.defaults.DTWHelperDefault;

/*public class myDTWtest {

		protected static final int MFCCLength = 13;

		public static void main(String[] args) throws IOException, InterruptedException {
			
			//DTWHelper myDTWHelper= new myDTW();
			DTWHelper DTWHelperDefault= new DTWHelperDefault();
			 
			// Chemin de recherche des fichiers sons
		    String base = "/test_res/audio/";
		    
		    // Appel a l'extracteur par defaut (calcul des MFCC)
		    Extractor extractor = Extractor.getExtractor();
		    
			// Etape 1. Lecture de Alpha
		    List<String> files = new ArrayList<>();
		    files.add(base + "Alpha.csv");
		    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
		    
		    // Etape 2. Recuperation des MFCC du mot Alpha
		    MFCC[] mfccsAlpha = new MFCC[MFCCLength];
	        for (int i = 0; i < mfccsAlpha.length; i++) {
	            mfccsAlpha[i] = extractor.nextMFCC(windowMaker);
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) de alpha
	        Field alphaField= new Field(mfccsAlpha);
	        
	        // Si on veut rajouter de nouveaux mots, il suffit de repeter les etapes 1 a 3
	        
	        // Par ex., on peut tester que la distance entre alpha et alpha c'est 0
	       // float mydistanceAlphaAlpha= myDTWHelper.DTWDistance(alphaField, alphaField);
	        float distanceAlphaAlphadefault= DTWHelperDefault.DTWDistance(alphaField, alphaField);
	        
	       // System.out.println("myDTW - valeur distance Alpha-Alpha calculee : "+mydistanceAlphaAlpha);
	        System.out.println("DTWHelperDefault - valeur distance Alpha-Alpha calculee : "+distanceAlphaAlphadefault);
		
	        // Calcul de la distance entre Alpha et Bravo
	        
	        // Etape 1. Lecture de Bravo
	        files= new ArrayList<>();
		    files.add(base + "Bravo.csv");
		    windowMaker = new MultipleFileWindowMaker(files);
		    
		 // Etape 2. Recuperation des MFCC du mot Bravo
		    MFCC[] mfccsBravo= new MFCC[MFCCLength];
	        for (int i = 0; i < mfccsBravo.length; i++) {
	            mfccsBravo[i] = extractor.nextMFCC(windowMaker);
	       
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) de Bravo
	        Field bravoField= new Field(mfccsBravo);
	        
	       //float mydistanceAlphaBravo= myDTWHelper.DTWDistance(alphaField, bravoField);
	        float distanceAlphaBravodefault= DTWHelperDefault.DTWDistance(alphaField, bravoField);
	        
	        //System.out.println("myDTW - valeur distance Alpha-Bravo calculee : "+mydistanceAlphaBravo);
	        System.out.println("DTWHelperDefault - valeur distance Alpha-Bravo calculee : "+distanceAlphaBravodefault);
		}

}*/

public class myDTWtest {

	//protected static final int MFCCLength = 13;
	
			// Fonction permettant de calculer la taille des Fields 
			//c'est-à-dire le nombre de MFCC du Field 
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
	    MFCC[][] mfccVocabulaire = new MFCC[vocabulaire.length][FieldLength];		    
	    
	    for(int i = 0; i < vocabulaire.length; i++)
	    {
	    	// Etape 1. Lecture du fichier
		    List<String> files = new ArrayList<>();
		    files.add(base + vocabulaire[i] + ".csv");
		    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
		    
		    // Etape 2. Recuperation des MFCC du mot
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
	    
	    //Chargement du locuteur référent
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

		    	//Récupération de l'indice de la valeur min
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
	    
	    //Affichage des résultats
	    for (int i = 0; i < vocabulaire.length; i++)
	    {
	    	for (int j = 0; j < vocabulaire.length; j++)
	    		System.out.print(matriceConfusion[i][j] + " ");
	    	System.out.println("");
	    }
	    
	    System.out.println("TauxReco = "+tauxReco);
	}
}
