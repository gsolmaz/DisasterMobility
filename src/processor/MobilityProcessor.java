package processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector2d;

import map.RedZone;
import map.Road;
import map.ThemeParkMap;
import model.Human;
import model.Point;
import model.SimulationParameters;
import model.SocialForceParameters;
/**
 * @author Gurkan Solmaz
 * 		   Department of EECS - University of Central Florida
 * 		   Disaster Mobility - Spring 2013
 * 		   Advisor: Dr. Damla Turgut
 */
public class MobilityProcessor {
	List<Human> humanList;
	List<Point> wayPointList;
	ThemeParkMap tpm;
	double visibility;
	double randomMoveDistance;
	double minSpeed,maxSpeed;
	double samplingTime; 
	double currentSimulationTime;
	double totalSimulationtime;
	SocialForceParameters sfp;
	
	public MobilityProcessor(SimulationParameters simParam, ThemeParkMap tpm,   SocialForceParameters sfp) {
		super();

		this.wayPointList = createWayPointList(tpm.getRoadList()); // 1309 Waypoint for MagicKingdom !!
		this.tpm = tpm;
		this.minSpeed = simParam.getMinSpeed();
		this.maxSpeed = simParam.getMaxSpeed();
		this.visibility = simParam.getVisibility();
		this.randomMoveDistance = simParam.getRandomMoveDistance();
		this.humanList = createHumanList(simParam.getNumberOfHumans(), sfp);
		this.samplingTime = simParam.getSamplingTime();
		this.totalSimulationtime = simParam.getSimulationTime();
		this.currentSimulationTime = 0;
		this.sfp = sfp;
	}
	
	

	public List<Point> createWayPointList(List<Road> roadList){
		List<Point> returnList = new ArrayList<Point>();
		for(int i=0; i<roadList.size(); i++){
			Road r = roadList.get(i);
			for(int j=0;j<r.getWayPointList().size();j++){
				Point p= r.getWayPointList().get(j);
					returnList.add(p);
			}
		}
		// delete multiple copies of tsame waypoint
		for(int i=0;i<returnList.size(); i++){
			Point p = returnList.get(i);				
			for(int j=0;j<returnList.size();j++){
				if(i==j) continue;
				Point tmpP = returnList.get(j);
				if(tmpP.getX() == p.getX() && tmpP.getY() == p.getY()){
					returnList.remove(j);
				}
			}
		}
		return returnList;
	}
	
	public List<Human> createHumanList(int numberOfHumans, SocialForceParameters sfp){
		int numberOfWayPoints = wayPointList.size();
		List<Human> returnList = new ArrayList<Human>();
		for(int i=0;i<numberOfHumans;i++){
			// generate a max speed
			double maxVelocity = MathFunctions.pickRandomDoubleValueBetweenTwoNumbers(minSpeed, maxSpeed);
			
			// generate social force values
			double A = MathFunctions.pickRandomDoubleValueBetweenTwoNumbers(sfp.getA() - sfp.getDeviationA(), sfp.getA() + sfp.getDeviationA());
			double B = MathFunctions.pickRandomDoubleValueBetweenTwoNumbers(sfp.getB() - sfp.getDeviationB(), sfp.getB() + sfp.getDeviationB());

			// position the user in a way point
			int wayPointIndex = MathFunctions.pickRandomIntegerValueBetweenTwoNumbers(0, numberOfWayPoints);
			Point initialPosition = wayPointList.get(wayPointIndex);
			//System.out.println("Initial point: "+ initialPosition.getX() + " " + initialPosition.getY());
			List<Point> initTrajectList = new ArrayList<Point>();
			initTrajectList.add(initialPosition);
	

			int gateIndex = MathFunctions.pickRandomIntegerValueBetweenTwoNumbers(0, tpm.getGateList().size());
			int gatePointIndex = MathFunctions.
					pickRandomIntegerValueBetweenTwoNumbers(0,  tpm.getGateList().get(gateIndex).getWayPointList().size());
			Point gatePoint =  tpm.getGateList().get(gateIndex).getWayPointList().get(gatePointIndex);
			//System.out.println("Gate point: "+ gatePoint.getX() + " " + gatePoint.getY());
			//System.out.println("Difference: "+ MathFunctions.findDistanceBetweenTwoPoints(initialPosition, gatePoint));
			//System.out.println("Max Velocity: "+ maxVelocity + " A: " + A + " B: " + B);

			Human h = new Human(maxVelocity, initialPosition, gatePoint, true, false, false, A, B, initTrajectList);
			returnList.add(h);
		}
		return returnList;
	}


