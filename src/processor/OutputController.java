package processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import model.Human;
import model.Point;
import model.SimulationParameters;

/**
 * @author Gurkan Solmaz
 * 		   Department of EECS - University of Central Florida
 * 		   Disaster Mobility - Spring 2013
 * 		   Advisor: Dr. Damla Turgut
 */
public class OutputController {
	List<Human> humanList;
	String currentDirectory;
	SimulationParameters sp;
	int newFileCounter;
	public OutputController(List<Human> humanList, SimulationParameters sp) {
		super();
		this.humanList = humanList;
		this.currentDirectory =	System.getProperty("user.dir");
		this.sp=sp;
		
		
		File f = new File(currentDirectory + "\\output\\");
		
		if(!f.exists()){
			f.mkdir();
		    System.out.println("Directory 'output' is created !!");  
		}
		
		this.currentDirectory = currentDirectory + "\\output\\"; 
				
		outputResults();
	}

	private void outputResults() {
	
		
		newFileCounter = 6;
		outputFlightLengths(); 		
		outputNumberOfWaitingPoints(); 
		outputDisasterResults(); 
		outputLifetimes(); 
		outputTrajectories();
		outputTraces();
	}

	private void outputLifetimes() {
		FileWriter fstream;
		try {
			File f = new File(currentDirectory );
			File[] fileList = f.listFiles();
			fstream = new FileWriter(currentDirectory+ "DisasterLifetimes" + (fileList.length/newFileCounter+1) + "_Vis_" 
			+ sp.getVisibility() +  "_RedZones" + sp.getNumberOfRedZones() +  ".txt");
				

					
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(humanList.size() +"\n");

			// write number of entries
			for(int i=0;i<humanList.size();i++){
				Human h = humanList.get(i);
				out.write(h.getLifeTime()+"\n");	
			}
			out.close();
			fstream.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void outputDisasterResults() {
		FileWriter fstream;
		try {
			
			
			File f = new File(currentDirectory );
			File[] fileList = f.listFiles();
			fstream = new FileWriter(currentDirectory+ "DisasterResults" + (fileList.length/newFileCounter+1) + ".txt");
				
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Number of humans:" + humanList.size() +"\n");
			
			int saved=0;
			for(int i=0;i<humanList.size();i++){
				Human h = humanList.get(i);
				if(h.isSaved()){
					saved++;
				}
			}
			out.write("Number of saved:" +saved +"\n");

			out.close();
			fstream.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void outputFlightLengths() {	
		FileWriter fstream;
		
		try {
			
			File f = new File(currentDirectory );
			File[] fileList = f.listFiles();
			fstream = new FileWriter(currentDirectory+ "DisasterFlightLengths" + (fileList.length/newFileCounter+1) + ".txt");
		
			BufferedWriter out = new BufferedWriter(fstream);
			// write number of entries
			for(int i=0;i<humanList.size();i++){
				Human h = humanList.get(i);
				for(int j=0;j<h.getFlightLengthList().size();j++){
					out.write(h.getFlightLengthList().get(j)+"\n");
				}
			}
			out.close();
			fstream.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void outputNumberOfWaitingPoints() {
		FileWriter fstream;
		try {
			
			File f = new File(currentDirectory );
			File[] fileList = f.listFiles();
			fstream = new FileWriter(currentDirectory+ "DisasterNumberOfWaitingPoints" + (fileList.length/newFileCounter+1) + ".txt");
					
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(humanList.size() +"\n");

			// write number of entries
			for(int i=0;i<humanList.size();i++){
				Human h = humanList.get(i);
				out.write(h.getWaitingPointList().size()+"\n");	
			}
			out.close();
			fstream.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private void outputTrajectories() {	
		

		FileWriter fstream = null;
		
		try {
			File f = new File(currentDirectory );
			File[] fileList = f.listFiles();
			fstream = new FileWriter(currentDirectory+ "DisasterTrajectories" + (fileList.length/newFileCounter+1) + ".txt");
			

		
			BufferedWriter out = new BufferedWriter(fstream);
			// write pre information
			
			out.write("NumberOfVisitors: " + humanList.size()+"\n");
			out.write("SimulationTime: " + sp.getSimulationTime() +"\n");
			out.write("SamplingTime: " + sp.getSamplingTime() +"\n");
			
		
			for(int i=0; i<sp.getSimulationTime()/sp.getSamplingTime(); i++){
				// CHECK FOR taking only simulation times of 10 seconds -- OPTIONAL
				if((i*sp.getSamplingTime())%10!=0){
					continue;
				}
				
				
				// Write Current Sim Time
				out.write("Current Simulation Time: " + i*sp.getSamplingTime() + "\n");
				// write the indices & coordinates of each mobile node for the current time
				for(int j=0; j<humanList.size(); j++){
					// if the node is not "dead", write the trajectory
					if(humanList.get(j).getLifeTime() >=  i*sp.getSamplingTime()){
						out.write("Index: " + j + " ");
						// coordinates 
						out.write("Coordinates: " + humanList.get(j).getTrajectoryPointList().get(i).getX() + " " + humanList.get(j).getTrajectoryPointList().get(i).getY() + "\n");	
					}
				}
			}
			out.close();
			fstream.close();		
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
private void outputTraces()  {	
		

		FileWriter fstream;
		
		try {
			
			File f = new File(currentDirectory );
			File[] fileList = f.listFiles();
			fstream = new FileWriter(currentDirectory+ "DisasterTraces" + (fileList.length/newFileCounter+1) + ".txt");
			
			
			boolean isFileLocked = true;
			while(isFileLocked){	
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) { continue;}
				try {
				    org.apache.commons.io.FileUtils.touch(f);
				    isFileLocked = false;
				    System.out.println("Starts writing traces!!");
				} catch (IOException e) { 
				    System.out.println("Cannot write traces, waiting!!");
					continue;
				}
			}

	
			BufferedWriter out = new BufferedWriter(fstream);
			// write pre information
						
			for(int i=0;i<humanList.size();i++){ // for each node
				for(int j=0;j<=sp.getSimulationTime()/sp.getSamplingTime();j++){ //for each time
					out.write(j + " "); // current time
					if(humanList.get(i).getLifeTime() > j* sp.getSamplingTime()){ // if alive
						Point p = humanList.get(i).getTrajectoryPointList().get(j);
						out.write(p.getX() + " " + p.getY() + "\n" );
					}else{ // dead now
						out.write("None" + " " + "None" + "\n" );
					}
				}
			}
			
			out.close();
			fstream.close();		
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
