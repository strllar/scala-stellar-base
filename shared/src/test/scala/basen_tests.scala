package mytests

import org.scalatest._
import org.strllar.stellarbase.{BaseN, Hex, Lit}

object BaseNSpec extends FlatSpec with Matchers  {

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

  "Base32 should" should "pass all" in {
    val b32 = BaseN.base32
    it should "encode" in {
      Lit("ME======").asBase32 shouldEqual Lit("a")
      Lit("a").toBase32 shouldEqual  Lit("ME======").asBase32
      b32.encode("a".getBytes) shouldEqual "ME======"
      b32.encode("be".getBytes) shouldEqual "MJSQ===="
      b32.encode("bee".getBytes) shouldEqual "MJSWK==="
      b32.encode("beer".getBytes) shouldEqual "MJSWK4Q="
      b32.encode("beers".getBytes) shouldEqual "MJSWK4TT"
      b32.encode("beers 1".getBytes) shouldEqual "MJSWK4TTEAYQ===="
      b32.encode("shockingly dismissed".getBytes) shouldEqual "ONUG6Y3LNFXGO3DZEBSGS43NNFZXGZLE"
    }
    it should "decode" in {
      (b32.decode("ME======") sameElements "a")
      (b32.decode("MJSQ====") sameElements "be")
      (b32.decode("ONXW4===") sameElements "son")
      (b32.decode("MJSWK===") sameElements "bee")
      (b32.decode("MJSWK4Q=") sameElements "beer")
      (b32.decode("MJSWK4TT") sameElements "beers")
      (b32.decode("MJSWK4TTN5XA====") sameElements "beerson")
      (b32.decode("MJSWK4TTEAYQ====") sameElements "beers 1")
      (b32.decode("ONUG6Y3LNFXGO3DZEBSGS43NNFZXGZLE") sameElements "shockingly dismissed")
    }

    def BASE64(s:String) = BaseN.base64.encode(s.getBytes)
    def BASE32(s:String) = BaseN.base32.encode(s.getBytes)
    def BASE32HEX(s:String) = BaseN.base32hex.encode(s.getBytes)
    def BASE16(s:String) = BaseN.base16.encode(s.getBytes)
    it should "pass Test Vectors in RFC 4648" in {
      BASE64("") shouldEqual ""
      BASE64("f") shouldEqual "Zg=="
      BASE64("fo") shouldEqual "Zm8="
      BASE64("foo") shouldEqual "Zm9v"
      BASE64("foob") shouldEqual "Zm9vYg=="
      BASE64("fooba") shouldEqual "Zm9vYmE="
      BASE64("foobar") shouldEqual "Zm9vYmFy"
      BASE32("") shouldEqual ""
      BASE32("f") shouldEqual "MY======"
      BASE32("fo") shouldEqual "MZXQ===="
      BASE32("foo") shouldEqual "MZXW6==="
      BASE32("foob") shouldEqual "MZXW6YQ="
      BASE32("fooba") shouldEqual "MZXW6YTB"
      BASE32("foobar") shouldEqual "MZXW6YTBOI======"
      BASE32HEX("") shouldEqual ""
      BASE32HEX("f") shouldEqual "CO======"
      BASE32HEX("fo") shouldEqual "CPNG===="
      BASE32HEX("foo") shouldEqual "CPNMU==="
      BASE32HEX("foob") shouldEqual "CPNMUOG="
      BASE32HEX("fooba") shouldEqual "CPNMUOJ1"
      BASE32HEX("foobar") shouldEqual "CPNMUOJ1E8======"
      BASE16("") shouldEqual ""
      BASE16("f") shouldEqual "66"
      BASE16("fo") shouldEqual "666F"
      BASE16("foo") shouldEqual "666F6F"
      BASE16("foob") shouldEqual "666F6F62"
      BASE16("fooba") shouldEqual "666F6F6261"
      BASE16("foobar") shouldEqual "666F6F626172"
    }
    it should "be binary safe" in {
      b32.decode(
        b32.encode(Array(0x00, 0xff, 0x88).map(_.toByte))
      ).deep  shouldEqual  Hex("00ff88").bytes.deep

      b32.encode(Hex("f61e1f998d69151de8334dbe753ab17ae831c13849a6aecd95d0a4e5dc25").bytes).shouldEqual("6YPB7GMNNEKR32BTJW7HKOVRPLUDDQJYJGTK5TMV2CSOLXBF")

      b32.decode("6YPB7GMNNEKR32BTJW7HKOVRPLUDDQJYJGTK5TMV2CSOLXBF").deep shouldEqual Hex("f61e1f998d69151de8334dbe753ab17ae831c13849a6aecd95d0a4e5dc25").bytes.deep
    }
  }
}