package hybrid.transaction

import examples.commons.SimpleBoxTransactionCompanion
import examples.hybrid.TreasuryManager
import examples.hybrid.transaction.BallotTransaction.VoterType
import examples.hybrid.transaction.{BallotTransaction, BallotTransactionCompanion}
import org.scalatest.FunSuite
import treasury.crypto.core.{Cryptosystem, One, VoteCases}
import treasury.crypto.voting.RegularVoter

class BallotTransactionTest extends FunSuite {

  val cs = TreasuryManager.cs
  val (privKey, pubKey) = cs.createKeyPair

  test("serialization") {
    val numberOfExperts = 6
    val voter = new RegularVoter(cs, numberOfExperts, pubKey, One)
    val ballot = voter.produceVote(0, VoteCases.Abstain)
    val ballot2 = voter.produceVote(1, VoteCases.Abstain)

    val txBytes = BallotTransaction.create(pubKey, VoterType.Voter, Seq(ballot,ballot2), 12).get.bytes
    val tx = SimpleBoxTransactionCompanion.parseBytes(txBytes).get.asInstanceOf[BallotTransaction]

    assert(voter.verifyBallot(tx.ballots(0)))
    assert(tx.ballots(0).proposalId == 0)
    assert(voter.verifyBallot(tx.ballots(1)))
    assert(tx.ballots(1).proposalId == 1)
    assert(tx.pubKey == pubKey)
    assert(tx.voterType == VoterType.Voter)
    assert(tx.epochID == 12)
  }
}