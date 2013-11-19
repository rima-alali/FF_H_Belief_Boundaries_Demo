package demo;


import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;



public class RescueHelicopterEnvEnsemble extends Ensemble {

	@Membership
	public static boolean membership(
			@In("coord.hPos") Double hPos,
			@In("coord.hSpeed") Double hSpeed,
			@In("coord.hGas") Double hGas,
			@In("coord.hBrake") Double hBrake,
			@In("coord.hMoveByOrder") Boolean hMoveByOrder,
			
		
			@In("member.eH2Gas") Double eH2Gas,
			@In("member.eH2Brake") Double eH2Brake,
			@In("member.eH2Pos") Double eH2Pos,
			@In("member.eH2Speed") Double eH2Speed
		){
			return true;
	}
	
	@KnowledgeExchange
	@PeriodicScheduling(50)
	public static void map(
			@Out("coord.hPos")  OutWrapper<Double> hPos,
			@Out("coord.hSpeed")  OutWrapper<Double> hSpeed,
			@In("coord.hGas") Double hGas,
			@In("coord.hBrake") Double hBrake,
		
			@Out("member.eH2Gas")  OutWrapper<Double> eH2Gas,
			@Out("member.eH2Brake")  OutWrapper<Double> eH2Brake,
			@In("member.eH2Pos") Double eH2Pos,
			@In("member.eH2Speed") Double eH2Speed
	) {
	
		eH2Gas.value = hGas;
		eH2Brake.value = hBrake;
		hPos.value = eH2Pos;
		hSpeed.value = eH2Speed;
	}
}