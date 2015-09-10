package processor;

import java.io.IOException;

import map.ThemeParkMap;
import model.SimulationParameters;
import model.SocialForceParameters;
import osm.OsmMap;
import visualizer.MapVisualizer;

/**
 * @author Gurkan Solmaz
 * 		   Department of EECS - University of Central Florida
 * 		   Disaster Mobility - Spring 2013
 * 		   Advisor: Dr. Damla Turgut
 */
public class MainController {

	public static void main(String args[]) throws IOException, InterruptedException{
		// set simulation parameters
		
		SimulationParameters sp = new SimulationParameters(2000,  // simulation time
				40,   // communication range
				0.0,  // min speed
				1,    // max speed
				50,   // number of red zones
				1000, // active red zone time
				2, 	  // min road width
				30,   // max road width
				100,  // max visibility
				50,   // red zone radius
				10,	  // random move distance
				1);// number of humans
		
		sp.setUserInputs(10, // sampling time 
						"MagicKindgom", // park name
						true, // isVisualizerOn ?
						false,  // isMapVisualizerOn (FULL SCREEN, not available for screenshot)
						true); // isKeyboardControlOn
		
		// set social force parameters
		SocialForceParameters sfp = new SocialForceParameters(0.11, 0.06, 0.84, 0.63, 0.5, 0.1, 0.5, true, false);
		
		// load the map file
		MapLoader ml = new MapLoader(sp.getMapName());
		OsmMap osmMap = ml.getOsmMap();
		
		// create the theme park map
		MapProcessor mapProcessor = new MapProcessor(osmMap, sp.getMinRoadWidth(), sp.getMaxRoadWidth(), sp.getNumberOfRedZones(), sp.getRedZoneRadius());
		ThemeParkMap themeParkMap = mapProcessor.generateRedZoneTimes(sp.getSimulationTime(),sp.getActiveRedZoneTime());
		
		// visualizer theme park map 
		if(sp.isMapVisualizerOn()){
			MapVisualizer mv = new MapVisualizer(themeParkMap); 
		}
		// create initial humans and waypoints ! 
		MobilityProcessor mp = new MobilityProcessor(sp,themeParkMap, sfp);
		
		// start the simulation
		SimulationController sc = new SimulationController(sp, mp, themeParkMap);
		sc.simulate();
		
		OutputController oc = new OutputController(sc.getHumanListFromMobilityData(),sp);
		
		System.out.println("Simulation is completed");
		System.exit(0);		
	}
}
