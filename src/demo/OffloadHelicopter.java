package demo;


import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.MidpointIntegrator;


import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class OffloadHelicopter extends Component {

	public String hName;
	public Double hPos = 0.0;
	public Double hSpeed = 0.0;
	public Double hGas = 0.0;
	public Double hBrake = 0.0;
	public Boolean hMove = false;
	public Boolean hOrder = false;
	public Double hRangeDistance = 100.0; 

	
	public Double hFFPos = 0.0;
	public Double hFFSpeed = 0.0;
	public Double hFFCreationTime = 0.0;
	public Boolean hFFConnected = false;
	public Boolean hFFLost = false;
	
	public Double hHPos = 0.0;
	public Double hHSpeed = 0.0;
	public Boolean hHConnected = false;
	public Double hHCreationTime = 0.0;
	

	public Double hFFTargetPos = 0.0;
	public Double hFFTargetSpeed = 0.0;
 
	public Double hHTargetPos = 0.0;
	public Double hHTargetSpeed = 0.0;

	protected static double hFFPosMin = 0.0;
	protected static double hFFSpeedMin = 0.0;
	protected static double hFFPosMax = 0.0;
	protected static double hFFSpeedMax = 0.0;

	protected static double hHLastTime = 0.0;
	protected static double hHInit = 0.0;
	protected static double hHPosMin = 0.0;
	protected static double hHSpeedMin = 0.0;
	protected static double hHPosMax = 0.0;
	protected static double hHSpeedMax = 0.0;

	protected static double hInit = 0.0;
	protected static double hLastTime = 0.0;
	protected static double hIntegratorError = 0.0;
	protected static double hErrorWindup = 0.0;

	protected static final double KP_D = 0.193;
	protected static final double KP_S = 0.12631;
	protected static final double KI_S = 0.001;
	protected static final double KT_S = 0.01;
	protected static final double SEC_NANOSEC_FACTOR = 1000000000;
	protected static final double TIMEPERIOD = 100;
	protected static final double SEC_MILISEC_FACTOR = 1000;
	protected static final double DESIRED_DISTANCE = 0;
	protected static final double DESIRED_SPEED = 10;
	protected static final double SPEED_UPPER_LIMIT = 200;
	protected static final double THRESHOLD = 200;

	public OffloadHelicopter() {
		hName = "H";
	}

	@Process
	@PeriodicScheduling((int) TIMEPERIOD)
	public static void computeTarget(
			@In("hPos") Double hPos,
			@In("hSpeed") Double hSpeed, 
			@In("hRangeDistance") Double hRangeDistance,
	

			@In("hHPos") Double hHPos, 
			@In("hHSpeed") Double hHSpeed,
			@In("hHConnected") Boolean hHConnected,
			@In("hHCreationTime") Double hHCreationTime,

			@Out("hGas") OutWrapper<Double> hGas,
			@Out("hBrake") OutWrapper<Double> hBrake,

			@InOut("hOrder") OutWrapper<Boolean> hOrder,
			@InOut("hMove") OutWrapper<Boolean> hMove,

			@InOut("hFFPos") OutWrapper<Double> hFFPos, 
			@InOut("hFFSpeed") OutWrapper<Double> hFFSpeed,
			@InOut("hFFCreationTime") OutWrapper<Double> hFFCreationTime,
			@InOut("hFFLost") OutWrapper<Boolean> hFFLost,
			@InOut("hFFConnected") OutWrapper<Boolean> hFFConnected,
			@InOut("hFFTargetPos") OutWrapper<Double> hFFTargetPos,
			@InOut("hFFTargetSpeed") OutWrapper<Double> hFFTargetSpeed
 			) {
	
		System.out.println(" - OffloadHelicopter : pos "+hPos+", speed "+hSpeed+".."+hFFTargetSpeed.value+".In the OffloadHelicopter : - firefighter : pos "+hFFPos.value+" ,  speed "+hFFSpeed.value+" , creation time "+hFFCreationTime.value);
		if(hFFCreationTime.value != 0.0){
			double inaccuracy = -1;
			computeBeliefBoundaries(hFFPos.value, hFFSpeed.value, hFFTargetPos.value, hFFCreationTime.value);
		
			if (hFFTargetPos.value != 0.0)
				inaccuracy = Math.max( Math.abs(hFFPos.value  - hFFPosMin), Math.abs(hFFPosMax - hFFPos.value)); 
			
			if (inaccuracy <= THRESHOLD ) {
				hFFLost.value = false;
			} else 
			if(inaccuracy > THRESHOLD) {
				hFFConnected.value = false;
				hFFLost.value = true;
			}

			if(!hFFConnected.value && inaccuracy >= 0 && hHConnected){
				if(hHPos != 0.0)
					computeBeliefBoundariesHelicopter(hHPos, hHSpeed, hHPos, hHCreationTime);
				hMove.value = hasToMove(hPos,hHPos,hRangeDistance);
				if(hMove.value && hOrder.value){
					hFFPos.value = 0.0;
					hFFSpeed.value = 0.0;
					hFFCreationTime.value = 0.0;
					clearFF();
					hFFLost.value = false;
					hFFTargetPos.value = hPos; 
					hFFTargetSpeed.value = DESIRED_SPEED;
					System.err.println("%%%%%%%%%%     RescureHelicopter connected to the FireFighter   %%%%%%%%%%%%");
				}else if(hMove.value && !hOrder.value){
					if(hFFPosMin > hPos){
						hFFTargetPos.value = hFFPosMax;
						hFFTargetSpeed.value = hFFSpeedMax;
					}else if(hPos > hFFPosMax){
						hFFTargetPos.value = hPos  + hRangeDistance;
						hFFTargetSpeed.value = DESIRED_SPEED;
					} else {			
						hFFTargetPos.value = hFFPosMax;			
						hFFTargetSpeed.value = hFFSpeedMax;	
					}											
					System.err.println("Move.....  from : "+hPos+"  to "+hFFTargetPos.value+",   Min pos: "+hFFPosMin+"  Max pos:"+hFFPosMax);
				}else if(!hMove.value && hOrder.value){
					hFFPos.value = 0.0;
					hFFSpeed.value = 0.0;
					hFFCreationTime.value = 0.0;
					clearFF();
					hFFLost.value = false;
					hFFTargetPos.value = hPos; 
					hFFTargetSpeed.value = DESIRED_SPEED;
					System.err.println("%%%%%%%%%%     RescureHelicopter connected to the FireFighter   %%%%%%%%%%%%");
				}else if(!hMove.value && !hOrder.value){
					hFFPos.value = 0.0;
					hFFSpeed.value = 0.0;
					hFFCreationTime.value = 0.0;
					clearFF();
					hFFLost.value = false;
					hFFTargetPos.value = hPos; 
					hFFTargetSpeed.value = DESIRED_SPEED;
					System.err.println("OffloadHelicopter order RescueHelicopter to move ......");
				}
			}else{
				hFFTargetPos.value = hPos; 
				hFFTargetSpeed.value = DESIRED_SPEED;
			}
		}else{
			hFFTargetPos.value = hPos; 
			hFFTargetSpeed.value = DESIRED_SPEED;
		}

		Pedal p = speedControl(hPos, hSpeed, hFFTargetPos.value, hFFTargetSpeed.value);
		hGas.value = p.gas;
		hBrake.value = p.brake;
	}
	

	private static Pedal speedControl( Double ffPos, Double ffSpeed, Double hFFTargetPos, Double hFFTargetSpeed ) {

		Pedal result = null;
		if (hFFTargetPos == 0.0) {
			result = new Pedal(0.0, 0.0);
		} else {
			double timePeriodInSeconds = TIMEPERIOD / SEC_MILISEC_FACTOR;
			double distanceError = -DESIRED_DISTANCE + hFFTargetPos - ffPos;
			double pidDistance = KP_D * distanceError;
			double error = pidDistance + hFFTargetSpeed - ffSpeed;
			hIntegratorError += (KI_S * error + KT_S * hErrorWindup)
					* timePeriodInSeconds;
			double pidSpeed = KP_S * error + hIntegratorError;
			hErrorWindup = saturate(pidSpeed) - pidSpeed;
			
			if (pidSpeed >= 0) {
				result = new Pedal(pidSpeed, 0.0);
			} else {
				result = new Pedal(0.0, -pidSpeed);
			}
		}
		
		return result;
	}


	private static void computeBeliefBoundaries( Double hFFPos, Double hFFSpeed, Double hFFTargetPos, Double hFFCreationTime ) {

		double currentTime = System.nanoTime() / SEC_NANOSEC_FACTOR;
		double[] minBoundaries = new double[1];
		double[] maxBoundaries = new double[1];
		double startTime = 0.0;
		
		if(hFFTargetPos != 0.0 ) {
			if(hFFCreationTime > hInit){
				startTime = hFFCreationTime;
				hFFPosMin = hFFPos;
				hFFPosMax = hFFPos;
				hFFSpeedMin = hFFSpeed;
				hFFSpeedMax = hFFSpeed;
			}else {
				if (hFFCreationTime <= hLastTime) {
					startTime = hLastTime;
				} else if(hFFCreationTime > hLastTime){
					startTime = hFFCreationTime;
					hFFPosMin = hFFPos;
					hFFPosMax = hFFPos;
					hFFSpeedMin = hFFSpeed;
					hFFSpeedMax = hFFSpeed;
				}
			}
			
			// ---------------------- knowledge evaluation --------------------------------

			double accMin = Database.getAcceleration(hFFSpeedMin,
					hFFPosMin, Database.lTorques, 0.0, 1.0,
					Database.lMass);
			double accMax = Database.getAcceleration(hFFSpeedMax,
					hFFPosMax, Database.lTorques, 1.0, 0.0,
					Database.lMass);

			FirstOrderIntegrator integrator = new MidpointIntegrator(1);
			integrator.setMaxEvaluations((int) TIMEPERIOD);
			FirstOrderDifferentialEquations f = new Derivation(); 
			// ------------- min ----------------------

			minBoundaries[0] = accMin;
			integrator.integrate(f, startTime, minBoundaries, currentTime, minBoundaries);
			hFFSpeedMin += minBoundaries[0];
			integrator.integrate(f, startTime, minBoundaries, currentTime, minBoundaries);
			hFFPosMin += minBoundaries[0];
			// ------------- max ----------------------

			maxBoundaries[0] = accMax;
			integrator.integrate(f, startTime, maxBoundaries, currentTime, maxBoundaries);
			hFFSpeedMax += maxBoundaries[0];
			integrator.integrate(f, startTime, maxBoundaries, currentTime, maxBoundaries);
			hFFPosMax += maxBoundaries[0];

		}
		if(hFFSpeedMax > 200 ) hFFSpeedMax = 200.0;
		if(hFFSpeedMin < 0.0) hFFSpeedMin = 0.0;
		if(hFFPosMin < 0.0) hFFPosMin = 0.0;
		hLastTime = currentTime;
		hInit = hFFCreationTime;
	}



	private static void computeBeliefBoundariesHelicopter( Double hHPos, Double hHSpeed, Double hHTargetPos, Double hHCreationTime ) {

			double currentTime = System.nanoTime() / SEC_NANOSEC_FACTOR;
			double[] minBoundaries = new double[1];
			double[] maxBoundaries = new double[1];
			double startTime = 0.0;
			
			if(hHTargetPos != 0.0 ) {
				if(hHCreationTime > hHInit){
					startTime = hHCreationTime;
					hHPosMin = hHPos;
					hHPosMax = hHPos;
					hHSpeedMin = hHSpeed;
					hHSpeedMax = hHSpeed;
				}else {
					if (hHCreationTime <= hHLastTime) {
						startTime = hHLastTime;
					} else if(hHCreationTime > hHLastTime){
						startTime = hHCreationTime;
						hHPosMin = hHPos;
						hHPosMax = hHPos;
						hHSpeedMin = hHSpeed;
						hHSpeedMax = hHSpeed;
					}
				}
				
				// ---------------------- knowledge evaluation --------------------------------
				double accMin = Database.getAcceleration(hHSpeedMin,
						hHPosMin, Database.lTorques, 0.0, 1.0,
						Database.lMass);
				double accMax = Database.getAcceleration(hHSpeedMax,
						hHPosMax, Database.lTorques, 1.0, 0.0,
						Database.lMass);

				FirstOrderIntegrator integrator = new MidpointIntegrator(1);
				integrator.setMaxEvaluations((int) TIMEPERIOD);
				FirstOrderDifferentialEquations f = new Derivation(); 
				// ------------- min ----------------------

				minBoundaries[0] = accMin;
				integrator.integrate(f, startTime, minBoundaries, currentTime, minBoundaries);
				hHSpeedMin += minBoundaries[0];
				integrator.integrate(f, startTime, minBoundaries, currentTime, minBoundaries);
				hHPosMin += minBoundaries[0];
				// ------------- max ----------------------

				maxBoundaries[0] = accMax;
				integrator.integrate(f, startTime, maxBoundaries, currentTime, maxBoundaries);
				hHSpeedMax += maxBoundaries[0];
				integrator.integrate(f, startTime, maxBoundaries, currentTime, maxBoundaries);
				hHPosMax += maxBoundaries[0];

			}
			if(hHSpeedMax > 200 ) hHSpeedMax = 200.0;
			if(hHSpeedMin < 0.0) hHSpeedMin = 0.0;
			if(hHPosMin < 0.0) hHPosMin = 0.0;
			hHLastTime = currentTime;
			hHInit = hHCreationTime;
		}


	private static double saturate(double val) {
		if (val > 1)
			val = 1;
		else if (val < -1)
			val = -1;
		return val;
	}

	
	private static class Derivation implements FirstOrderDifferentialEquations {

		@Override
		public int getDimension() {
			return 1;
		}

		@Override
		public void computeDerivatives(double t, double[] y, double[] yDot)
				throws MaxCountExceededException, DimensionMismatchException {
			int params = 1;
			int order = 1;
			DerivativeStructure x = new DerivativeStructure(params, order, 0, y[0]);
			DerivativeStructure f = x.divide(t);
			yDot[0] = f.getValue();
		}
	}
	
	private static class Pedal{
		public Double gas;
		public Double brake;
	
		public Pedal(Double gas,Double brake) {
			this.gas = gas;
			this.brake = brake;
		}	
	}

	
	private static Boolean hasToMove(double hPos, double hHPos, double hRangeDistance) {
		boolean result = false;
		double distH1 = 0.0;
		double distH2 = 0.0;
		if(hFFPosMin > hPos){
			distH1 = hFFPosMax - hPos;
		}else if(hPos > hFFPosMax){
			distH1 = hPos - hFFPosMin;
		}else {
			distH1 = Math.max(hFFPosMax - hPos, hPos - hFFPosMin);
		}
		
		if (hHPosMin > hFFPosMax ){
			distH2 = hHPosMax - hFFPosMin;
		}else if(hFFPosMin > hHPosMax){
			distH2 = hFFPosMax - hHPosMin;
		}else{
			distH2 = Math.max(hHPosMax - hFFPosMin, hFFPosMax - hHPosMin);
		}
		
		if(hHPos == 0.0)
			result = true;
		else if((distH1 < distH2)  && hPos != 0.0 && hHPos != 0.0 && distH1 < 2*hRangeDistance ){
				result = true;
		} else if((distH1 > distH2)  && hPos != 0.0 && hHPos != 0.0 && distH2 < 2*hRangeDistance ){
			result = false;
		} else if((distH1 < distH2)  && hPos != 0.0 && hHPos != 0.0){
			result = true;
		} else result = false;
		
		return result;
	}
	
	private static void clearFF(){
		hFFPosMin = 0.0;
		hFFPosMax = 0.0;
		hFFSpeedMin = 0.0;
		hFFSpeedMax = 0.0;
	}
	
	private static void clearH(){
		hHPosMin = 0.0;
		hHPosMax = 0.0;
		hHSpeedMin = 0.0;
		hHSpeedMax = 0.0;
	}
}