package demo;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class FireFighterHelicopterEnsemble extends Ensemble {

	@Membership
	public static boolean membership(
			@In("coord.ffPos") Double ffPos,
			@In("coord.ffSpeed") Double ffSpeed,
			@In("coord.ffCreationTime") Double ffCreationTime,
			@In("coord.ffLastCommunication") Double ffLastCommunication,

			@In("member.hPos") Double hPos,
			@In("member.hRangeDistance") Double hRangeDistance,
			@In("member.hFFConnected") Boolean hFFConnected,
			@In("member.hFFPos") Double hFFPos,
			@In("member.hFFSpeed") Double hFFSpeed,
			@In("member.hFFCreationTime") Double hFFCreationTime) {

		double currentTime = (double) System.nanoTime() / 1000000000;
		double dist = (Math.abs(hPos - ffPos));
		boolean conn = hFFConnected == null ? false : hFFConnected;

		if (dist < 2 * hRangeDistance && (currentTime - ffLastCommunication) > 1 && ffLastCommunication != 0.0 && !conn && hFFPos < 800) {
			System.err.println("###delay :" + (currentTime - ffLastCommunication)
					+ "  helicopter at pos: " + hPos + "  try to connect firefighter pos: "+ffPos
					+"  . current time = "+ currentTime + "     ffLastCommunication : "+ ffLastCommunication);
			return true;
		} else if (conn && hFFPos < 1000){
			System.err.println("Helicopter at Pos : "+hPos+" connected to firefighter :"+ffPos);
			return true;
		}
		
		return false;

	}

	@KnowledgeExchange
	@PeriodicScheduling(50)
	public static void map(
			@In("coord.ffPos") Double ffPos,
			@In("coord.ffSpeed") Double ffSpeed,
			@In("coord.ffCreationTime") Double ffCreationTime,
			@Out("coord.ffLastCommunication") OutWrapper<Double> ffLastCommunication,

			@Out("member.hFFPos") OutWrapper<Double> hFFPos,
			@Out("member.hFFSpeed") OutWrapper<Double> hFFSpeed,
			@Out("member.hFFConnected") OutWrapper<Boolean> hFFConnected,
			@Out("member.hFFCreationTime") OutWrapper<Double> hFFCreationTime) {

		hFFPos.value = ffPos;
		hFFSpeed.value = ffSpeed;
		hFFCreationTime.value = ffCreationTime;
		hFFConnected.value = true;
		ffLastCommunication.value = ffCreationTime;
	}
}