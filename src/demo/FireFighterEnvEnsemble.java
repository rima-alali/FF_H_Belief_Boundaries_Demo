package demo;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class FireFighterEnvEnsemble extends Ensemble {

	@Membership
	public static boolean membership(
			@In("coord.ffPos") Double ffPos,
			@In("coord.ffSpeed") Double ffSpeed,
			@In("coord.ffGas") Double ffGas,
			@In("coord.ffBrake") Double ffBrake,
			@In("coord.ffCreationTime") Double ffCreationTime,

			@In("member.eFFGas") Double eFFGas,
			@In("member.eFFBrake") Double eFFBrake,
			@In("member.eFFPos") Double eFFPos,
			@In("member.eFFSpeed") Double eFFSpeed,
			@In("member.eLastTime") Double eLastTime) {

		return true;
	}

	@KnowledgeExchange
	@PeriodicScheduling(50)
	public static void map(
			@Out("coord.ffPos") OutWrapper<Double> ffPos,
			@Out("coord.ffSpeed") OutWrapper<Double> ffSpeed,
			@In("coord.ffGas") Double ffGas,
			@In("coord.ffBrake") Double ffBrake,
			@Out("coord.ffCreationTime") OutWrapper<Double> ffCreationTime,

			@Out("member.eFFGas") OutWrapper<Double> eFFGas,
			@Out("member.eFFBrake") OutWrapper<Double> eFFBrake,
			@In("member.eFFPos") Double eFFPos,
			@In("member.eFFSpeed") Double eFFSpeed,
			@In("member.eLastTime") Double eLastTime) {

		eFFGas.value = ffGas;
		eFFBrake.value = ffBrake;
		ffPos.value = eFFPos;
		ffSpeed.value = eFFSpeed;
		ffCreationTime.value = eLastTime;
	}
}