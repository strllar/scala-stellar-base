package org.strllar.stellarbase

package xdr {


//    struct Transaction
//    {
//    // account used to run the transaction
//    AccountID sourceAccount;
//
//    // the fee the sourceAccount will pay
//    uint32 fee;
//
//    // sequence number to consume in the account
//    SequenceNumber seqNum;
//
//    // validity range (inclusive) for the last ledger close time
//    TimeBounds* timeBounds;
//
//    Memo memo;
//
//    Operation operations<100>;
//
//    // reserved for future use
//    union switch (int v)
//    {
//    case 0:
//    void;
//    }
//    ext;
//    };

  class Transaction {

  }

  object Test {

    val trans = xdr_generator.XDRGen[Transaction]

    def test(): Unit = {
      trans.decode()
    }
  }
}