	// for each human
		// check if the human reached the gate or if the human is dead
		// check if reached the nextDestination
			// if reached, decide on the next destination
		// compute the new actual speed according to social force model and desired max speeds
		// move according to the new actual speed
		// save the trajectory point
		// end for
	public void updateHumans(double currentTime) {
		this.currentSimulationTime = currentTime;
		for(int i=0;i<humanList.size();i++){
			Human h = humanList.get(i);
			
			if(!h.isActive()) continue;
			if(h.isSaved() || h.isDead()){
				h.setActive(false);	continue;
			}
			
			if((currentTime + samplingTime) == totalSimulationtime){
				h.setLifeTime(totalSimulationtime);
			}
			
	
	
			// check if reached the destination
			if(h.getNextDestinationPoint() == null || 
					(h.getPositionPoint().getX() == h.getNextDestinationPoint().getX() && h.getPositionPoint().getY()==h.getNextDestinationPoint().getY())){		
				
				h= decideNextDestination(h);
			
				// set the current (beginning) speed accordingly
				if(h.getCurrentVelocity()==null){
					Vector2d startVelocity = MathFunctions.findSpeedVectorByTwoPointsAndVelocity(h.getPositionPoint(), h.getNextDestinationPoint(), h.getMaxSpeed());
					h.setCurrentVelocity(startVelocity);
				}
			}		
			h= computeAcceleration(h, i);
			h = moveHumanAndSetNewSpeed(h,currentTime);
			if(i==0){
				System.out.println("ACC: " +h.getAcceleration().length());
				System.out.println("VEL: " +h.getCurrentVelocity().length());
				System.out.println("POS: " + MathFunctions.findDistanceBetweenTwoPoints(h.getGatePoint(), h.getPositionPoint()));
			}
			// save the trajectory
	
			h.addTrajectoryPoint(h.getPositionPoint());	
			// replace the human with the new human
			humanList.set(i, h);
			}
	}
	
