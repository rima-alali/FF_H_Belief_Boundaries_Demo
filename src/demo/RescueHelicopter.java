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


public class RescueHelicopter extends Component {

	public String hName;
	public Double hPos = 0.0;
	public Double hSpeed = 0.0;
	public Double hGas = 0.0;
	public Double hBrake = 0.0;
	public Double hCreationTime = 0.0;
	public Double hRangeDistance = 100.0; // sensors or camera range  
	public Boolean hMove = false;
	public Boolean hMoveByOrder = false;

	public Double hFFPos = 0.0;
	public Double hFFSpeed = 0.0;
	public Double hFFCreationTime = 0.0;
	public Boolean hFFLost = false;
	public Boolean hFFConnected = false;
	
	public Double hFFTargetPos = 0.0;
	public Double hFFTargetSpeed = 0.0;
 
	protected static double hFFPosMin = 0.0;
	protected static double hFFSpeedMin = 0.0;
	protected static double hFFPosMax = 0.0;
	protected static double hFFSpeedMax = 0.0;

	protected static double hPosTarget = 0.0;
	protected static double hSpeedTarget = 0.0;

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

	public RescueHelicopter() {
		hName = "H2";
	}

	@Process
	@PeriodicScheduling((int) TIMEPERIOD)
	public static void computeTarget(
			@In("hPos") Double hPos,
			@In("hSpeed") Double hSpeed, 
			@In("hRangeDistance") Double hRangeDistance,

			@In("hFFPos") Double hFFPos, 
			@In("hFFSpeed") Double hFFSpeed,
			@In("hFFCreationTime") Double hFFCreationTime,

			@Out("hGas") OutWrapper<Double> hGas,
			@Out("hBrake") OutWrapper<Double> hBrake,

			@InOut("hMove") OutWrapper<Boolean> hMove,
			@InOut("hMoveByOrder") OutWrapper<Boolean> hMoveByOrder,
 			@InOut("hFFConnected") OutWrapper<Boolean> hFFConnected,
 			@InOut("hFFLost") OutWrapper<Boolean> hFFLost,

			@InOut("hFFTargetPos") OutWrapper<Double> hFFTargetPos,
			@InOut("hFFTargetSpeed") OutWrapper<Double> hFFTargetSpeed
 			) {
	

		System.out.println(" - RescueHelicopter : pos "+hPos+", speed "+hSpeed+"...In the RescueHelicopter : - firefighter : pos "+hFFPos+" ,  speed "+hFFSpeed+" , creation time "+hFFCreationTime);
		if(hFFCreationTime != 0.0){
			double inaccuracy = -1;
			computeBeliefBoundaries(hFFPos, hFFSpeed, hFFTargetPos.value, hFFCreationTime);
		
			if (hFFTargetPos.value != 0.0)
				inaccuracy = Math.max( Math.abs(hFFPos  - hFFPosMin), Math.abs(hFFPosMax - hFFPos)); 
			
			if (inaccuracy <= THRESHOLD ) {
				hMove.value = false;
				hFFLost.value = false;
			} else 
			if(inaccuracy > THRESHOLD) {
				hFFConnected.value = false;
				hMove.value = true;
				hFFLost.value = true;
			}
			if(!hFFConnected.value && inaccuracy >= 0 && hFFLost.value){
				if(hMoveByOrder.value || hMove.value){
					if(hFFPosMin > hPos){
						hFFTargetPos.value = hFFPosMax;
						hFFTargetSpeed.value = hFFSpeedMax;
					}else if(hPos > hFFPosMax){
						hFFTargetPos.value = hPos;
						hFFTargetSpeed.value = DESIRED_SPEED;
					} else {			
						hFFTargetPos.value = hFFPosMax;			
						hFFTargetSpeed.value = hFFSpeedMax;	
					}											
					System.err.println("H2_Move.....  from : "+hPos+"  to "+hFFTargetPos.value+",   Min pos: "+hFFPosMin+"  Max pos:"+hFFPosMax);
				}else if(!hMove.value){
					hFFTargetPos.value = hPos; 
					hFFTargetSpeed.value = DESIRED_SPEED;
					System.err.println("H2_%%%%%%%%%%     OffloadHelicopter moves toward FireFighter   %%%%%%%%%%%%");
				}else if(!hMove.value){
					hFFTargetPos.value = hPos; 
					hFFTargetSpeed.value = DESIRED_SPEED;
					System.err.println("H2_OffloadHelicopter order RescueHelicopter to move ......");
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


	private static void computeBeliefBoundaries( Double hFFPos, Double hFFSpeed, Double hfFFTargetPos, Double hFFCreationTime ) {

		double currentTime = System.nanoTime() / SEC_NANOSEC_FACTOR;
		double[] minBoundaries = new double[1];
		double[] maxBoundaries = new double[1];
		double startTime = 0.0;
		
		if(hfFFTargetPos != 0.0 ) {
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

	
	private static void target(double hPos) {
		
		if(hFFPosMin > hPos){
			hPosTarget = hFFPosMax;
			hSpeedTarget = hFFSpeedMax;
		}else if(hPos > hFFPosMax){
			hPosTarget = hFFPosMin;
			hSpeedTarget = hFFSpeedMin;
		}
		
	}
}