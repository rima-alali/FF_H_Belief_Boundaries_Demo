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
			@In("coord.hFFLost") Boolean hFFLost,
			@In("coord.hHPos") Double hHPos,
			@In("coord.hHSpeed") Double hHSpeed,
			@In("coord.hHConnected") Boolean hHConnected,
					
			@In("member.hPos") Double mHPos,
			@In("member.hSpeed") Double mHSpeed,
			@In("member.hCreationTime") Double hCreationTime,
			@In("member.hMoveByOrder") Boolean mHMoveByOrder,
			@In("member.hFFConnected") Boolean mHFFConnected,
			@In("member.hFFPos") Double mHFFPos,
			@In("member.hFFSpeed") Double mHFFSpeed,
			@In("member.hFFCreationTime") Double mHFFCreationTime
 
			){

		boolean h1_conn = hFFConnected == null ? false : hFFConnected;
		double h1_ff_pos = hFFPos == null ? -1.0 : hFFPos;

		if( !h1_conn && h1_ff_pos > 0.0 && hFFLost)
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
			@Out("coord.hHConnected") OutWrapper<Boolean> hHConnected,
			@Out("coord.hHCreationTime") OutWrapper<Double> hHCreationTime,
					
			@In("member.hPos") Double mHPos,
			@In("member.hSpeed") Double mHSpeed,
			@In("member.hCreationTime") Double mHCreationTime,
			@In("member.hFFConnected") Boolean mHFFConnected,
			@Out("member.hMoveByOrder") OutWrapper<Boolean> mHMoveByOrder,
			@Out("member.hFFPos") OutWrapper<Double> mHFFPos,
			@Out("member.hFFSpeed") OutWrapper<Double> mHFFSpeed,
			@Out("member.hFFCreationTime") OutWrapper<Double> mHFFCreationTime
	) {
	boolean mHFFConn = mHFFConnected == null ? false : mHFFConnected;
	mHFFPos.value = 0.0;
	mHFFSpeed.value = 0.0;
	mHFFCreationTime.value = 0.0;
	hHPos.value = 0.0;
	hHSpeed.value = 0.0;
	hHCreationTime.value = 0.0;
	hHConnected.value = true;

	if( hMove && mHFFConn){
		System.err.println("OffloadHelicopter contact RescueHelicopter......");
		hOrder.value = true;
		mHMoveByOrder.value = false;
	}else if( hMove && !mHFFConn){
		System.err.println("OffloadHelicopter moves ......");
		hOrder.value = false;
		mHMoveByOrder.value = false;
	} else 	if( !hMove && mHFFConn){
		System.err.println("OffloadHelicopter contact RescueHelicopter ......");
		hOrder.value = true;
		mHMoveByOrder.value = true;
	}else if( !hMove && !mHFFConn){
		System.err.println("OffloadHelicopter order RescueHelicopter to move ......");
		hOrder.value = false;
		mHMoveByOrder.value = true;
		mHFFPos.value = hFFPos;
		mHFFSpeed.value = hFFSpeed;
		mHFFCreationTime.value = hFFCreationTime;
		
		hHPos.value = mHPos;
		hHSpeed.value = mHSpeed;
		hHCreationTime.value = mHCreationTime;
	}
	
	}
}