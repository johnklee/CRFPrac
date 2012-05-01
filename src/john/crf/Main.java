package john.crf;

import java.io.File;

public class Main {
	public static void main(String args[]) throws Exception
	{
		if(args.length==2)
		{
			String trainDataPath = args[0];
			String outputTrainPath = args[1];
			System.out.printf("\t[Main] Training data path=%s...\n", args[0]);
			HNameModel testModel = new HNameModel();
			testModel.nlabels = 100;
			File tdFdr = new File(trainDataPath);
			if(tdFdr.exists() && tdFdr.isDirectory())
			{
				for(File f:tdFdr.listFiles())
				{
					System.out.printf("\t[Main] Process training data : %s...\n", f.getName());
					testModel.parseRawdata(f.getAbsolutePath());					
				}
				testModel.train();
				System.out.printf("\t[Main] Save Model to %s...\n", outputTrainPath);
				testModel.saveModel(outputTrainPath);
			}
			else
			{
				System.out.printf("\t[Main] Training data path doesn't exist!\n");
			}
		}
		else
		{
			System.out.printf("\t[Main] Give two arguments : training data path & output model path!\n");
		}
	}
}
