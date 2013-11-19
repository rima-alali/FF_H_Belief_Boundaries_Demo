package demo;


import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;


public class Environment extends Component {

	public String eName = "E";
	public Double eLGas = 0.0;      
	public Double eLBrake = 0.0;    
 	public Double eFFGas = 0.0;    
	public Double eFFBrake = 0.0;  
 	public Double eHGas = 0.0;    
	public Double eHBrake = 0.0;  
 	public Double eH2Gas = 0.0;    
	public Double eH2Brake = 0.0;  

 	public Double eLPos = 0.0;
	public Double eLSpeed = 0.0;
	public Double eFFPos = 0.0;
	public Double eFFSpeed = 0.0; 
	public Double eHPos = 700.0;
	public Double eHSpeed = 0.0; 
	public Double eH2Pos = 1400.0;
	public Double eH2Speed = 0.0; 
	public Double eLastTime = 0.0;

	
	protected static final double TIMEPERIOD = 100;
	protected static final double SEC_NANOSECOND_FACTOR = 1000000000;
	protected static final double SEC_MILISEC_FACTOR = 1000;
	
	
	
	public Environment() {
	}	
	
	@Process 
	@PeriodicScheduling((int) TIMEPERIOD)
	public static void environmentResponse(
			@In("eLGas") Double eLGas,
			@In("eLBrake") Double eLBrake,
 			@In("eFFGas") Double eFFGas,
			@In("eFFBrake") Double eFFBrake,
			@In("eHGas") Double eHGas,
			@In("eHBrake") Double eHBrake,
			@In("eH2Gas") Double eH2Gas,
			@In("eH2Brake") Double eH2Brake,
			
			@InOut("eLPos") OutWrapper<Double> eLPos,			 
			@InOut("eLSpeed") OutWrapper<Double> eLSpeed,
 			@InOut("eFFPos") OutWrapper<Double> eFFPos,			 
			@InOut("eFFSpeed") OutWrapper<Double> eFFSpeed,
 			@InOut("eHPos") OutWrapper<Double> eHPos,			 
			@InOut("eHSpeed") OutWrapper<Double> eHSpeed,
 			@InOut("eH2Pos") OutWrapper<Double> eH2Pos,			 
			@InOut("eH2Speed") OutWrapper<Double> eH2Speed,
			
			@Out("eLastTime") OutWrapper<Double> eLastTime
			){
	
		double currentTime = System.nanoTime()/SEC_NANOSECOND_FACTOR;
		double timePeriodInSeconds = TIMEPERIOD/SEC_MILISEC_FACTOR;
		
		// ----------------------- leader ----------------------------------------------------------------------
		double lAcceleration = Database.getAcceleration(eLSpeed.value, eLPos.value, Database.lTorques, eLGas, eLBrake, Database.lMass);
		eLSpeed.value += lAcceleration * timePeriodInSeconds;
		eLPos.value += eLSpeed.value * timePeriodInSeconds;
  		//------------------------ FireFighter ---------------------------------------------------------------------
		double ffAcceleration = Database.getAcceleration(eFFSpeed.value, eFFPos.value, Database.fTorques, eFFGas, eFFBrake,Database.fMass);
		eFFSpeed.value += ffAcceleration * timePeriodInSeconds; 
		eFFPos.value += eFFSpeed.value * timePeriodInSeconds;
  		//------------------------ Helicopter ---------------------------------------------------------------------
		double hAcceleration = Database.getAcceleration(eHSpeed.value, eHPos.value, Database.fTorques, eHGas, eHBrake,Database.fMass);
		eHSpeed.value += hAcceleration * timePeriodInSeconds; 
		eHPos.value += eHSpeed.value * timePeriodInSeconds;
  		//------------------------ Helicopter2 ---------------------------------------------------------------------
		double h2Acceleration = Database.getAcceleration(eH2Speed.value, eH2Pos.value, Database.fTorques, eH2Gas, eH2Brake,Database.fMass);
		eH2Speed.value += h2Acceleration * timePeriodInSeconds; 
		eH2Pos.value += eH2Speed.value * timePeriodInSeconds;
		//--------------------------------------------------------------------------------------------------------
		 		
		eLastTime.value = currentTime;
//		System.out.println("=================================== full statue ==========================================");
//		System.out.println("Speed leader : "+eLSpeed.value+", pos : "+eLPos.value+"... time :"+currentTime);
// 		System.out.println("Speed FireFighter : "+eFFSpeed.value+", pos : "+eFFPos.value+"... time :"+currentTime);
// 		System.out.println("Speed OffloadHelicopter : "+eHSpeed.value+", pos : "+eHPos.value+"... time :"+currentTime);
//		System.out.println("Speed RescueHelicopter : "+eH2Speed.value+", pos : "+eH2Pos.value+"... time :"+currentTime);
//		System.out.println("==========================================================================================");
	}
}