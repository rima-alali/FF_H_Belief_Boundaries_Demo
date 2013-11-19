package demo;

import javafx.geometry.HPos;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.MidpointIntegrator;

import com.ibm.jsse2.v;

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
	public Double hRangeDistance = 100.0; // sensors or camera range  

	public Boolean hMove = false;
	public Boolean hOrder = false;
	
	public Double hFFPos = 0.0;
	public Double hFFSpeed = 0.0;
	public Double hFFCreationTime = 0.0;
	public Boolean hFFConnected = false;
	
	public Double hHPos = 0.0;
	public Double hHSpeed = 0.0;
	public Double hHCreationTime = 0.0;

	public Double hFFTargetPos = 0.0;
	public Double hFFTargetSpeed = 0.0;
 
	public Double hHTargetPos = 0.0;
	public Double hHTargetSpeed = 0.0;

	protected static double hLastTime = 0.0;
	protected static double hFFPosMin = 0.0;
	protected static double hFFSpeedMin = 0.0;
	protected static double hFFPosMax = 0.0;
	protected static double hFFSpeedMax = 0.0;

	protected static double hHLastTime = 0.0;
	protected static double hHPosMin = 0.0;
	protected static double hHSpeedMin = 0.0;
	protected static double hHPosMax = 0.0;
	protected static double hHSpeedMax = 0.0;

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
	protected static final double DESIRED_SPEED = 0;
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
			@In("hHCreationTime") Double hHCreationTime,

			@Out("hGas") OutWrapper<Double> hGas,
			@Out("hBrake") OutWrapper<Double> hBrake,

			@InOut("hOrder") OutWrapper<Boolean> hOrder,
			@InOut("hMove") OutWrapper<Boolean> hMove,

			@InOut("hFFPos") OutWrapper<Double> hFFPos, 
			@InOut("hFFSpeed") OutWrapper<Double> hFFSpeed,
			@InOut("hFFCreationTime") OutWrapper<Double> hFFCreationTime,
			@InOut("hFFConnected") OutWrapper<Boolean> hFFConnected,
			@InOut("hFFTargetPos") OutWrapper<Double> hFFTargetPos,
			@InOut("hFFTargetSpeed") OutWrapper<Double> hFFTargetSpeed
 			) {
	
		System.out.println(" - OffloadHelicopter : pos "+hPos+", speed "+hSpeed+"...In the OffloadHelicopter : - firefighter : pos "+hFFPos.value+" ,  speed "+hFFSpeed.value+" , creation time "+hFFCreationTime.value);

		if(hFFCreationTime.value != 0.0){
			double inaccuracy = -1;
			computeBeliefBoundaries(hFFPos.value, hFFSpeed.value, hFFTargetPos.value, hFFCreationTime.value );
		
			if (hFFTargetPos.value != 0.0)
				inaccuracy = Math.max( Math.abs(hFFPos.value  - hFFPosMin), Math.abs(hFFPosMax - hFFPos.value)); 
			
			if (inaccuracy <= THRESHOLD) {
 				hFFTargetPos.value = hPos;
				hFFTargetSpeed.value = hSpeed;
				
			} else if(inaccuracy > THRESHOLD) {
				hFFConnected.value = false;
			}
	
			if(!hFFConnected.value){
				if(hHPos != 0.0)
					computeBeliefBoundariesHelicopter(hHPos, hHSpeed, hHPos, hHCreationTime);
				double hHInaccuracy = -1;
				hHInaccuracy = Math.max( Math.abs(hHPos - hHPosMin), Math.abs(hHPosMax - hHPos)); // do we care if it is so old?????
				hMove.value = hasToMove(hPos,hHPos,hRangeDistance);
	
				if( hOrder.value && hMove.value){
					System.out.println("%%%%%%%%%%     Order RescureHelicopter to move   %%%%%%%%%%%%");
					hFFTargetPos.value = hPos;
					hFFTargetSpeed.value = hSpeed;
				}else if(!hOrder.value && hMove.value){
					if(hFFPosMin > hPos){
						hFFTargetPos.value = hFFPosMax;
						hFFTargetSpeed.value = hFFSpeedMax;
					}else if(hPos > hFFPosMax){
						hFFTargetPos.value = hFFPosMin;
						hFFTargetSpeed.value = hFFSpeedMin;
					} else {			
						hFFTargetPos.value = hFFPosMax;			
						hFFTargetSpeed.value = hFFSpeedMax;		
					}											
					System.out.println("Move.....  from : "+hPos+"  to "+hFFTargetPos.value+",   Min pos: "+hFFPosMin+"   Max pos:"+hFFPosMax);
				} else {
 					System.out.println("Inside the rang : FireFighter connected to RescueHelicopter");
					hFFPos.value = 0.0;
					hFFSpeed.value = 0.0;
					hFFCreationTime.value = 0.0;
					hFFTargetPos.value = hPos; 
					hFFTargetSpeed.value = hSpeed;
				}
			}
		} else {
			
			hFFTargetPos.value = hPos;
			hFFTargetSpeed.value = hSpeed;
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
			if (hFFCreationTime <= hLastTime) {
				startTime = hLastTime;
			} else {
				startTime = hFFCreationTime;
				hFFPosMin = hFFPos;
				hFFPosMax = hFFPos;
				hFFSpeedMin = hFFSpeed;
				hFFSpeedMax = hFFSpeed;
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
		hLastTime = currentTime;
	}



	private static void computeBeliefBoundariesHelicopter( Double fHPos, Double fHSpeed, Double fHTargetPos, Double fHCreationTime ) {

		double currentTime = System.nanoTime() / SEC_NANOSEC_FACTOR;
		double[] minBoundaries = new double[1];
		double[] maxBoundaries = new double[1];
		double startTime = 0.0;

		if(fHTargetPos != 0.0 ) {

			if (fHCreationTime <= hLastTime) {
				startTime = hLastTime;
			} else {
				startTime = fHCreationTime;
				hHPosMin = fHPos;
				hHPosMax = fHPos;
				hHSpeedMin = fHSpeed;
				hHSpeedMax = fHSpeed;
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
		hHLastTime = currentTime;
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
		double distH1 =0.0;
		double distH2 = 0.0;
		
		if(hFFPosMin > hPos){
			distH1 = hFFPosMax - hPos;
		}else if(hPos > hFFPosMax){
			distH1 = hPos - hFFPosMin;
		} else if (hHPosMin > hFFPosMax ){
			distH2 = hHPosMax - hFFPosMin;
		}else if(hFFPosMax > hHPosMax){
			distH2 = hFFPosMax - hHPosMin;
		}
		
		if((distH1 < distH2)  && hPos != 0.0 && hHPos != 0.0 && Math.abs(hPos - hFFPosMax)< (2* hRangeDistance)){
				return true;
		} else if((distH1 > distH2)  && hPos != 0.0 && hHPos != 0.0){
			return false;
		} else if(hHPos == 0.0)
			return true;
		return false;
	}
	
}