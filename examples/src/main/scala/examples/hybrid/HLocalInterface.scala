package examples.hybrid

import akka.actor.ActorRef
import examples.hybrid.blocks.{HybridPersistentNodeViewModifier, PosBlock, PowBlock}
import examples.hybrid.mining.PosForger.{StartForging, StopForging}
import examples.hybrid.mining.PowMiner.{StartMining, StopMining}
import examples.hybrid.state.SimpleBoxTransaction
import scorex.core.LocalInterface
import scorex.core.transaction.box.proposition.PublicKey25519Proposition

//todo: add refs to pow miner / forger to constructor params
class HLocalInterface(override val viewHolderRef: ActorRef,
                      powMinerRef : ActorRef,
                      posForgerRef: ActorRef)
  extends LocalInterface[PublicKey25519Proposition, SimpleBoxTransaction, HybridPersistentNodeViewModifier] {

  override protected def onStartingPersistentModifierApplication(pmod: HybridPersistentNodeViewModifier): Unit = {}

  override protected def onFailedTransaction(tx: SimpleBoxTransaction): Unit = {}

  override protected def onFailedModification(mod: HybridPersistentNodeViewModifier): Unit = {}

  override protected def onSuccessfulTransaction(tx: SimpleBoxTransaction): Unit = {}

  //stop PoW miner and start PoS forger if PoW block comes
  //stop PoW forger and start PoW miner if PoS block comes
  override protected def onSuccessfulModification(mod: HybridPersistentNodeViewModifier): Unit = {
    mod match {
      case wb: PowBlock =>
        powMinerRef ! StopMining
        posForgerRef ! StartForging

      case sb: PosBlock =>
        posForgerRef ! StopForging
        powMinerRef ! StartMining(sb.parentId, sb.id)
    }
  }

  override protected def onNoBetterNeighbour(): Unit = {}

  override protected def onBetterNeighbourAppeared(): Unit = {}
}