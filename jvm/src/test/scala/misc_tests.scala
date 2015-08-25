package mytests

import com.inthenow.zcheck.{SpecLite}

import org.strllar.stellarbase.{xdr, xdr_generator => xgr, stellar_xdr}

object UnsortedSpec extends SpecLite {
  "Nothing" should {
    "be not wrong" in {
      stellar_xdr.Test.test()
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