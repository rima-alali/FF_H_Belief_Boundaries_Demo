package demo;

import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;



public class HelicopterLeaderEnsemble extends Ensemble {

 	
	@Membership
	public static boolean membership(
			@In("coord.hPos") Double hPos,
			@In("coord.hSpeed") Double hSpeed, 
			@In("coord.hFFPos") Double hFFPos, 
			@In("coord.hFFSpeed") Double hFFSpeed,
			@In("coord.hFFCreationTime") Double hFFCreationTime,
 	
			@In("member.lFFPos") Double lFFPos,
			@In("member.lFFSpeed") Double lFFSpeed,
			@In("member.lFFCreationTime") Double lFFCreationTime
		){
		
		double hCreation= hFFCreationTime == null ? 0.0 : hFFCreationTime;
		double lCreation = lFFCreationTime == null ? 0.0 : lFFCreationTime;
		
		if((hCreation - lCreation)> 0)
			return true;
		return false;
	}
	
	
	@KnowledgeExchange
	@PeriodicScheduling(50)
	public static void map(
			@In("coord.hFFPos") Double hFFPos, 
			@In("coord.hFFSpeed") Double hFFSpeed,
			@In("coord.hFFCreationTime") Double hFFCreationTime,
 	
			@Out("member.lFFPos") OutWrapper<Double> lFFPos,
			@Out("member.lFFSpeed") OutWrapper<Double> lFFSpeed,
			@Out("member.lFFCreationTime") OutWrapper<Double> lFFCreationTime
	) {
	
			lFFPos.value = hFFPos;
			lFFSpeed.value = hFFSpeed;
			lFFCreationTime.value = hFFCreationTime;
 			System.err.println("____________________________ save firefighter statue in the leader through helicopter ________________________");
	}	
}