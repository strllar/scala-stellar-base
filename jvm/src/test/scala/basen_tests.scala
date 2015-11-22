package mytests

import com.inthenow.zcheck.{SpecLite}
import org.strllar.stellarbase.{BaseN}

object BaseNSpec extends SpecLite {

//  "Hex should" should {
//    "be constructed from legal hex string" in {
//      Hex("a dead beef bedded a defaced babe")
//      Hex("C0FFEEBABE")
//    }
//    "be constructed from 0xx tuple" in {
//      Hex(0xde, 0xca, 0xde)
//      Hex(0xCA, 0xFE, 0xBA, 0xBE)
//    }
//    "throw from illegal hex string" in {
//      Hex("decades")
//      Hex("C0FFEEBABE!")
//    }
//  }

  "Base32 should" should {
    val b32 = BaseN.base32
    "encode" in {
      //Lit("a").toBase32 must_== Lit("ME======")
      b32.encode("a".getBytes) must_== "ME======"
      b32.encode("be".getBytes) must_== "MJSQ===="
      b32.encode("bee".getBytes) must_== "MJSWK==="
      b32.encode("beer".getBytes) must_== "MJSWK4Q="
      b32.encode("beers".getBytes) must_== "MJSWK4TT"
      b32.encode("beers 1".getBytes) must_== "MJSWK4TTEAYQ===="
      b32.encode("shockingly dismissed".getBytes) must_== "ONUG6Y3LNFXGO3DZEBSGS43NNFZXGZLE"
    }
    "decode" in {
      check(b32.decode("ME======") sameElements "a")
      check(b32.decode("MJSQ====") sameElements "be")
      check(b32.decode("ONXW4===") sameElements "son")
      check(b32.decode("MJSWK===") sameElements "bee")
      check(b32.decode("MJSWK4Q=") sameElements "beer")
      check(b32.decode("MJSWK4TT") sameElements "beers")
      check(b32.decode("MJSWK4TTN5XA====") sameElements "beerson")
      check(b32.decode("MJSWK4TTEAYQ====") sameElements "beers 1")
      check(b32.decode("ONUG6Y3LNFXGO3DZEBSGS43NNFZXGZLE") sameElements "shockingly dismissed")
    }
//    "be binary safe" in {
//      check (b32.decode(
//        b32.encode(Array(0x00, 0xff, 0x88).map(_.toByte))
//      )  sameElements (Hex("00ff88"):Array[Byte]))
//
//      b32.encode(Hex("f61e1f998d69151de8334dbe753ab17ae831c13849a6aecd95d0a4e5dc25")).must_==("6YPB7GMNNEKR32BTJW7HKOVRPLUDDQJYJGTK5TMV2CSOLXBF")
//
//      check (b32.decode("6YPB7GMNNEKR32BTJW7HKOVRPLUDDQJYJGTK5TMV2CSOLXBF") sameElements (Hex("f61e1f998d69151de8334dbe753ab17ae831c13849a6aecd95d0a4e5dc25"):Array[Byte]))
//    }
  }
}