package demo;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class FireFighter extends Component {

	public String ffName;

	public Double ffPos = 0.0;
	public Double ffSpeed = 0.0;
	public Double ffGas = 0.0;
	public Double ffBrake = 0.0;
	public Double ffCreationTime = 0.0;
	public Double ffLastCommunication = 0.0;
 
	public Double ffIntegratorSpeedError = 0.0;
	public Double ffErrorWindup = 0.0;

	protected static final double KP = 0.05;
	protected static final double KI = 0.000228325;
	protected static final double KT = 0.01;
	protected static final double DISEREDSPEED = 50;
	protected static final double TIMEPERIOD = 100;
	protected static final double SEC_MILI_SEC_FACTOR = 1000;

	public FireFighter() {
		ffName = "FireFighter";
	}

	@Process
	@PeriodicScheduling((int) TIMEPERIOD)
	public static void speedControl(
			@In("ffPos") Double ffPos,
			@In("ffSpeed") Double ffSpeed,
			@In("ffCreationTime") Double ffCreationTime,

			@Out("ffGas") OutWrapper<Double> ffGas,
			@Out("ffBrake") OutWrapper<Double> ffBrake,

			@InOut("ffIntegratorSpeedError") OutWrapper<Double> ffIntegratorSpeedError,
			@InOut("ffErrorWindup") OutWrapper<Double> ffErrorWindup
			) {
		
		System.out.println(" - FireFighter : pos "+ffPos+", speed : "+ffSpeed+"... time :"+ffCreationTime);
		double timePeriodInSeconds = TIMEPERIOD / SEC_MILI_SEC_FACTOR;
		double speedError = DISEREDSPEED - ffSpeed; 
		ffIntegratorSpeedError.value += (KI * speedError + KT * ffErrorWindup.value) * timePeriodInSeconds;
		double pid = KP * speedError + ffIntegratorSpeedError.value;
		ffErrorWindup.value = saturate(pid) - pid;
		
		if (pid >= 0) {
			ffGas.value = pid;
			ffBrake.value = 0.0;
		} else {
			ffGas.value = 0.0;
			ffBrake.value = -pid;
		}
		
	}

	
	private static double saturate(double val) {
		if (val > 1)
			val = 1;
		else if (val < -1)
			val = -1;
		return val;
	}

}