	private Human moveHumanAndSetNewSpeed(Human h, double currentTime) {
		Point position = h.getPositionPoint();
		Vector2d distance = new Vector2d(0,0);
		distance.add(h.getCurrentVelocity());
		if(distance.length() > maxSpeed){
			System.out.println("ERROR in calculating the new velocity ! ");
		}
		distance.scale(samplingTime);
		Vector2d a = new Vector2d(); 
		a.set(h.getAcceleration());
		a.scale(samplingTime*samplingTime*0.5);
		distance.add(a); // distance formula : D= 1/2*a*t^2  
		
		double x,y;
		if(MathFunctions.findDistanceBetweenTwoPoints(h.getPositionPoint(), h.getGatePoint()) < 25 ){
			// reached the target gate !!
			x = h.getGatePoint().getX(); 
		    y = h.getGatePoint().getY();
		    h.setSaved(true);
		    h.setLifeTime(currentTime);
		    
		    // store the results
		    h.addWaitingPoint(h.getGatePoint());
		    h.addTmpFlightLength(MathFunctions.findDistanceBetweenTwoPoints(h.getPositionPoint(), h.getGatePoint()));
		    h.addFlightLengthList(h.getTmpFlightLength());
		    h.setTmpFlightLength(0);
		}
		else if(distance.length() >= MathFunctions.findDistanceBetweenTwoPoints(h.getPositionPoint(), h.getNextDestinationPoint()) ){
			// came into the next destination point !! 
			x = h.getNextDestinationPoint().getX(); 
		    y = h.getNextDestinationPoint().getY();
		    
		    // store the results
		    h.addWaitingPoint(h.getNextDestinationPoint());
		    h.addTmpFlightLength(MathFunctions.findDistanceBetweenTwoPoints(h.getPositionPoint(), h.getNextDestinationPoint()));
		    h.addFlightLengthList(h.getTmpFlightLength());
		    h.setTmpFlightLength(0);
		}
		else{
			x = position.getX() + distance.x;
			y = position.getY() + distance.y;
			// store the results (necessary to calculate the flight lengths)
		    h.addTmpFlightLength(distance.length());
		}
		Vector2d newVelocity = new Vector2d(0,0);
		newVelocity.add(h.getCurrentVelocity());
		Vector2d at = new Vector2d(0,0);
		at.add(h.getAcceleration());
		at.scale(samplingTime);
		newVelocity.add(at);  // V + a*t is the new velocity
		if(h.isSaved()){
			newVelocity =  new Vector2d(0,0);
		}
		if(newVelocity.length()>h.getMaxSpeed()){
			newVelocity = new Vector2d(0,0);
			newVelocity=MathFunctions.findSpeedVectorByTwoPointsAndVelocity(h.getPositionPoint(), h.getNextDestinationPoint(), h.getMaxSpeed());
		}

		h.setCurrentVelocity(newVelocity);
		Point newPosition = new Point(x, y);
	
		h.setPositionPoint(newPosition);
		return h;
	}

	private Human computeAcceleration(Human h, int indexOfTheHuman) {
		
		Vector2d socialForces = new Vector2d(0.0, 0.0); //initialize social forces
		
		Random r = new Random();
		double currentVisibility = (double) r.nextInt((int)visibility);
		
		
		
		// look at the other visible people
		for(int i=0;i<humanList.size();i++){
			if(i== indexOfTheHuman) continue;
			
			Human tmpHuman = humanList.get(i);
			if(!tmpHuman.isActive()) continue;
			
			// distance vector
			Vector2d distanceVector = MathFunctions.findVectorBetweenTwoPoints(tmpHuman.getPositionPoint(), h.getPositionPoint());
			double d = distanceVector.length();
			if(d==0){
				continue; // ignore the people waiting in the same place (it can happen only on the very beginning ! 
			}
			if(d<=currentVisibility){
				// calculate the social force 
				Vector2d sf =  new Vector2d(0,0);
				sf.add(findPedestrianSocialForce(h, d, distanceVector));
				socialForces.add(sf);
			}
		} 
		// found the total distance-dependent interaction force (Second part of the sum in Eq.3)
		
		// now, find the first part of the summation in Eq. 3)
		double desiredSpeed = h.getMaxSpeed();
		Vector2d desiredDirection = MathFunctions.findVectorDirectionBetweenTwoPoints(h.getPositionPoint(), h.getNextDestinationPoint());
		double t = sfp.getRelaxationTime();
		Vector2d desiredVelocity = MathFunctions.multiplyVectorByScalar(desiredDirection, desiredSpeed);
		desiredVelocity.sub(h.getCurrentVelocity());  
		Vector2d speedDiff = new Vector2d(0,0);
		speedDiff.add(desiredVelocity);
		speedDiff.scale(1/t);
		// first part (component) of the equation ends ! 
		// add the first part
		socialForces.add(speedDiff);
		
		// we may add social interaction forces caused by obstacles here later on
		
		//change the velocity according to the calculated acceleration
		h.setAcceleration(socialForces);
		return h;
		
	}
	
