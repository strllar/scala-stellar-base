package mytests

import com.inthenow.zcheck.{SpecLite}

import org.strllar.stellarbase.{xdr_generator => xgr, Network, StrKey, xdr, stellar_xdr}

object UnsortedSpec extends SpecLite {
  "Nothing" should {
    "be not wrong" in {
      stellar_xdr.Test.test()

      import org.strllar.stellarbase.StrKey
      import org.strllar.stellarbase.Networks.XLMTestnet
      import org.strllar.stellarbase.manual_xdr._

      import scodec.bits._

      val dashang = implicitly[Network].keyFactory.parseAccountID("GAKIEJNSVB44WDEVNDY6RGRVOXWHZWYM7V3UULOZBVISONXM76OYHRGU").get
      val master = StrKey.master

      val tx =
        Transaction(master.accountid, 100, 0, Memo.$MEMO_TEXT("hello world"), Transaction.Union_ext.$0())
        .$operations(Operation(Operation.Union_body.$ACCOUNT_MERGE(dashang)))

      ByteVector(XDROpaque.from(tx)) must_== hex"0000000062fc1d0bd091b2b61c0dd656346b2a68d7d347c6f2c2c8ee6d04470256fc05f700000064000000000000000100000000000000010000000b68656c6c6f20776f726c640000000001000000000000000800000000148225b2a879cb0c9568f1e89a3575ec7cdb0cfd774a2dd90d512736ecff9d8300000000"

      val txenv = TransactionEnvelope(tx)

    }
  }
}

//case class SimpleXDRStruct(element:Int)
//
//object XDRGenSpec extends SpecLite {
//  "XDRGen should" should {
//    "encode simple struct with simpel primtives" in {
//
//      val simple  = xgr.XDRGen[SimpleXDRStruct]
//
//      val result :Array[Byte] = simple.toXDR(SimpleXDRStruct(10))
//
//      result.deep must_== Array(0x00, 0x00, 0x00, 0x00).map(_.toByte).deep
//
//
////      val res = simple.fromXDR(Array(0x00, 0x00, 0x00, 0x11)) match {
////        case Success(SimpleXDRStruct(x)) => x
////        case _ => fail("decode error")
////      }
////      res.must_== 15
//
//    }
//  }
//}