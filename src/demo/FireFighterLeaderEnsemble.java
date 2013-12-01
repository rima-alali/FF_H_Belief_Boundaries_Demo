package demo;


import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;



public class FireFighterLeaderEnsemble extends Ensemble {

	protected static int n = 0;
 
	@Membership
	public static boolean membership(
			@In("coord.ffPos") Double ffPos,
			@In("coord.ffSpeed") Double ffSpeed,
			@In("coord.ffCreationTime") Double ffCreationTime,
			@In("coord.ffLastCommunication") Double ffLastCommunication,
			
			@In("member.lPos") Double lPos,
			@In("member.lSpeed") Double lSpeed,
			@In("member.lFFPos") Double lFFPos,
			@In("member.lFFSpeed") Double lFFSpeed,
			@In("member.lFFCreationTime") Double lFFCreationTime
 		){

		if( ffPos <= 500 ){
			System.err.println("FireFighter connected to Leader directly : creation time : "+ffCreationTime);
			n++;
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
			
			@In("member.lPos") Double lPos,
			@In("member.lSpeed") Double lSpeed,
			@Out("member.lFFPos") OutWrapper<Double> lFFPos,
			@Out("member.lFFSpeed") OutWrapper<Double> lFFSpeed,
			@Out("member.lFFCreationTime") OutWrapper<Double> lFFCreationTime
	) {
		lFFPos.value = ffPos;
		lFFSpeed.value = ffSpeed;
		lFFCreationTime.value = ffCreationTime;
		ffLastCommunication.value = ffCreationTime;
	}
}