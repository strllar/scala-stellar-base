package org.strllar.stellarbase

package stellar_xdr {


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

    //val trans = xdr_generator.XDRGen[Transaction]

    def test(): Unit = {
      //trans.toXDR()

//      class aSampleStruct {
//        val f1:Int
//        val f2:Option[Array[Byte]]
//        val f3 = unionSwitch[EnumType](armType[SwitchEntry1].as "name1",
//        armtype[SwitchEntry2].as"name2",
//        armtype[SwitchEntry3].as"name3") {
//          case 1 => "name1";
//          case 2 => "name2";
//          case 3 => "name3";
//        }
//      }
    }
  }
}
