import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;

public class myDTW extends DTWHelper {

	
	private float distanceVect (Field AnalysedSeq, Field ReferenceSeq, int i, int j)
	{
		MFCC mfcc1 = AnalysedSeq.getMFCC(i);
		MFCC mfcc2 = ReferenceSeq.getMFCC(j);
		myMFCCdistance mfccDist = new myMFCCdistance();	
		
		return mfccDist.distance(mfcc1,mfcc2);
	}
	
	
	private float minimum (float valeurDiagonale, float valeurHaut, float valeurGauche)
	{	
		if ((valeurDiagonale < valeurGauche) && (valeurDiagonale < valeurHaut))
		{
			return valeurDiagonale;
		}
		else if ((valeurHaut < valeurGauche) && (valeurHaut < valeurDiagonale))
		{
			return valeurHaut;
		}
		else
		{
			return valeurGauche;
		}
	}
	
	
	@Override
	public float DTWDistance(Field AnalysedSeq, Field ReferenceSeq) {
		// Methode qui calcule le score de la DTW 
		// entre 2 ensembles de MFCC

		int I,J, w0, w1, w2;
		final int inf = java.lang.Integer.MAX_VALUE;
		
		w0 = w1 = w2 = 1;
		I = AnalysedSeq.getLength() + 1;
		J = ReferenceSeq.getLength() + 1;
		
		float g[][] = new float[I][J];
		  
		for (int j = 1; j < J; j++)
			g[0][j] = inf;
		
		
		for(int i = 1; i < I; i++)
		{
		    g[i][0] = inf;
		    for(int j = 1; j < J; j++)
		    {
		       float dist = distanceVect(AnalysedSeq,ReferenceSeq,i - 1,j - 1);
		       g[i][j]= minimum(g[i-1][j-1] + w1*dist, 	//Diagonale
		                        g[i-1][j]   + w0*dist, 	//Haut
		                        g[i][j-1]   + w2*dist); //Gauche 
		    }
		}
		return g[I-1][J-1]/(I + J - 2);
	}
}
