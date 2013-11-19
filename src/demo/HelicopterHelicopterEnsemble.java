package demo;


import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;



public class HelicopterHelicopterEnsemble extends Ensemble {


	@Membership
	public static boolean membership(
			@In("coord.hPos") Double hPos,
			@In("coord.hSpeed") Double hSpeed,
			@In("coord.hFFConnected") Boolean hFFConnected,
			@In("coord.hMove") Boolean hMove,
			@In("coord.hOrder") Boolean hOrder,
			@In("coord.hFFPos") Double hFFPos,
			@In("coord.hFFSpeed") Double hFFSpeed,
			@In("coord.hFFCreationTime") Double hFFCreationTime,
			@In("coord.hHPos") Double hHPos,
			@In("coord.hHSpeed") Double hHSpeed,
			
			@In("member.hPos") Double mHPos,
			@In("member.hSpeed") Double mHSpeed,
			@In("member.hMoveByOrder") Boolean mHMoveByOrder,
			@In("coord.hFFConnected") Boolean mHFFConnected,
			@In("member.hFFPos") Double mHFFPos,
			@In("member.hFFSpeed") Double mHFFSpeed,
			@In("member.hFFCreationTime") Double mHFFCreationTime
 
			){

		double mcreation = mHFFCreationTime == null ? 0.0 : mHFFCreationTime;
		if( !hFFConnected && (hFFCreationTime < mcreation) )
			return true;
		
		return false;
	}
	
	@KnowledgeExchange
	@PeriodicScheduling(50)
	public static void map(
			@In("coord.hMove") Boolean hMove,
			@In("coord.hFFPos") Double hFFPos,
			@In("coord.hFFSpeed") Double hFFSpeed,
			@In("coord.hFFCreationTime") Double hFFCreationTime,
			@Out("coord.hOrder") OutWrapper<Boolean> hOrder,
			@Out("coord.hHPos") OutWrapper<Double> hHPos,
			@Out("coord.hHSpeed") OutWrapper<Double> hHSpeed,
			
			@In("member.hPos") Double mHPos,
			@In("member.hSpeed") Double mHSpeed,
			@In("member.hFFConnected") Boolean mHFFConnected,
			@Out("member.hMoveByOrder") OutWrapper<Boolean> mHMoveByOrder,
			@Out("member.hFFPos") OutWrapper<Double> mHFFPos,
			@Out("member.hFFSpeed") OutWrapper<Double> mHFFSpeed,
			@Out("member.hFFCreationTime") OutWrapper<Double> mHFFCreationTime
	) {
		
	if( hMove && mHFFConnected){
		System.err.println("OffloadHelicopter contact RescueHelicopter......");
		hOrder.value = true;
		mHMoveByOrder.value = false;
	}else if( hMove && !mHFFConnected){
		System.err.println("OffloadHelicopter contact RescueHelicopter......");
		hOrder.value = true;
		mHMoveByOrder.value = true;
	} else 	if( !hMove && mHFFConnected){
		hOrder.value = false;
		mHMoveByOrder.value = false;
	}else if( !hMove && !mHFFConnected){
		System.err.println("OffloadHelicopter contact RescueHelicopter......");
		hOrder.value = true;
		mHMoveByOrder.value = true;
	}
	mHFFPos.value = hFFPos;
	mHFFSpeed.value = hFFSpeed;
	mHFFCreationTime.value = hFFCreationTime;
	hHPos.value = mHPos;
	hHSpeed.value = mHSpeed;
	
	}
}