	private Vector2d findPedestrianSocialForce(Human h, double d, Vector2d dv){
		Vector2d socialForce = null;
		// find the acceleration according to social force model
		if(sfp.isCircular()){  //circular social force model (eq.5)
 			socialForce = dv;
			socialForce.normalize(); // find unit vector (d/||d|| )
			socialForce.scale(h.getA()); // multiply A
			socialForce.scale(Math.exp(-1 * (d / h.getB())));	

		}
		else{ //elliptical social force model
			// will be implemented later
		}
		if(sfp.isAngular()){
			// find angular social force
			//Equation 11
			Vector2d curVel = h.getCurrentVelocity();
			curVel.normalize();
			Vector2d distVect = dv;
			distVect.normalize();
			distVect.scale(-1);
			double cosValue = curVel.dot(distVect);
			double lambda = sfp.getLambda();
			double w = lambda + ((1-lambda)* (1+ (cosValue)) /2);		
			// use the prefactor value w for angular dependency of social force
			socialForce.scale(w);
		}
	
		
		return socialForce;
	}

	private Human decideNextDestination(Human h) {
		// check if the gate is visible
		Random r = new Random();
		double currentVisibility = (double) r.nextInt((int)visibility);
		
		
		
		
		double gateDistance = MathFunctions.findDistanceBetweenTwoPoints(h.getPositionPoint(), h.getGatePoint());
		if(currentVisibility > gateDistance){ // gate is visible, set gate as the next and the final destination
			h.setNextDestinationPoint(h.getGatePoint()); 
		}
		double wpToGateDistance = 99999; // set a huge number at the beginning
		int selectedWayPointIndex = -1;
		// have a look at the visible waypoints
		for(int i=0 ;i<wayPointList.size();i++){
			Point p = wayPointList.get(i);
			
			boolean flag=false;
			// check if the waypoint is in a red zone
			for(int j=0;j<tpm.getRedZoneList().size(); j++){
				RedZone tmpRedZone = tpm.getRedZoneList().get(j);
				if(currentSimulationTime >= tmpRedZone.getActiveStartTime() && currentSimulationTime<=tmpRedZone.getActiveEndTime()){
					double distanceToRedZone=MathFunctions.findDistanceBetweenTwoPoints(tmpRedZone.getCenterPoint(),p);
					if(distanceToRedZone <= tmpRedZone.getRadius()){
						// the redzone which will be decided as next destination is in redzone, do not choose it
						flag=true; break;
					}
				}	
			}
			for(int j=0; j<h.getTrajectoryPointList().size();j++){ 
				// check if the point is visited before
				Point visitedPoint = h.getTrajectoryPointList().get(j);
				if(visitedPoint.getX()==p.getX() && visitedPoint.getY() == p.getY()){
					flag=true; break;
				}
			}
			// check the above conditions
			if(flag) continue;
			
			double distance = MathFunctions.findDistanceBetweenTwoPoints(h.getPositionPoint(), p);
			if(distance==0){ // the current position point cannot be selected as the new destination point !
				continue; 
			}
			if(currentVisibility < distance )continue;
			// the point is visible, find its distance to the gate
		    double tmpDistance = MathFunctions.findDistanceBetweenTwoPoints(p, h.getGatePoint());
			if(tmpDistance < wpToGateDistance){ // the waypoint is in a better direction compared to the gate
				wpToGateDistance = tmpDistance;
				selectedWayPointIndex = i;
			}
		}
		if(selectedWayPointIndex==-1){
			//System.out.println("Error: Could not find any waypoint as the next destination !! ");
			// None of the points are visible, select a random visible point as new destination
			h= setRandomNextDestination(h);
		} else{
			h.setNextDestinationPoint(wayPointList.get(selectedWayPointIndex));
		}
		
		return h;
	}

	private Human setRandomNextDestination(Human h) { 
		// select a random next destination point, by selecting a random direction
		double direction = MathFunctions.pickRandomDoubleValueBetweenTwoNumbers(0, 360);
		Point currentPoint = h.getPositionPoint();
		double yPoint = currentPoint.getY() + Math.sin(direction)*randomMoveDistance;
		double xPoint = currentPoint.getX() + Math.cos(direction)*randomMoveDistance;
		Point newDestination = new Point(xPoint, yPoint);
		h.setNextDestinationPoint(newDestination);
		return h;
	}



	public List<Human> getHumanList() {
		return humanList;
	}
	
	

}
