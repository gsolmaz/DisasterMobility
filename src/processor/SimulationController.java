package processor;

import java.util.List;

import map.ThemeParkMap;
import model.Human;
import model.SimulationParameters;
import visualizer.SimulationVisualizer;
	/**
	 * @author Gurkan Solmaz
	 * 		   Department of EECS - University of Central Florida
	 * 		   Disaster Mobility - Spring 2013
	 * 		   Advisor: Dr. Damla Turgut
	 */
	public class SimulationController {
		double simulationTime;
		double samplingTime;
		double currentTime;
		boolean isKeyboardInputOn;
		boolean isVisualizerOn;
		MobilityProcessor mp;
		ThemeParkMap tpm;
		
		SimulationController(SimulationParameters sp, MobilityProcessor mp, ThemeParkMap tpm){
			this.simulationTime=sp.getSimulationTime();
			this.samplingTime=sp.getSamplingTime();
			this.isKeyboardInputOn=sp.isKeyboardInputOn();
			this.isVisualizerOn=sp.isVisualizerOn();
			this.mp = mp;
			this.tpm = tpm; // used for visualizer
			this.currentTime=0;
		}
		
		public void simulate() throws InterruptedException{
			System.out.println("Simulation started !");
			SimulationVisualizer sv=null;
			if(isVisualizerOn){
				sv = new SimulationVisualizer(tpm);
			}
			
			while(true){	
				if(!isKeyboardInputOn || sv.isFinish() || sv.isResume()){ 
					// simulation is allowed to continue 
					System.out.println("-------------Simulation time: " + currentTime + " seconds");
	
					// do the necessary moves
					mp.updateHumans(currentTime);
					
					// update visualizer & draw	
					if(isVisualizerOn){ 
						sv.setHumanList(mp.getHumanList());
						sv.setResume(false); sv.setCurrentTime(currentTime);
					}
					currentTime += samplingTime;
					if(currentTime >= simulationTime){
						System.out.println("Simulation time: " + currentTime + " seconds");
						break; // simulation time passed, finish the simulation 
					}
				}
				Thread.sleep(1);
			}	
		}
		public List<Human> getHumanListFromMobilityData(){
			return mp.getHumanList();
		}
	}
